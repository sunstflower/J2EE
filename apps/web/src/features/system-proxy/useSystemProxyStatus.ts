import { useEffect, useState } from "react";
import type { SystemProxyStatus } from "../../shared/types";
import { createSystemProxyService } from "./systemProxyService";
import { subscribeDesktopRuntime } from "../../shared/runtime/desktopRuntime";

type SystemProxyState = {
  data: SystemProxyStatus | null;
  loading: boolean;
  acting: boolean;
  error: string | null;
};

export function useSystemProxyStatus() {
  const [state, setState] = useState<SystemProxyState>({
    data: null,
    loading: true,
    acting: false,
    error: null
  });

  useEffect(() => {
    let active = true;
    const service = createSystemProxyService();

    async function load() {
      try {
        const data = await service.getStatus();
        if (!active) {
          return;
        }

        setState({
          data,
          loading: false,
          acting: false,
          error: null
        });
      } catch (error) {
        if (!active) {
          return;
        }

        setState({
          data: null,
          loading: false,
          acting: false,
          error: error instanceof Error ? error.message : "Failed to load system proxy status"
        });
      }
    }

    load();
    const unsubscribe = subscribeDesktopRuntime(() => {
      void load();
    });

    return () => {
      active = false;
      unsubscribe();
    };
  }, []);

  async function update(payload: {
    enabled: boolean;
    scope?: "ALL_ENABLED" | "SELECTED";
    services?: string[];
    acceptRecommendedServices?: boolean;
  }) {
    const service = createSystemProxyService();
    setState((current) => ({ ...current, acting: true, error: null }));

    try {
      const data = await service.update(payload);
      setState({
        data,
        loading: false,
        acting: false,
        error: null
      });
    } catch (error) {
      setState((current) => ({
        ...current,
        acting: false,
        error: error instanceof Error ? error.message : "Failed to update system proxy"
      }));
    }
  }

  return {
    ...state,
    enable: (options?: { scope?: "ALL_ENABLED" | "SELECTED"; services?: string[] }) =>
      update({ enabled: true, ...options }),
    disable: (options?: { scope?: "ALL_ENABLED" | "SELECTED"; services?: string[] }) =>
      update({ enabled: false, ...options }),
    setEnabled: (enabled: boolean, options?: { scope?: "ALL_ENABLED" | "SELECTED"; services?: string[] }) =>
      update({ enabled, ...options }),
    acceptRecommendedServices: () =>
      update({ enabled: state.data?.enabled ?? false, acceptRecommendedServices: true })
  };
}
