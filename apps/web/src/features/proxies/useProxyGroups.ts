import { useEffect, useState } from "react";
import { subscribeDesktopRuntime } from "../../shared/runtime/desktopRuntime";
import type { ProxyGroupSelection } from "../../shared/types";
import { createProxyGroupsService } from "./proxyGroupsService";

type ProxyGroupsState = {
  data: ProxyGroupSelection[];
  loading: boolean;
  saving: boolean;
  error: string | null;
};

export function useProxyGroups() {
  const [state, setState] = useState<ProxyGroupsState>({
    data: [],
    loading: true,
    saving: false,
    error: null
  });

  useEffect(() => {
    let active = true;

    async function load() {
      const service = createProxyGroupsService();
      try {
        const data = await service.getGroups();
        if (!active) {
          return;
        }

        setState({
          data,
          loading: false,
          saving: false,
          error: null
        });
      } catch (error) {
        if (!active) {
          return;
        }

        setState({
          data: [],
          loading: false,
          saving: false,
          error: error instanceof Error ? error.message : "Failed to load proxy groups"
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

  async function updateSelection(groupName: string, selectedNodeName: string) {
    const service = createProxyGroupsService();
    setState((current) => ({ ...current, saving: true, error: null }));
    try {
      const updated = await service.updateSelection(groupName, selectedNodeName);
      setState((current) => ({
        data: current.data.map((group) => (group.groupName === groupName ? updated : group)),
        loading: false,
        saving: false,
        error: null
      }));
    } catch (error) {
      setState((current) => ({
        ...current,
        saving: false,
        error: error instanceof Error ? error.message : "Failed to update proxy selection"
      }));
    }
  }

  return {
    ...state,
    updateSelection
  };
}
