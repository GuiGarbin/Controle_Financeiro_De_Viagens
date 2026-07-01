const { app, BrowserWindow, Menu, ipcMain } = require('electron')
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
    // Remove a barra de menu padrao do Electron (File/Edit/View...).
    Menu.setApplicationMenu(null)

    const win = new BrowserWindow({
        width: 1307,
        height: 720,
        minWidth: 1307,   // nao permite redimensionar abaixo do tamanho inicial
        minHeight: 720,
        center: true,     // abre centralizado na tela
        autoHideMenuBar: true,
        frame: false,            // sem a moldura nativa do Windows (usamos barra propria)
        transparent: true,       // permite cantos arredondados via CSS
        backgroundColor: '#00000000',
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

    // Controles de janela da barra personalizada (estilo macOS) no frontend.
    let savedBounds = null
    let isFullscreen = false

    win.on('enter-full-screen', () => { isFullscreen = true })
    win.on('leave-full-screen', () => {
        isFullscreen = false
        // Restaura o tamanho/posição anterior depois que a saída termina.
        if (savedBounds) win.setBounds(savedBounds)
    })
    ipcMain.on('win:minimize', () => win.minimize())
    ipcMain.on('win:fullscreen', () => {
        if (isFullscreen) {
            win.setFullScreen(false)
        } else {
            savedBounds = win.getBounds() // guarda o tamanho atual (inicial ou já redimensionado)
            win.setFullScreen(true)
        }
    })
    ipcMain.on('win:close', () => win.close())
}

app.whenReady().then(() => {
    startJava()
    setTimeout(createWindow, 6000)
})

function killJava() {
    const proc = javaProcess
    if (!proc) return
    javaProcess = null
    const pid = proc.pid
    if (process.platform === 'win32' && pid) {
        try {
            spawn('taskkill', ['/pid', String(pid), '/T', '/F'])
        } catch (e) {
            try { proc.kill() } catch (_) {}
        }
    } else {
        try { proc.kill('SIGKILL') } catch (_) {}
    }
}

app.on('window-all-closed', () => {
    killJava()
    if (process.platform !== 'darwin') app.quit()
})

app.on('before-quit', killJava)
process.on('exit', killJava)