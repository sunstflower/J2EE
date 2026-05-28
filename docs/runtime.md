# Runtime

## Purpose

This document defines the default runtime layout and process bootstrap flow for local development and packaged macOS runs.

## Runtime Principles

1. Bundled application assets remain read-only at runtime
2. Generated config, logs, and temporary state are written to a separate runtime directory
3. Electron owns backend bootstrap
4. Spring Boot owns Clash.Meta lifecycle
5. React never discovers backend connection details by itself

## Runtime Layout

### Packaged App

Recommended layout:

1. Desktop application bundle
   - Contains Electron application resources
   - Contains the bundled Clash.Meta executable in application resources

2. Writable runtime directory
   - Stores generated Clash.Meta config
   - Stores runtime PID or state markers if needed
   - Stores rotating log files
   - Stores temporary exported diagnostics if needed

Recommended macOS runtime root:

```text
~/Library/Application Support/mac-proxy-client/
```

Recommended subdirectories:

```text
~/Library/Application Support/mac-proxy-client/
  runtime/
    clash-meta/
      config/
      logs/
      state/
    local-api/
```

### Local Development

Recommended development layout:

1. Bundled-like binary asset location inside the repository
   - Keeps development close to packaged behavior

2. Writable runtime directory outside immutable source assets

Recommended development paths:

```text
<repo>/runtime-assets/clash-meta/
<repo>/.runtime/clash-meta/
```

Suggested split:

```text
<repo>/runtime-assets/clash-meta/
  bin/

<repo>/.runtime/
  clash-meta/
    config/
    logs/
    state/
  local-api/
```

## Clash.Meta Asset Strategy

### Packaged App

- Electron ships the Clash.Meta binary as an application resource
- Spring Boot receives the resolved absolute binary path from Electron during startup
- Spring Boot does not guess packaged asset locations on its own

### Local Development

- The repository keeps the Clash.Meta binary under a dedicated runtime asset folder
- Electron resolves the binary path from that development asset folder
- Spring Boot receives the resolved absolute binary path from Electron during startup

## Desktop-To-Backend Bootstrap

The startup flow should be:

1. Electron starts
2. Electron resolves:
   - Spring Boot launch command
   - Clash.Meta absolute binary path
   - Writable runtime root path
3. Electron generates a session token for the backend session
4. Electron launches Spring Boot with:
   - random port enabled
   - session token
   - Clash.Meta path
   - runtime root path
5. Spring Boot binds to a random localhost port
6. Spring Boot prints or exposes a machine-readable ready signal
7. Electron captures the selected port
8. Electron creates the renderer connection context
9. React uses only the connection context supplied by Electron

Current development scaffold:

- Spring Boot emits `LOCAL_API_READY port=<port>` on `ApplicationReadyEvent`
- Electron parses that stdout line as the source of truth for the bound port
- Electron now passes:
  - `APP_SESSION_TOKEN`
  - `APP_RUNTIME_ROOT`
  - `APP_CORE_CLASH_META_PATH`
- Vite-based renderer development currently runs at `http://127.0.0.1:5173`, so local-api CORS explicitly allows that origin
- Session-token interception now permits unauthenticated `OPTIONS` preflight requests so development-mode browser fetches can reach the same session-protected local API
- The intended development path is still: Electron supplies local API runtime context to the renderer through preload, and React consumes that desktop-managed connection context instead of discovering connection details itself

## Recommended Launch Parameters

Electron should pass explicit runtime values to Spring Boot instead of relying on hardcoded conventions.

Suggested parameters:

- `app.core.clash-meta.path`
- `app.runtime.root`
- `app.session.token`
- `server.port=0`

These names are placeholders for now and can be refined when the actual scaffold is created.

## Session Token Handling

Rules:

1. Generate a fresh token per desktop app launch
2. Keep the token in memory only
3. Do not persist the token to SQLite
4. Do not write the token to long-lived logs
5. Require the token on UI-facing local API requests

Recommended transport options for later implementation:

- `Authorization: Bearer <token>`
- `X-Session-Token: <token>`

Either is acceptable. The project should pick one and keep it consistent.

Current scaffold status:

- Backend interceptor expects `Authorization: Bearer <token>`
- Development token defaults from `APP_SESSION_TOKEN`
- Runtime root defaults from `APP_RUNTIME_ROOT`, falling back to `./.runtime`
- Frontend real-API mode reads:
  - `VITE_LOCAL_API_BASE_URL`
  - `VITE_LOCAL_API_SESSION_TOKEN`
- Electron development shell now starts Spring Boot, generates a session token, parses the bound port from startup logs, and exposes both values to the renderer preload bridge
- Electron development shell also resolves the development Clash.Meta path from `runtime-assets/clash-meta/bin/clash-meta` when no explicit override is provided
- During recent development-mode Electron point testing, business flows still worked against a live local API, but one run did not expose `window.desktopRuntime` in the renderer; until that bootstrap defect is isolated, explicit `VITE_LOCAL_API_BASE_URL` and `VITE_LOCAL_API_SESSION_TOKEN` overrides remain the fallback debug path

