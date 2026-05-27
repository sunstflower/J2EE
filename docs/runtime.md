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

### State

Recommended location:

```text
<runtime-root>/clash-meta/state/
```

Examples:

- current PID marker
- last generated config checksum
- last known core launch metadata

## Failure Handling Expectations

1. If Spring Boot fails to start, Electron should surface a backend startup error
2. If the port or token handoff fails, React should not be initialized against guessed defaults
3. If Clash.Meta path resolution fails, Spring Boot should not try fallback guesses silently
4. If runtime directory creation fails, startup should stop with a clear error

## Open Implementation Choices

These are still implementation details, not architecture blockers:

1. Whether Spring Boot readiness is detected from structured stdout, a health endpoint, or both
2. Whether Electron uses preload plus IPC for renderer access from the first iteration
3. Exact log rolling policy and retention counts
