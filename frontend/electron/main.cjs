const { app, BrowserWindow } = require('electron')
const { spawn } = require('child_process')
const path = require('path')

const DEV_SERVER = 'http://localhost:5173'
let javaProcess


function startJava() {
    const jarPath = app.isPackaged
        ? path.join(process.resourcesPath, 'backend', 'app.jar')
        : path.join(__dirname, '..', 'resources', 'backend', 'app.jar')

    console.log('Starting Java from:', jarPath)

    console.log('JAR path:', jarPath)

    const fs = require('fs')
    console.log('JAR exists:', fs.existsSync(jarPath))

    // Passa um caminho de dados absoluto e gravavel para o backend, em vez de
    // deixar o Spring resolver um caminho relativo a partir do cwd (frontend/),
    // o que quebrava a escrita (HTTP 500). Empacotado: pasta userData do app;
    // dev: a pasta de dados do backend no repositorio.
    const dataDir = app.isPackaged
        ? path.join(app.getPath('userData'), 'data')
        : path.join(__dirname, '..', '..', 'backend', 'src', 'main', 'resources', 'data')

    console.log('Data dir:', dataDir)

    javaProcess = spawn('java', ['-jar', jarPath, `--app.data-dir=${dataDir}`], {
        stdio: ['ignore', 'pipe', 'pipe']
    })

    javaProcess.stdout.on('data', (data) => console.log('Java:', data.toString()))
    javaProcess.stderr.on('data', (data) => console.error('Java error:', data.toString()))
    javaProcess.on('error', (err) => console.error('Failed to spawn Java:', err))
    javaProcess.on('close', (code) => console.log('Java exited with code:', code))
}

function createWindow() {
    const win = new BrowserWindow({
        width: 800,
        height: 500,
        webPreferences: {
            preload: app.isPackaged
                ? path.join(process.resourcesPath, 'app.asar.unpacked', 'electron', 'preload.cjs')
                : path.join(__dirname, 'preload.cjs'),
            contextIsolation: true,
            nodeIntegration: false,
        },
    })

    if (app.isPackaged) {
        win.loadFile(path.join(__dirname, '..', 'dist', 'index.html'))
    } else {
        win.loadURL('http://localhost:5173')
    }

    win.webContents.openDevTools()
}

app.whenReady().then(() => {
    startJava()
    setTimeout(createWindow, 6000)
})

app.on('window-all-closed', () => {
    javaProcess?.kill()
    if (process.platform !== 'darwin') app.quit()
})