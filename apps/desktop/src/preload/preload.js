const { contextBridge, ipcRenderer } = require("electron");

const runtimeState = {
  platform: process.platform,
  localApiBaseUrl: null,
  localApiSessionToken: null
};
const runtimeListeners = new Set();
const recommendationAcceptListeners = new Set();

ipcRenderer.on("desktop-runtime", (_event, payload) => {
  runtimeState.platform = payload.platform;
  runtimeState.localApiBaseUrl = payload.localApiBaseUrl;
  runtimeState.localApiSessionToken = payload.localApiSessionToken;
  runtimeListeners.forEach((listener) => listener({ ...runtimeState }));
});

ipcRenderer.on("desktop-accept-recommendation", () => {
  recommendationAcceptListeners.forEach((listener) => listener());
});

contextBridge.exposeInMainWorld("desktopRuntime", {
  getRuntime() {
    return runtimeState;
  },
  subscribe(listener) {
    runtimeListeners.add(listener);
    listener({ ...runtimeState });

    return () => {
      runtimeListeners.delete(listener);
    };
  },
  notifyRecommendationChange(recommendedServices) {
    return ipcRenderer.invoke("desktop-notify-recommendation-change", {
      recommendedServices
    });
  },
  updateTrayState(payload) {
    return ipcRenderer.invoke("desktop-update-tray-state", payload);
  },
  onAcceptRecommendation(listener) {
    recommendationAcceptListeners.add(listener);

    return () => {
      recommendationAcceptListeners.delete(listener);
    };
  }
});
