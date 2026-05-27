import { LocalApiClient } from "../../shared/api/localApiClient";
import type { ApiSuccessResponse, SystemProxyStatus } from "../../shared/types";

export type SystemProxyService = {
  getStatus: () => Promise<SystemProxyStatus>;
  update: (payload: {
    enabled: boolean;
    scope?: "ALL_ENABLED" | "SELECTED";
    services?: string[];
    acceptRecommendedServices?: boolean;
  }) => Promise<SystemProxyStatus>;
};

class LocalApiSystemProxyService implements SystemProxyService {
  constructor(private readonly client: LocalApiClient) {}

  async getStatus(): Promise<SystemProxyStatus> {
    const response = await this.client.get<ApiSuccessResponse<SystemProxyStatus>>("/system-proxy");
    return response.data;
  }

  async update(payload: {
    enabled: boolean;
    scope?: "ALL_ENABLED" | "SELECTED";
    services?: string[];
    acceptRecommendedServices?: boolean;
  }): Promise<SystemProxyStatus> {
    const response = await this.client.put<ApiSuccessResponse<SystemProxyStatus>>("/system-proxy", payload);
    return response.data;
  }
}

export function createSystemProxyService(): SystemProxyService {
  return new LocalApiSystemProxyService(new LocalApiClient());
}
