import { LocalApiClient } from "../../shared/api/localApiClient";
import type { ApiSuccessResponse, AppSettings } from "../../shared/types";

export type SettingsService = {
  getSettings: () => Promise<AppSettings>;
  updateSettings: (settings: AppSettings) => Promise<AppSettings>;
};

class LocalApiSettingsService implements SettingsService {
  constructor(private readonly client: LocalApiClient) {}

  async getSettings(): Promise<AppSettings> {
    const response = await this.client.get<ApiSuccessResponse<AppSettings>>("/settings");
    return response.data;
  }

  async updateSettings(settings: AppSettings): Promise<AppSettings> {
    const response = await this.client.put<ApiSuccessResponse<AppSettings>>("/settings", settings);
    return response.data;
  }
}

export function createSettingsService(): SettingsService {
  return new LocalApiSettingsService(new LocalApiClient());
}
