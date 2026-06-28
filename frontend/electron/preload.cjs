const { contextBridge, ipcRenderer } = require('electron')

contextBridge.exposeInMainWorld('electronAPI', {
    version: () => process.versions.electron,
})

// Controles da janela usados pela barra de título personalizada (estilo macOS).
contextBridge.exposeInMainWorld('windowControls', {
    minimize: () => ipcRenderer.send('win:minimize'),
    fullscreen: () => ipcRenderer.send('win:fullscreen'),
    close: () => ipcRenderer.send('win:close'),
})
