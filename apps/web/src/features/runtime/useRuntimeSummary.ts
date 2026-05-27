import { useEffect, useState } from "react";
import type { RuntimeSummary } from "../../shared/types";
import { createRuntimeService } from "./runtimeService";
import { subscribeDesktopRuntime } from "../../shared/runtime/desktopRuntime";

type RuntimeState = {
  data: RuntimeSummary | null;
  loading: boolean;
  error: string | null;
};

export function useRuntimeSummary() {
  const [state, setState] = useState<RuntimeState>({
    data: null,
    loading: true,
    error: null
  });

  useEffect(() => {
    let active = true;
    const service = createRuntimeService();

    async function load() {
      try {
        const data = await service.getRuntimeSummary();
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
          error: error instanceof Error ? error.message : "Failed to load runtime summary"
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

  return state;
}
