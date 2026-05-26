type RequestOptions = {
  method?: string;
  body?: unknown;
  headers?: Record<string, string>;
};

export class LocalApiClient {
  private readonly baseUrl: string;
  private readonly sessionToken?: string;

  constructor(options?: { baseUrl?: string; sessionToken?: string }) {
    this.baseUrl = options?.baseUrl ?? "/api/v1";
    this.sessionToken = options?.sessionToken;
  }

  async get<T>(path: string): Promise<T> {
    return this.request<T>(path, { method: "GET" });
  }

  async post<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>(path, { method: "POST", body });
  }

  async put<T>(path: string, body?: unknown): Promise<T> {
    return this.request<T>(path, { method: "PUT", body });
  }

  private async request<T>(path: string, options: RequestOptions): Promise<T> {
    const response = await fetch(`${this.baseUrl}${path}`, {
      method: options.method ?? "GET",
      headers: {
        "Content-Type": "application/json",
        ...(this.sessionToken ? { Authorization: `Bearer ${this.sessionToken}` } : {}),
        ...options.headers
      },
      body: options.body ? JSON.stringify(options.body) : undefined
    });

    if (!response.ok) {
      throw new Error(`Local API request failed: ${response.status}`);
    }

    return response.json() as Promise<T>;
  }
}
