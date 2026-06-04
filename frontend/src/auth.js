const STORAGE_KEY = "drug-management-current-user";
const TOKEN_STORAGE_KEY = "drug-management-access-token";
let memoryCurrentUser = null;
let memoryToken = null;

function getStorage() {
  const storage = window.localStorage;
  if (
    storage &&
    typeof storage.getItem === "function" &&
    typeof storage.setItem === "function" &&
    typeof storage.removeItem === "function"
  ) {
    return storage;
  }
  return null;
}

export function inferRoleFromUserId(userId) {
  const normalizedId = String(userId).trim();
  if (normalizedId.startsWith("1")) {
    return "PHARMACIST";
  }
  if (normalizedId.startsWith("2")) {
    return "DOCTOR";
  }
  throw new Error("用户号必须以 1 或 2 开头");
}

export function loadCurrentUser() {
  const storage = getStorage();
  if (!storage) {
    return memoryCurrentUser;
  }
  const raw = storage.getItem(STORAGE_KEY);
  return raw ? JSON.parse(raw) : null;
}

export function loadAccessToken() {
  const storage = getStorage();
  if (!storage) {
    return memoryToken;
  }
  return storage.getItem(TOKEN_STORAGE_KEY);
}

export function saveAuthSession({ user, token }) {
  const storage = getStorage();
  if (!storage) {
    memoryCurrentUser = user;
    memoryToken = token;
    return;
  }
  storage.setItem(STORAGE_KEY, JSON.stringify(user));
  storage.setItem(TOKEN_STORAGE_KEY, token);
}

export function clearCurrentUser() {
  const storage = getStorage();
  if (!storage) {
    memoryCurrentUser = null;
    memoryToken = null;
    return;
  }
  storage.removeItem(STORAGE_KEY);
  storage.removeItem(TOKEN_STORAGE_KEY);
}
