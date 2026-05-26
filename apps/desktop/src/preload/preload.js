const { contextBridge, ipcRenderer } = require("electron");

const runtimeState = {
  platform: process.platform,
  localApiBaseUrl: null,
  localApiSessionToken: null
};
const runtimeListeners = new Set();

ipcRenderer.on("desktop-runtime", (_event, payload) => {
  runtimeState.platform = payload.platform;
  runtimeState.localApiBaseUrl = payload.localApiBaseUrl;
  runtimeState.localApiSessionToken = payload.localApiSessionToken;
  runtimeListeners.forEach((listener) => listener({ ...runtimeState }));
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
  }
});
