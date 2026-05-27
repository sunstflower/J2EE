type DesktopRuntimePayload = {
  platform: string;
  localApiBaseUrl: string | null;
  localApiSessionToken: string | null;
};

const EMPTY_RUNTIME: DesktopRuntimePayload = {
  platform: typeof window !== "undefined" ? window.navigator.platform : "unknown",
  localApiBaseUrl: null,
  localApiSessionToken: null
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
    listener(EMPTY_RUNTIME);

    return () => {};
  }

  return subscribe(listener);
}

export function hasDesktopRuntimeBridge() {
  return Boolean(window.desktopRuntime?.getRuntime || window.desktopRuntime?.subscribe);
}

export function hasResolvedDesktopLocalApi(runtime: DesktopRuntimePayload | null) {
  return Boolean(runtime?.localApiBaseUrl && runtime?.localApiSessionToken);
}

export async function waitForDesktopLocalApiRuntime(timeoutMs = 5000) {
  const currentRuntime = getDesktopRuntime();
  if (hasResolvedDesktopLocalApi(currentRuntime)) {
    return currentRuntime;
  }

  if (!hasDesktopRuntimeBridge()) {
    return currentRuntime;
  }

  return new Promise<DesktopRuntimePayload>((resolve) => {
    let settled = false;
    let unsubscribe = () => {};
    let timer = 0;

    const finish = (runtime: DesktopRuntimePayload) => {
      if (settled) {
        return;
      }

      settled = true;
      unsubscribe();
      window.clearTimeout(timer);
      resolve(runtime);
    };

    unsubscribe = subscribeDesktopRuntime((runtime) => {
      if (hasResolvedDesktopLocalApi(runtime)) {
        finish(runtime);
      }
    });

    timer = window.setTimeout(() => {
      finish(getDesktopRuntime() ?? EMPTY_RUNTIME);
    }, timeoutMs);
  });
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
