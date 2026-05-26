import { useEffect, useState } from "react";
import type { RuntimeSummary } from "../../shared/types";
import { createRuntimeService } from "./runtimeService";

type RuntimeState = {
  data: RuntimeSummary | null;
  loading: boolean;
  error: string | null;
};

const service = createRuntimeService();

export function useRuntimeSummary() {
  const [state, setState] = useState<RuntimeState>({
    data: null,
    loading: true,
    error: null
  });

  useEffect(() => {
    let active = true;

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

    return () => {
      active = false;
    };
  }, []);

  return state;
}
