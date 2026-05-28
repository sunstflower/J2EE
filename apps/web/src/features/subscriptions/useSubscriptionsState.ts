import { useEffect, useState } from "react";
import { subscribeDesktopRuntime } from "../../shared/runtime/desktopRuntime";
import type { Subscription } from "../../shared/types";
import { createSubscriptionsService, type SubscriptionInput } from "./subscriptionsService";

type SubscriptionsState = {
  data: Subscription[];
  loading: boolean;
  saving: boolean;
  refreshing: boolean;
  error: string | null;
};

export function useSubscriptionsState() {
  const [state, setState] = useState<SubscriptionsState>({
    data: [],
    loading: true,
    saving: false,
    refreshing: false,
    error: null
  });

  useEffect(() => {
    let active = true;
    const service = createSubscriptionsService();

    async function load() {
      try {
        const data = await service.getSubscriptions();
        if (!active) {
          return;
        }

        setState({
          data,
          loading: false,
          saving: false,
          refreshing: false,
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
          refreshing: false,
          error: error instanceof Error ? error.message : "Failed to load subscriptions"
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

  async function create(input: SubscriptionInput) {
    const service = createSubscriptionsService();
    setState((current) => ({ ...current, saving: true, error: null }));
    try {
      const created = await service.createSubscription(input);
      setState((current) => ({
        data: [created, ...current.data],
        loading: false,
        saving: false,
        refreshing: false,
        error: null
      }));
    } catch (error) {
      setState((current) => ({
        ...current,
        saving: false,
        error: error instanceof Error ? error.message : "Failed to create subscription"
      }));
    }
  }

  async function update(id: number, input: SubscriptionInput) {
    const service = createSubscriptionsService();
    setState((current) => ({ ...current, saving: true, error: null }));
    try {
      const updated = await service.updateSubscription(id, input);
      setState((current) => ({
        data: current.data.map((subscription) => (subscription.id === id ? updated : subscription)),
        loading: false,
        saving: false,
        refreshing: false,
        error: null
      }));
    } catch (error) {
      setState((current) => ({
        ...current,
        saving: false,
        error: error instanceof Error ? error.message : "Failed to update subscription"
      }));
    }
  }

  async function remove(id: number) {
    const service = createSubscriptionsService();
    setState((current) => ({ ...current, saving: true, error: null }));
    try {
      await service.deleteSubscription(id);
      setState((current) => ({
        data: current.data.filter((subscription) => subscription.id !== id),
        loading: false,
        saving: false,
        refreshing: false,
        error: null
      }));
    } catch (error) {
      setState((current) => ({
        ...current,
        saving: false,
        error: error instanceof Error ? error.message : "Failed to delete subscription"
      }));
    }
  }

  async function refresh(id: number) {
    const service = createSubscriptionsService();
    setState((current) => ({ ...current, refreshing: true, error: null }));
    try {
      const updated = await service.refreshSubscription(id);
      setState((current) => ({
        data: current.data.map((subscription) => (subscription.id === id ? updated : subscription)),
        loading: false,
        saving: false,
        refreshing: false,
        error: null
      }));
    } catch (error) {
      setState((current) => ({
        ...current,
        refreshing: false,
        error: error instanceof Error ? error.message : "Failed to refresh subscription"
      }));
    }
  }

  async function refreshAll() {
    const service = createSubscriptionsService();
    setState((current) => ({ ...current, refreshing: true, error: null }));
    try {
      const refreshed = await service.refreshSubscriptions();
      const refreshedById = new Map(refreshed.map((subscription) => [subscription.id, subscription]));
      setState((current) => ({
        data: current.data.map((subscription) => refreshedById.get(subscription.id) ?? subscription),
        loading: false,
        saving: false,
        refreshing: false,
        error: null
      }));
    } catch (error) {
      setState((current) => ({
        ...current,
        refreshing: false,
        error: error instanceof Error ? error.message : "Failed to refresh subscriptions"
      }));
    }
  }

  return {
    ...state,
    create,
    update,
    remove,
    refresh,
    refreshAll
  };
}
