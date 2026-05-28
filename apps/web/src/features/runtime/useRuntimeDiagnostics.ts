import { useEffect, useState } from "react";
import type { RuntimeErrors, RuntimeLogs } from "../../shared/types";
import { createRuntimeService } from "./runtimeService";
import { subscribeDesktopRuntime } from "../../shared/runtime/desktopRuntime";

type RuntimeDiagnosticsState = {
  logs: RuntimeLogs | null;
  errors: RuntimeErrors | null;
  loading: boolean;
  error: string | null;
};

export function useRuntimeDiagnostics() {
  const [state, setState] = useState<RuntimeDiagnosticsState>({
    logs: null,
    errors: null,
    loading: true,
    error: null
  });

  useEffect(() => {
    let active = true;
    const service = createRuntimeService();

    async function load() {
      try {
        const [logs, errors] = await Promise.all([
          service.getRuntimeLogs(12),
          service.getRuntimeErrors()
        ]);
        if (!active) {
          return;
        }

        setState({
          logs,
          errors,
          loading: false,
          error: null
        });
      } catch (error) {
        if (!active) {
          return;
        }

        setState({
          logs: null,
          errors: null,
          loading: false,
          error: error instanceof Error ? error.message : "Failed to load runtime diagnostics"
        });
      }
    }

    void load();
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
