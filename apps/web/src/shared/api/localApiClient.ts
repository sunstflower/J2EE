import { getDesktopRuntime, hasDesktopRuntimeBridge, hasResolvedDesktopLocalApi, waitForDesktopLocalApiRuntime } from "../runtime/desktopRuntime";

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
  constructor(options?: { baseUrl?: string; sessionToken?: string }) {
    this.baseUrlOverride = options?.baseUrl;
    this.sessionTokenOverride = options?.sessionToken;
  }

  private readonly baseUrlOverride?: string;
  private readonly sessionTokenOverride?: string;

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
    const requestContext = await this.resolveRequestContext();
    const response = await fetch(`${requestContext.baseUrl}${path}`, {
      method: options.method ?? "GET",
      headers: {
        "Content-Type": "application/json",
        ...(requestContext.sessionToken ? { Authorization: `Bearer ${requestContext.sessionToken}` } : {}),
        ...options.headers
      },
      body: options.body ? JSON.stringify(options.body) : undefined
    });

    if (!response.ok) {
      throw new Error(`Local API request failed: ${response.status}`);
    }

    return response.json() as Promise<T>;
  }

  private async resolveRequestContext() {
    if (this.baseUrlOverride) {
      return {
        baseUrl: this.baseUrlOverride,
        sessionToken: this.sessionTokenOverride
      };
    }

    const currentRuntime = getDesktopRuntime();
    if (hasResolvedDesktopLocalApi(currentRuntime)) {
      return {
        baseUrl: currentRuntime!.localApiBaseUrl!,
        sessionToken: currentRuntime!.localApiSessionToken!
      };
    }

    if (hasDesktopRuntimeBridge()) {
      const runtime = await waitForDesktopLocalApiRuntime();
      if (hasResolvedDesktopLocalApi(runtime)) {
        return {
          baseUrl: runtime!.localApiBaseUrl!,
          sessionToken: runtime!.localApiSessionToken!
        };
      }
    }

    return {
      baseUrl: resolveBaseUrl(),
      sessionToken: resolveSessionToken()
    };
  }
}
