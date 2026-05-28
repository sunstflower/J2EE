import { useEffect, useState } from "react";
import { subscribeDesktopRuntime } from "../../shared/runtime/desktopRuntime";
import type { ImportedProxyNode } from "../../shared/types";
import { createProxiesService } from "./proxiesService";

type ImportedProxyNodesState = {
  data: ImportedProxyNode[];
  loading: boolean;
  error: string | null;
};

export function useImportedProxyNodes() {
  const [state, setState] = useState<ImportedProxyNodesState>({
    data: [],
    loading: true,
    error: null
  });

  useEffect(() => {
    let active = true;

    async function load() {
      const service = createProxiesService();
      try {
        const data = await service.getImportedNodes();
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
          data: [],
          loading: false,
          error: error instanceof Error ? error.message : "Failed to load imported proxy nodes"
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
