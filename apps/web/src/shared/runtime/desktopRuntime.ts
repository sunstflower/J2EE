type DesktopRuntimePayload = {
  platform: string;
  localApiBaseUrl: string | null;
  localApiSessionToken: string | null;
};

declare global {
  interface Window {
    desktopRuntime?: {
      getRuntime?: () => DesktopRuntimePayload;
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
