/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_USE_REAL_LOCAL_API?: string;
  readonly VITE_LOCAL_API_BASE_URL?: string;
  readonly VITE_LOCAL_API_SESSION_TOKEN?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