## Local API Binding

Rules:

1. Bind only to `127.0.0.1`
2. Use random port allocation
3. Fail startup clearly if the app cannot determine the actual bound port
4. Never assume a fixed port in the renderer layer

## Runtime Files

### Generated Config

Recommended file:

```text
<runtime-root>/clash-meta/config/config.yaml
```

Rules:

1. Spring Boot owns config generation
2. Generated files may be replaced atomically
3. The packaged binary directory must remain untouched

Current scaffold status:

- `CoreManagerService` currently writes a minimal generated config to `clash-meta/config/config.yaml`
- `Clash.Meta` is started with explicit `-f <config-file> -d <runtime-root>/clash-meta`
- the local SQLite file currently lives at `<runtime-root>/local-api/local-api.db`
- current subscription/proxy integration has now been verified end-to-end in development: Electron starts Spring Boot, Spring Boot imports subscription nodes into SQLite, generated Clash.Meta config is written under the runtime root, and the bundled Clash.Meta binary can start successfully from that generated file
- a repository fixture at `.tmp-core-verify/sample.yaml` is now maintained as the canonical local development sample for file-based subscription import checks
- overlapping subscription content is now normalized at config-render time by de-duplicating generated proxy entries on effective Clash.Meta proxy name, preventing duplicate-name startup failures during `core/start` and `core/reload`
- runtime diagnostics are now available through:
  - `GET /api/v1/runtime`
  - `GET /api/v1/runtime/logs`
  - `GET /api/v1/runtime/errors`
- the Overview panel consumes those endpoints to show runtime summary, recent surfaced errors, and the core log tail during local verification

### Logs

Recommended location:

```text
<runtime-root>/clash-meta/logs/
```

Rules:

1. Runtime logs should be separated from source and packaged assets
2. Rolling log strategy is preferred over unbounded append-only files
3. Sensitive values should be filtered before persistence when practical

Current scaffold status:

- the first lifecycle implementation appends core process output to `clash-meta/logs/clash-meta.log`
- log rotation is not implemented yet
- local development verification should inspect `clash-meta/logs/clash-meta.log` together with `clash-meta/config/config.yaml` when confirming whether imported subscription nodes actually reached the running core

### State

Recommended location:

```text
<runtime-root>/clash-meta/state/
```

Examples:

- current PID marker
- last generated config checksum
- last known core launch metadata
- system proxy restore snapshot for previously active network service proxy settings
- persisted system proxy service scope and selected service list in SQLite settings
- backend heuristics plus macOS network service order for recommended primary services, used as the selected-mode fallback set
- active non-VPN interface detection from `scutil --nwi`, used to bias recommendation toward the current live path
- persisted confirmed recommendation snapshot for selected-mode service targeting in SQLite settings

Current scaffold status:

- `CoreManagerService` now writes a runtime-root-scoped pid marker at `clash-meta/state/core.pid` for the currently managed Clash.Meta process
- `core/start` now cleans up stale matching Clash.Meta processes under the same runtime root before launching a new managed instance
- `core/stop` removes both the active managed process and the persisted pid marker so later backend restarts do not inherit stale runtime ownership data

## Failure Handling Expectations

1. If Spring Boot fails to start, Electron should surface a backend startup error
2. If the port or token handoff fails, React should not be initialized against guessed defaults
3. If Clash.Meta path resolution fails, Spring Boot should not try fallback guesses silently
4. If runtime directory creation fails, startup should stop with a clear error

## Verified Development Flow

The currently verified local development sequence is:

1. Vendor or import the bundled Clash.Meta binary into `runtime-assets/clash-meta/bin/clash-meta`
2. Start the Vite renderer on `127.0.0.1:5173`
3. Start `npm run dev:desktop`
4. Let Electron launch Spring Boot and inject:
   - `APP_SESSION_TOKEN`
   - `APP_RUNTIME_ROOT`
   - `APP_CORE_CLASH_META_PATH`
5. Use the subscriptions UI or local API to import `.tmp-core-verify/sample.yaml` through a `file://` subscription
6. Verify that:
   - imported rows appear in `<runtime-root>/local-api/local-api.db`
   - generated nodes appear in `<runtime-root>/clash-meta/config/config.yaml`
   - Clash.Meta startup logs appear in `<runtime-root>/clash-meta/logs/clash-meta.log`
   - `clash-meta/state/core.pid` appears while core is running and disappears after `core/stop`
   - Overview diagnostics can read runtime summary, runtime errors, and the core log tail
7. In the Electron window, verify:
   - subscription `Refresh` and `Refresh enabled`
   - proxy-group selection persistence
   - core `Start`, `Reload`, and `Stop`

Current limitation:
- log rotation is still not implemented
- duplicate subscription node names are tolerated only at generated-config level; imported SQLite rows are still stored per subscription and may legitimately repeat names across sources
- Electron development-mode renderer bootstrap may still need explicit Vite local-api env overrides if the preload bridge does not attach during a given run
