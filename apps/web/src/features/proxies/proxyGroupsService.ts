import { LocalApiClient } from "../../shared/api/localApiClient";
import type { ApiSuccessResponse, ProxyGroupSelection } from "../../shared/types";

export type ProxyGroupsService = {
  getGroups: () => Promise<ProxyGroupSelection[]>;
  updateSelection: (groupName: string, selectedNodeName: string) => Promise<ProxyGroupSelection>;
};

class LocalApiProxyGroupsService implements ProxyGroupsService {
  constructor(private readonly client: LocalApiClient) {}

  async getGroups(): Promise<ProxyGroupSelection[]> {
    const response = await this.client.get<ApiSuccessResponse<ProxyGroupSelection[]>>("/proxies/groups");
    return response.data;
  }

  async updateSelection(groupName: string, selectedNodeName: string): Promise<ProxyGroupSelection> {
    const response = await this.client.put<ApiSuccessResponse<ProxyGroupSelection>>(
      `/proxies/groups/${encodeURIComponent(groupName)}/selection`,
      { selectedNodeName }
    );
    return response.data;
  }
}

export function createProxyGroupsService(): ProxyGroupsService {
  return new LocalApiProxyGroupsService(new LocalApiClient());
}
