import { LocalApiClient } from "../../shared/api/localApiClient";
import type { ApiSuccessResponse, ImportedProxyNode } from "../../shared/types";

export type ProxiesService = {
  getImportedNodes: () => Promise<ImportedProxyNode[]>;
};

class LocalApiProxiesService implements ProxiesService {
  constructor(private readonly client: LocalApiClient) {}

  async getImportedNodes(): Promise<ImportedProxyNode[]> {
    const response = await this.client.get<ApiSuccessResponse<ImportedProxyNode[]>>("/proxies/nodes");
    return response.data;
  }
}

export function createProxiesService(): ProxiesService {
  return new LocalApiProxiesService(new LocalApiClient());
}
