import { LocalApiClient } from "../../shared/api/localApiClient";
import type { DashboardState } from "../../shared/types";
import { getMockDashboardState } from "./mockDashboardService";

export type DashboardService = {
  getDashboardState: () => Promise<DashboardState>;
};

class LocalApiDashboardService implements DashboardService {
  constructor(private readonly client: LocalApiClient) {}

  async getDashboardState(): Promise<DashboardState> {
    return this.client.get<DashboardState>("/dashboard");
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
