import { apiRequest, postPublic } from "./client";

export function login(payload) {
  return postPublic("/api/auth/login", payload);
}

export function register(payload) {
  return postPublic("/api/auth/register", payload);
}

export function getCurrentUserProfile() {
  return apiRequest("/api/auth/me");
}
