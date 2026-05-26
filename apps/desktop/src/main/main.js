const { app, BrowserWindow } = require("electron");
const path = require("node:path");
const { startBackend } = require("./backend");

const DEFAULT_WINDOW = {
  width: 1280,
  height: 840,
  minWidth: 1024,
  minHeight: 720
};

let backendRuntime = null;

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

  mainWindow.webContents.on("did-finish-load", () => {
    if (!backendRuntime) {
      return;
    }

    mainWindow.webContents.send("desktop-runtime", {
      platform: process.platform,
      localApiBaseUrl: `http://127.0.0.1:${backendRuntime.port}/api/v1`,
      localApiSessionToken: backendRuntime.sessionToken
    });
  });

  mainWindow.loadURL(resolveRendererEntry());
}

app.whenReady().then(async () => {
  try {
    backendRuntime = await startBackend();
    createMainWindow();
  } catch (error) {
    console.error(error);
    app.quit();
  }

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

app.on("before-quit", () => {
  if (backendRuntime?.child && !backendRuntime.child.killed) {
    backendRuntime.child.kill();
  }
});
