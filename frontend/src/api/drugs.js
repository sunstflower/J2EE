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

export function queryDrugs(params) {
  return apiRequest(`/api/drugs${toQueryString(params)}`);
}

export function createDrug(payload) {
  return apiRequest("/api/drugs", {
    method: "POST",
    body: payload,
  });
}

export function deleteDrug(id) {
  return apiRequest(`/api/drugs/${id}`, {
    method: "DELETE",
  });
}
