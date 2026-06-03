import { loadCurrentUser } from "../auth";

const DEFAULT_API_BASE_URL = "http://localhost:8080";

function getApiBaseUrl() {
  return import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL;
}

function buildHeaders(body, headers = {}) {
  const currentUser = loadCurrentUser();
  const nextHeaders = new Headers(headers);

  if (body !== undefined && !nextHeaders.has("Content-Type")) {
    nextHeaders.set("Content-Type", "application/json");
  }

  if (currentUser) {
    nextHeaders.set("X-User-Id", String(currentUser.userId));
    nextHeaders.set("X-User-Name", encodeURIComponent(currentUser.userName));
    nextHeaders.set("X-User-Role", currentUser.role);
  }

  return nextHeaders;
}

async function parseResponse(response) {
  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    throw new Error(payload?.message || "请求失败");
  }

  if (!payload || payload.code !== 0) {
    throw new Error(payload?.message || "接口返回异常");
  }

  return payload.data;
}

export async function apiRequest(path, options = {}) {
  const { body, headers, ...rest } = options;
  const response = await fetch(`${getApiBaseUrl()}${path}`, {
    ...rest,
    headers: buildHeaders(body, headers),
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  return parseResponse(response);
}
