const { app, BrowserWindow } = require("electron");
const path = require("node:path");

const DEFAULT_WINDOW = {
  width: 1280,
  height: 840,
  minWidth: 1024,
  minHeight: 720
};

function resolveRendererEntry() {
  const rendererUrl = process.env.MAC_PROXY_RENDERER_URL;

  if (rendererUrl) {
    return rendererUrl;
  }

  return `file://${path.join(__dirname, "../../renderer/index.html")}`;
}

function createMainWindow() {
  const mainWindow = new BrowserWindow({
    ...DEFAULT_WINDOW,
    title: "mac-proxy-client",
    webPreferences: {
      preload: path.join(__dirname, "../preload/preload.js"),
      contextIsolation: true,
      nodeIntegration: false
    }
  });

  mainWindow.loadURL(resolveRendererEntry());
}

app.whenReady().then(() => {
  createMainWindow();

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createMainWindow();
    }
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
