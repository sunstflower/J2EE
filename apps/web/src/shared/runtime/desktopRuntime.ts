type DesktopRuntimePayload = {
  platform: string;
  localApiBaseUrl: string | null;
  localApiSessionToken: string | null;
};

declare global {
  interface Window {
    desktopRuntime?: {
      getRuntime?: () => DesktopRuntimePayload;
      subscribe?: (listener: (runtime: DesktopRuntimePayload) => void) => () => void;
    };
  }
}

export function getDesktopRuntime(): DesktopRuntimePayload | null {
  const getter = window.desktopRuntime?.getRuntime;

  if (!getter) {
    return null;
  }

  return getter();
}

export function subscribeDesktopRuntime(listener: (runtime: DesktopRuntimePayload) => void) {
  const subscribe = window.desktopRuntime?.subscribe;

  if (!subscribe) {
    listener({
      platform: window.navigator.platform,
      localApiBaseUrl: null,
      localApiSessionToken: null
    });

    return () => {};
  }

  return subscribe(listener);
}
