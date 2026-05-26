import { LocalApiClient } from "../../shared/api/localApiClient";
import type { ApiSuccessResponse, Subscription } from "../../shared/types";

export type SubscriptionInput = {
  name: string;
  sourceUrl: string;
  enabled: boolean;
};

export type SubscriptionsService = {
  getSubscriptions: () => Promise<Subscription[]>;
  createSubscription: (input: SubscriptionInput) => Promise<Subscription>;
  updateSubscription: (id: number, input: SubscriptionInput) => Promise<Subscription>;
  deleteSubscription: (id: number) => Promise<void>;
};

class LocalApiSubscriptionsService implements SubscriptionsService {
  constructor(private readonly client: LocalApiClient) {}

  async getSubscriptions(): Promise<Subscription[]> {
    const response = await this.client.get<ApiSuccessResponse<Subscription[]>>("/subscriptions");
    return response.data;
  }

  async createSubscription(input: SubscriptionInput): Promise<Subscription> {
    const response = await this.client.post<ApiSuccessResponse<Subscription>>("/subscriptions", input);
    return response.data;
  }

  async updateSubscription(id: number, input: SubscriptionInput): Promise<Subscription> {
    const response = await this.client.put<ApiSuccessResponse<Subscription>>(`/subscriptions/${id}`, input);
    return response.data;
  }

  async deleteSubscription(id: number): Promise<void> {
    await this.client.delete(`/subscriptions/${id}`);
  }
}

export function createSubscriptionsService(): SubscriptionsService {
  return new LocalApiSubscriptionsService(new LocalApiClient());
}
