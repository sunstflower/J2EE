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

export function queryInventories(params) {
  return apiRequest(`/api/inventories${toQueryString(params)}`);
}

export function getInventory(id) {
  return apiRequest(`/api/inventories/${id}`);
}

export function inboundInventory(payload) {
  return apiRequest("/api/inventories/inbound", {
    method: "POST",
    body: payload,
  });
}

export function outboundInventory(payload) {
  return apiRequest("/api/inventories/outbound", {
    method: "POST",
    body: payload,
  });
}

export function checkInventory(payload) {
  return apiRequest("/api/inventories/check", {
    method: "POST",
    body: payload,
  });
}

export function queryInventoryRecords(params) {
  return apiRequest(`/api/inventories/records${toQueryString(params)}`);
}
