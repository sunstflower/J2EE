import { LocalApiClient } from "../../shared/api/localApiClient";
import type { ApiSuccessResponse, RuntimeErrors, RuntimeLogs, RuntimeSummary } from "../../shared/types";

export type RuntimeService = {
  getRuntimeSummary: () => Promise<RuntimeSummary>;
  getRuntimeLogs: (limit?: number) => Promise<RuntimeLogs>;
  getRuntimeErrors: () => Promise<RuntimeErrors>;
};

class LocalApiRuntimeService implements RuntimeService {
  constructor(private readonly client: LocalApiClient) {}

  async getRuntimeSummary(): Promise<RuntimeSummary> {
    const response = await this.client.get<ApiSuccessResponse<RuntimeSummary>>("/runtime");
    return response.data;
  }

  async getRuntimeLogs(limit = 20): Promise<RuntimeLogs> {
    const response = await this.client.get<ApiSuccessResponse<RuntimeLogs>>(`/runtime/logs?limit=${limit}`);
    return response.data;
  }

  async getRuntimeErrors(): Promise<RuntimeErrors> {
    const response = await this.client.get<ApiSuccessResponse<RuntimeErrors>>("/runtime/errors");
    return response.data;
  }
}

export function createRuntimeService(): RuntimeService {
  return new LocalApiRuntimeService(new LocalApiClient());
}
