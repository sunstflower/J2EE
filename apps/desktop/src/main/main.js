const { app, BrowserWindow, Menu, Notification, Tray, ipcMain } = require("electron");
const path = require("node:path");
const { startBackend } = require("./backend");
const { resolveTrayImage } = require("./trayAssets");

app.setName("mac-proxy-client");

const DEFAULT_WINDOW = {
  width: 1280,
  height: 840,
  minWidth: 1024,
  minHeight: 720
};

let backendRuntime = null;
let lastRecommendationNotificationKey = "";
let mainWindow = null;
let tray = null;
let trayState = {
  systemProxyStatus: "Unknown",
  recommendationPending: false,
  recommendedServices: []
};
let pendingTrayAcceptRecommendation = false;

function resolveRendererEntry() {
  const rendererUrl = process.env.MAC_PROXY_RENDERER_URL;

  if (rendererUrl) {
    return rendererUrl;
  }

  if (app.isPackaged) {
    return `file://${path.join(process.resourcesPath, "web", "index.html")}`;
  }

  return `file://${path.join(__dirname, "../../renderer/index.html")}`;
}

function createMainWindow() {
  mainWindow = new BrowserWindow({
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

    if (pendingTrayAcceptRecommendation) {
      mainWindow.webContents.send("desktop-accept-recommendation");
      pendingTrayAcceptRecommendation = false;
    }
  });

  mainWindow.on("closed", () => {
    mainWindow = null;
  });

  mainWindow.loadURL(resolveRendererEntry());
}

function setupDesktopNotifications() {
  ipcMain.handle("desktop-notify-recommendation-change", (_event, payload) => {
    const services = Array.isArray(payload?.recommendedServices) ? payload.recommendedServices : [];
    const notificationKey = services.join("|");

    if (!services.length || notificationKey === lastRecommendationNotificationKey) {
      return { shown: false };
    }

    lastRecommendationNotificationKey = notificationKey;

    if (!Notification.isSupported()) {
      return { shown: false };
    }

    const body =
      services.length === 1
        ? `Recommended system proxy target changed to ${services[0]}.`
        : `Recommended system proxy targets changed to ${services.join(", ")}.`;

    const notification = new Notification({
      title: "mac-proxy-client",
      body
    });
    notification.show();

    return { shown: true };
  });
}

function setupTray() {
  if (tray) {
    return;
  }

  const trayImage = resolveTrayImage(resolveTrayVisualState());
  tray = new Tray(trayImage);
  tray.on("click", () => {
    focusMainWindow();
  });
  updateTrayMenu();
}

function focusMainWindow() {
  if (!mainWindow) {
    createMainWindow();
    return;
  }

  if (mainWindow.isMinimized()) {
    mainWindow.restore();
  }

  mainWindow.show();
  mainWindow.focus();
}

function updateTrayMenu() {
  if (!tray) {
    return;
  }

  updateTrayPresentation();

  const recommendationLabel = trayState.recommendationPending
    ? `Accept recommendation: ${trayState.recommendedServices.join(", ")}`
    : "No pending recommendation";

  const menu = Menu.buildFromTemplate([
    {
      label: `System proxy: ${trayState.systemProxyStatus}`,
      enabled: false
    },
    {
      label: trayState.recommendationPending ? "Recommendation pending" : "Recommendation stable",
      enabled: false
    },
    {
      label: recommendationLabel,
      enabled: trayState.recommendationPending,
      click: () => {
        pendingTrayAcceptRecommendation = true;
        if (mainWindow) {
          mainWindow.webContents.send("desktop-accept-recommendation");
          pendingTrayAcceptRecommendation = false;
        }
        focusMainWindow();
      }
    },
    { type: "separator" },
    {
      label: "Open Window",
      click: () => {
        focusMainWindow();
      }
    },
    {
      label: "Quit",
      click: () => {
        app.quit();
      }
    }
  ]);

  tray.setContextMenu(menu);
}

function updateTrayPresentation() {
  if (!tray) {
    return;
  }

  const title = resolveTrayTitle();
  const tooltip = resolveTrayTooltip();
  const image = resolveTrayImage(resolveTrayVisualState());

  tray.setImage(image);
  tray.setTitle(title);
  tray.setToolTip(tooltip);
}

function resolveTrayVisualState() {
  if (trayState.recommendationPending) {
    return "pending";
  }

  const normalizedStatus = String(trayState.systemProxyStatus || "").toLowerCase();
  if (normalizedStatus.includes("on")) {
    return "on";
  }
  if (normalizedStatus.includes("off")) {
    return "off";
  }

  return "idle";
}

function resolveTrayTitle() {
  if (trayState.recommendationPending) {
    return "Proxy !";
  }

  const normalizedStatus = String(trayState.systemProxyStatus || "").toLowerCase();
  if (normalizedStatus.includes("on")) {
    return "Proxy On";
  }
  if (normalizedStatus.includes("off")) {
    return "Proxy Off";
  }

  return "Proxy";
}

function resolveTrayTooltip() {
  const base = `mac-proxy-client\nSystem proxy: ${trayState.systemProxyStatus}`;

  if (!trayState.recommendationPending || !trayState.recommendedServices.length) {
    return `${base}\nRecommendation: stable`;
  }

  return `${base}\nRecommendation pending: ${trayState.recommendedServices.join(", ")}`;
}

app.whenReady().then(async () => {
  try {
    setupDesktopNotifications();
    setupTray();
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

ipcMain.handle("desktop-update-tray-state", (_event, payload) => {
  trayState = {
    systemProxyStatus: payload?.systemProxyStatus ?? "Unknown",
    recommendationPending: Boolean(payload?.recommendationPending),
    recommendedServices: Array.isArray(payload?.recommendedServices) ? payload.recommendedServices : []
  };
  updateTrayMenu();
  return { updated: true };
});
