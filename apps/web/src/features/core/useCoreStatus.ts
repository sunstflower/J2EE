import { useEffect, useState } from "react";
import type { CoreStatus } from "../../shared/types";
import { createCoreService } from "./coreService";
import { subscribeDesktopRuntime } from "../../shared/runtime/desktopRuntime";

type CoreState = {
  data: CoreStatus | null;
  loading: boolean;
  acting: boolean;
  error: string | null;
};

export function useCoreStatus() {
  const [state, setState] = useState<CoreState>({
    data: null,
    loading: true,
    acting: false,
    error: null
  });

  useEffect(() => {
    let active = true;
    const service = createCoreService();

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
          error: error instanceof Error ? error.message : "Failed to load core status"
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

  async function run(action: "start" | "stop" | "reload") {
    const service = createCoreService();
    setState((current) => ({ ...current, acting: true, error: null }));
    try {
      const data =
        action === "start"
          ? await service.start()
          : action === "stop"
            ? await service.stop()
            : await service.reload();

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
        error: error instanceof Error ? error.message : `Failed to ${action} core`
      }));
    }
  }

  return {
    ...state,
    start: () => run("start"),
    stop: () => run("stop"),
    reload: () => run("reload")
  };
}
