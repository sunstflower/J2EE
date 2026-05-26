import { useEffect, useState } from "react";
import type { AppSettings } from "../../shared/types";
import { createSettingsService } from "./settingsService";

type SettingsState = {
  data: AppSettings | null;
  loading: boolean;
  saving: boolean;
  error: string | null;
};

const service = createSettingsService();

export function useSettingsState() {
  const [state, setState] = useState<SettingsState>({
    data: null,
    loading: true,
    saving: false,
    error: null
  });

  useEffect(() => {
    let active = true;

    async function load() {
      try {
        const data = await service.getSettings();

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
          data: null,
          loading: false,
          saving: false,
          error: error instanceof Error ? error.message : "Failed to load settings"
        });
      }
    }

    load();

    return () => {
      active = false;
    };
  }, []);

  async function save(data: AppSettings) {
    setState((current) => ({
      ...current,
      saving: true,
      error: null
    }));

    try {
      const updated = await service.updateSettings(data);

      setState({
        data: updated,
        loading: false,
        saving: false,
        error: null
      });
    } catch (error) {
      setState((current) => ({
        ...current,
        saving: false,
        error: error instanceof Error ? error.message : "Failed to save settings"
      }));
    }
  }

  return {
    ...state,
    save
  };
}
