import { useEffect, useMemo, useState } from "react";
import type { DashboardState } from "../../shared/types";
import { createDashboardService } from "./dashboardService";

type DashboardLoadState = {
  data: DashboardState | null;
  loading: boolean;
  error: string | null;
};

export function useDashboardState() {
  const service = useMemo(() => createDashboardService(), []);
  const [state, setState] = useState<DashboardLoadState>({
    data: null,
    loading: true,
    error: null
  });

  useEffect(() => {
    let active = true;

    async function load() {
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

    load();

    return () => {
      active = false;
    };
  }, [service]);

  return state;
}
