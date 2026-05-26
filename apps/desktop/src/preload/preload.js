const { contextBridge, ipcRenderer } = require("electron");

const runtimeState = {
  platform: process.platform,
  localApiBaseUrl: null,
  localApiSessionToken: null
};

ipcRenderer.on("desktop-runtime", (_event, payload) => {
  runtimeState.platform = payload.platform;
  runtimeState.localApiBaseUrl = payload.localApiBaseUrl;
  runtimeState.localApiSessionToken = payload.localApiSessionToken;
});

contextBridge.exposeInMainWorld("desktopRuntime", {
  getRuntime() {
    return runtimeState;
  }
});
