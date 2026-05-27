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
      notifyRecommendationChange?: (recommendedServices: string[]) => Promise<{ shown: boolean }>;
      updateTrayState?: (payload: {
        systemProxyStatus: string;
        recommendationPending: boolean;
        recommendedServices: string[];
      }) => Promise<{ updated: boolean }>;
      onAcceptRecommendation?: (listener: () => void) => () => void;
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

export async function notifyDesktopRecommendationChange(recommendedServices: string[]) {
  const notifier = window.desktopRuntime?.notifyRecommendationChange;

  if (!notifier) {
    return { shown: false };
  }

  return notifier(recommendedServices);
}

export async function updateDesktopTrayState(payload: {
  systemProxyStatus: string;
  recommendationPending: boolean;
  recommendedServices: string[];
}) {
  const updater = window.desktopRuntime?.updateTrayState;

  if (!updater) {
    return { updated: false };
  }

  return updater(payload);
}

export function subscribeDesktopAcceptRecommendation(listener: () => void) {
  const subscribe = window.desktopRuntime?.onAcceptRecommendation;

  if (!subscribe) {
    return () => {};
  }

  return subscribe(listener);
}
