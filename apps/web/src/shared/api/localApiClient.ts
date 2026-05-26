import { getDesktopRuntime } from "../runtime/desktopRuntime";

type RequestOptions = {
  method?: string;
  body?: unknown;
  headers?: Record<string, string>;
};

function resolveBaseUrl() {
  const desktopRuntime = getDesktopRuntime();
  return desktopRuntime?.localApiBaseUrl || import.meta.env.VITE_LOCAL_API_BASE_URL || "/api/v1";
}

function resolveSessionToken() {
  const desktopRuntime = getDesktopRuntime();
  return desktopRuntime?.localApiSessionToken || import.meta.env.VITE_LOCAL_API_SESSION_TOKEN;
}

export class LocalApiClient {
  private readonly baseUrl: string;
  private readonly sessionToken?: string;

  constructor(options?: { baseUrl?: string; sessionToken?: string }) {
    this.baseUrl = options?.baseUrl ?? resolveBaseUrl();
    this.sessionToken = options?.sessionToken ?? resolveSessionToken();
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

  async delete<T>(path: string): Promise<T> {
    return this.request<T>(path, { method: "DELETE" });
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
