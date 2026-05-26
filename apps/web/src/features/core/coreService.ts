import { LocalApiClient } from "../../shared/api/localApiClient";
import type { ApiSuccessResponse, CoreStatus } from "../../shared/types";

export type CoreService = {
  getStatus: () => Promise<CoreStatus>;
  start: () => Promise<CoreStatus>;
  stop: () => Promise<CoreStatus>;
  reload: () => Promise<CoreStatus>;
};

class LocalApiCoreService implements CoreService {
  constructor(private readonly client: LocalApiClient) {}

  async getStatus(): Promise<CoreStatus> {
    const response = await this.client.get<ApiSuccessResponse<CoreStatus>>("/core");
    return response.data;
  }

  async start(): Promise<CoreStatus> {
    const response = await this.client.post<ApiSuccessResponse<CoreStatus>>("/core/start");
    return response.data;
  }

  async stop(): Promise<CoreStatus> {
    const response = await this.client.post<ApiSuccessResponse<CoreStatus>>("/core/stop");
    return response.data;
  }

  async reload(): Promise<CoreStatus> {
    const response = await this.client.post<ApiSuccessResponse<CoreStatus>>("/core/reload");
    return response.data;
  }
}

export function createCoreService(): CoreService {
  return new LocalApiCoreService(new LocalApiClient());
}
