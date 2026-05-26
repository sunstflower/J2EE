import { LocalApiClient } from "../../shared/api/localApiClient";
import type { ApiSuccessResponse, DashboardState } from "../../shared/types";
import { getMockDashboardState } from "./mockDashboardService";

export type DashboardService = {
  getDashboardState: () => Promise<DashboardState>;
};

class LocalApiDashboardService implements DashboardService {
  constructor(private readonly client: LocalApiClient) {}

  async getDashboardState(): Promise<DashboardState> {
    const response = await this.client.get<ApiSuccessResponse<DashboardState>>("/dashboard");
    return response.data;
  }
}

class MockDashboardService implements DashboardService {
  async getDashboardState(): Promise<DashboardState> {
    return getMockDashboardState();
  }
}

export function createDashboardService(): DashboardService {
  if (import.meta.env.VITE_USE_REAL_LOCAL_API === "true") {
    return new LocalApiDashboardService(new LocalApiClient());
  }

  return new MockDashboardService();
}
