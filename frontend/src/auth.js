const STORAGE_KEY = "drug-management-current-user";
let memoryCurrentUser = null;

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

export const DEMO_USERS = [
  { userId: 100, userName: "王医生", role: "DOCTOR" },
  { userId: 200, userName: "张药师", role: "PHARMACIST" },
];

export function loadCurrentUser() {
  const storage = getStorage();
  if (!storage) {
    return memoryCurrentUser;
  }
  const raw = storage.getItem(STORAGE_KEY);
  return raw ? JSON.parse(raw) : null;
}

export function saveCurrentUser(user) {
  const storage = getStorage();
  if (!storage) {
    memoryCurrentUser = user;
    return;
  }
  storage.setItem(STORAGE_KEY, JSON.stringify(user));
}

export function clearCurrentUser() {
  const storage = getStorage();
  if (!storage) {
    memoryCurrentUser = null;
    return;
  }
  storage.removeItem(STORAGE_KEY);
}
