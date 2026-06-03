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

export function queryPrescriptions(params) {
  return apiRequest(`/api/prescriptions${toQueryString(params)}`);
}

export function getPrescription(id) {
  return apiRequest(`/api/prescriptions/${id}`);
}

export function createPrescription(payload) {
  return apiRequest("/api/prescriptions", {
    method: "POST",
    body: payload,
  });
}

export function approvePrescriptionByDoctor(id, payload) {
  return apiRequest(`/api/prescriptions/${id}/doctor-approve`, {
    method: "POST",
    body: payload,
  });
}

export function submitPrescription(id) {
  return apiRequest(`/api/prescriptions/${id}/submit`, {
    method: "POST",
  });
}

export function auditPrescription(id, payload) {
  return apiRequest(`/api/prescriptions/${id}/audit`, {
    method: "POST",
    body: payload,
  });
}

export function dispensePrescription(id, payload) {
  return apiRequest(`/api/prescriptions/${id}/dispense`, {
    method: "POST",
    body: payload,
  });
}

export function cancelPrescription(id) {
  return apiRequest(`/api/prescriptions/${id}/cancel`, {
    method: "POST",
  });
}
