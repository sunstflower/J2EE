import { useEffect, useState } from "react";
import { subscribeDesktopRuntime } from "../../shared/runtime/desktopRuntime";
import type { DashboardState } from "../../shared/types";
import { createDashboardService } from "./dashboardService";

type DashboardLoadState = {
  data: DashboardState | null;
  loading: boolean;
  error: string | null;
};

export function useDashboardState() {
  const [state, setState] = useState<DashboardLoadState>({
    data: null,
    loading: true,
    error: null
  });

  useEffect(() => {
    let active = true;
    let loaded = false;

    async function loadFromCurrentRuntime() {
      const service = createDashboardService();

      try {
        const data = await service.getDashboardState();

        if (!active) {
          return;
        }

        setState({
          data,
          loading: false,
          error: null
        });
        loaded = true;
      } catch (error) {
        if (!active) {
          return;
        }

        setState({
          data: null,
          loading: false,
          error: error instanceof Error ? error.message : "Unknown dashboard load failure"
        });
      }
    }

    const unsubscribe = subscribeDesktopRuntime((runtime) => {
      if (loaded && runtime.localApiBaseUrl && runtime.localApiSessionToken) {
        return;
      }

      setState((current) => ({
        ...current,
        loading: true,
        error: null
      }));

      loadFromCurrentRuntime();
    });

    return () => {
      active = false;
      unsubscribe();
    };
  }, []);

  return state;
}
