import { LocalApiClient } from "../../shared/api/localApiClient";
import type { ApiSuccessResponse, RuntimeSummary } from "../../shared/types";

export type RuntimeService = {
  getRuntimeSummary: () => Promise<RuntimeSummary>;
};

class LocalApiRuntimeService implements RuntimeService {
  constructor(private readonly client: LocalApiClient) {}

  async getRuntimeSummary(): Promise<RuntimeSummary> {
    const response = await this.client.get<ApiSuccessResponse<RuntimeSummary>>("/runtime");
    return response.data;
  }
}

export function createRuntimeService(): RuntimeService {
  return new LocalApiRuntimeService(new LocalApiClient());
}
