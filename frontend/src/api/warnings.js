import { apiRequest } from "./client";

function toQueryString(params) {
  const query = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== "" && value !== undefined && value !== null) {
      query.set(key, String(value));
    }
  });

  const queryString = query.toString();
  return queryString ? `?${queryString}` : "";
}

export function queryLowStockWarnings(params) {
  return apiRequest(`/api/warnings/low-stock${toQueryString(params)}`);
}

export function queryExpiryWarnings(params) {
  return apiRequest(`/api/warnings/expiry${toQueryString(params)}`);
}
