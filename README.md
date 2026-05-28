# mac-proxy-client

A macOS local proxy client built with a desktop shell, a local Spring Boot API, and a React management UI.

This repository is initialized for architecture scheme 2:

- Desktop shell: Electron
- Frontend: React
- Local backend: Spring Boot
- Persistence: MyBatis + SQLite
- Proxy dataplane: bundled Clash.Meta core

This repository now includes formal scaffolds for the desktop shell, web UI, and local API. The baseline local control plane is initialized, including desktop-to-backend bootstrap, session-token-protected local APIs, SQLite persistence for early settings and subscriptions, and a first real Clash.Meta lifecycle service boundary.

## Locked Decisions

The following design decisions are now fixed for the first implementation:

- Proxy core: `Clash.Meta`
- Proxy mode: `system proxy` only
- Core distribution: bundled with the desktop application
- Core owner: `services/local-api` managed by Spring Boot
- Backend layering: `Controller / Service / DAO`
- Persistence: `SQLite` with `MyBatis`
- Local API access: random localhost port + session token

## Goals

- Provide a macOS-first proxy client similar in product shape to Clash clients
- Keep Java responsible for control-plane logic, not proxy protocol implementation
- Expose local RESTful APIs for UI orchestration and runtime control
- Support system proxy mode, subscription management, policy routing, and runtime management around Clash.Meta

## Non-Goals For The Current Stage

- No full Clash.Meta config modeling yet
- No production-ready UI polish yet
- No signed production release yet
- No notarization pipeline yet
- No TUN mode work in the first implementation

## Proposed Architecture

The application runs as three cooperating local processes:

1. Desktop shell process
   - Owns tray/menu bar, notifications, window lifecycle, startup integration
   - Launches and monitors the local API service
   - Hosts the React UI

2. Local API process
   - Spring Boot service bound to localhost only
   - Uses a random local port and a session token for desktop-to-backend access
   - Provides RESTful APIs for config management, runtime state, subscription sync, and core lifecycle
   - Uses MyBatis with SQLite for local persistence

3. Proxy core process
   - Bundled `Clash.Meta` executable, not reimplemented in Java
   - Managed only by the local API service
   - Receives generated runtime config from the local API
   - Handles real traffic forwarding

## Repository Layout

```text
mac-proxy-client/
  apps/
    desktop/        # Electron shell scaffold
    web/            # React + Vite UI scaffold
  services/
    local-api/      # Spring Boot + MyBatis local backend scaffold
  docs/
    api-draft.md
    architecture.md
    decisions.md
    roadmap.md
    runtime.md
  scripts/          # Development helper scripts
```

## Scaffold Status

Current scaffolds:

- `apps/desktop`: Electron main process, preload bridge, development-time Spring Boot launcher, renderer fallback page
- `apps/web`: React 19 + Vite + TypeScript + Tailwind CSS v4 shell with real local API service modules
- `services/local-api`: Spring Boot 3 + MyBatis + SQLite local API with controller/service/dao baseline and early runtime endpoints

Not implemented yet:

- signed and notarized desktop distribution
- release update channel integration
- log rotation and retention policies
- Electron development-mode preload bridge hardening and troubleshooting guidance

Already initialized:

- Electron launches Spring Boot in development and captures a machine-readable ready line
- Backend binds to random localhost port and validates a session token on `/api/v1/**` except health
- Development-mode local API access from the Vite renderer is now allowed through explicit CORS mapping for `http://127.0.0.1:5173`, and session-token interception now lets `OPTIONS` preflight requests pass cleanly
- SQLite persistence is active for settings and subscriptions
- Core endpoints exist for status, start, stop, and reload
- System proxy endpoints now integrate with macOS `networksetup`, with runtime snapshot-based restore on disable
- System proxy targeting supports either all enabled network services or an explicit selected-service list
- Default targeting now starts in selected-service mode and prefers likely primary interfaces using macOS service order plus interface heuristics
- Active non-VPN network services are now preferred ahead of passive or virtual services when building the default recommendation set
- The backend now tracks confirmed recommended service sets so the UI can prompt when the preferred target set changes
- The overview panel now surfaces pending target-set recommendation changes with a direct accept action
- Electron can now surface a local desktop notification when a pending recommendation change is first observed
- Electron tray now shows system proxy status and can trigger one-click recommendation acceptance
- Electron tray title and tooltip now reflect current system proxy status and pending recommendation state
- Electron tray icon now switches visual state for `On`, `Off`, `Pending`, and idle conditions
- Desktop tray icon loading now prefers packaged state-specific PNG assets, using `macos-template-*` files on macOS and default colored assets elsewhere, then falls back to generated vector icons when assets are missing
- The repository now defines a canonical bundled-core vendor/import flow into `runtime-assets/clash-meta/bin/clash-meta`
- Electron Builder packaging scaffold now collects the web build, Spring Boot jar, and bundled Clash.Meta binary into desktop `extraResources`
- Clash.Meta runtime ports are now allocated dynamically at start time, and system proxy targeting follows the current mixed port instead of assuming `7890`
- Before Clash.Meta starts, its mixed/controller ports are intentionally unset, so backend and frontend logic must treat `0` as "not allocated yet" instead of falling back to historical defaults
- Generated Clash.Meta proxy entries are now de-duplicated by effective proxy name at config-render time, so overlapping subscription content does not produce invalid duplicate-name configs during `start` or `reload`
- Core runtime ownership now persists a runtime-root-scoped pid marker and cleans up matching stale Clash.Meta processes on `start`, `stop`, and `reload`, so backend restarts can recover and reassert control instead of leaving orphaned core instances behind
- Runtime root and Clash.Meta path can be injected explicitly through environment variables
- Subscription refresh, imported proxy node persistence, proxy-group selection, generated Clash.Meta config output, and local development core startup have now all been verified together against the current Electron + Spring Boot + React scaffold
- Overview now includes a minimal diagnostics surface backed by `/api/v1/runtime`, `/api/v1/runtime/logs`, and `/api/v1/runtime/errors`
- Electron point testing has now exercised Overview core `Start` / `Reload` / `Stop`, Subscriptions refresh flows, and Proxies group selection against a live local-api session

## Planned Module Boundaries

### apps/desktop

Responsibilities:

- Launch local backend on app startup
- Bundle and ship Clash.Meta with the app package
- Provide tray/menu integration
- Surface notifications and simple runtime status
- Coordinate window open/close behavior
- Handle desktop-only capabilities that should not live in React
- Provide renderer bootstrap context for backend port and session token

### apps/web

Responsibilities:

- Settings UI
- Proxy node and group management UI
- Subscription management UI
- Runtime status and logs UI
- Frontend state management and API consumption only

### services/local-api

Responsibilities:

- Expose localhost-only RESTful APIs
- Require a session token for UI-facing local API calls
- Manage runtime configuration generation
- Control Clash.Meta process lifecycle
- Persist application config and subscription data
- Offer health and diagnostics endpoints

Required package layering later:

- `controller`
- `service`
- `dao`

Supporting packages can be added later when the project needs them, but the baseline service design should stay aligned with the `Controller / Service / DAO` structure.

## Initial Development Phases

### Phase 0: Initialization

- Establish architecture documents
- Create repository skeleton
- Define contributor rules
- Initialize formal scaffolds for Electron, React, and Spring Boot

### Phase 1: Local API MVP

- Health endpoint
- App config persistence
- Subscription entity and CRUD
- Clash.Meta process abstraction
- Runtime config generation for Clash.Meta

### Phase 2: Desktop Shell MVP

- Launch local API from Electron
- Bundle Clash.Meta and expose its installation path to the local API
- Basic tray integration
- Backend connectivity status
- Open local React UI

### Phase 3: Client Features

- System proxy mode
- Subscription refresh flow
- Node selection and policy groups
- Runtime logs panel
- Core lifecycle diagnostics

### Phase 4: Packaging And Security

- App packaging strategy
- Core bundle validation and versioning
- Permission and entitlement strategy
- Secure credential storage via Keychain

## Key Design Decisions

### Why Electron

Electron is selected for the first implementation because it lowers desktop integration friction and keeps React reusable. If footprint becomes a priority later, the shell can be reevaluated against Tauri.

### Why Spring Boot

Spring Boot provides a stable control-plane service layer, testable REST boundaries, and a clean place to isolate system integration logic without forcing desktop-specific concerns into the UI. It is also the only process responsible for managing Clash.Meta.

### Why SQLite

This is a local-first desktop application. SQLite keeps deployment simple and matches the product shape better than introducing a client-server database.

### Why External Proxy Core

The proxy protocol dataplane is a separate concern with significant complexity and security risk. This project integrates a mature core instead of reimplementing transport logic in Java.

### Why Clash.Meta

Clash.Meta is selected because the product goal is a Clash-like macOS client, and Clash.Meta aligns well with the expected configuration model, rule groups, and operator workflow.

### Why System Proxy Only

The first implementation is intentionally limited to `system proxy` mode. This keeps the initial networking model practical on macOS and avoids early expansion into TUN-specific privilege, entitlement, and lifecycle complexity.

### Why Bundle The Core

Bundling Clash.Meta with the installation package gives the project a predictable runtime surface, avoids first-run download dependencies, and keeps version compatibility under repository control.

## macOS-Specific Constraints To Validate Early

- System proxy toggle behavior and rollback
- Localhost port allocation and conflict detection
- Session token generation and secure in-memory handoff
- Process relaunch and crash recovery
- Bundled core executable permissions and path resolution
- App signing, entitlements, and notarization path

## Documentation Index

- [docs/architecture.md](./docs/architecture.md)
- [docs/api-draft.md](./docs/api-draft.md)
- [docs/decisions.md](./docs/decisions.md)
- [docs/roadmap.md](./docs/roadmap.md)
- [docs/runtime.md](./docs/runtime.md)
- [AGENTS.md](./AGENTS.md)

## Local Development

Install workspace dependencies:

```bash
npm install
```

Vendor the pinned bundled Clash.Meta executable into the repository asset path:

```bash
npm run vendor:core
```

Or import an explicit local binary into the same repository path:

```bash
npm run import:core -- /absolute/path/to/clash-meta
```

Run the web scaffold:

```bash
npm run dev:web
```

The Vite dev server is pinned to `127.0.0.1:5173` with `strictPort: true`. If that port is already occupied, stop the conflicting process instead of letting Vite drift to a different port.

If you need to attach the web UI to an already-running local API for debugging or Electron point testing, you can inject the local API connection explicitly:

```bash
VITE_LOCAL_API_BASE_URL=http://127.0.0.1:<port>/api/v1 \
VITE_LOCAL_API_SESSION_TOKEN=<token> \
npm run dev:web
```

Run the desktop scaffold:

```bash
npm run dev:desktop
```

The verified local development path is:

1. keep the renderer on `127.0.0.1:5173`
2. run `npm run dev:desktop`
3. let Electron launch Spring Boot automatically
4. import the maintained sample subscription fixture at `file:///Users/sunsetflower/myJobs/Java/mac-proxy-client/.tmp-core-verify/sample.yaml` or copy it to another absolute `file://` path if needed
5. refresh that subscription and confirm imported nodes in the Proxies view
6. change a proxy group selection in the Proxies view and confirm it persists
7. start, reload, and stop the core from Overview
8. inspect Overview diagnostics plus `.runtime/clash-meta/config/config.yaml` and `.runtime/clash-meta/logs/clash-meta.log` if behavior does not match the UI

This verified path proves local orchestration and generated-config startup. It does not yet prove that arbitrary remote subscription payloads contain valid live credentials.

Development-mode local API fetches from the Vite renderer now depend on explicit local CORS allowance for `http://127.0.0.1:5173`, and the backend currently allows unauthenticated `OPTIONS` preflight requests so the renderer can still use the same session-protected local API during development.

When attaching Electron to a Vite dev server, pass the actual renderer URL explicitly if Vite selected a different port:

```bash
MAC_PROXY_RENDERER_URL=http://127.0.0.1:5174 npm run dev:web --workspace @mac-proxy-client/desktop
```

That override is now mainly for intentionally custom renderer URLs. The default development path expects the standard `5173` port to stay stable.

## Current Usage Surface

The current scaffold is already usable for the following local workflows:

1. Add or refresh `file://`, `http://`, or `https://` subscriptions into the local SQLite store
2. Import subscription nodes and render them into generated Clash.Meta runtime config
3. Select proxy-group targets in the Proxies view and persist the selection
4. Start, reload, and stop the bundled Clash.Meta core from the Overview panel
5. Toggle system proxy state through the backend-managed macOS `networksetup` integration
6. Inspect runtime summary, recent runtime errors, and the core log tail from Overview diagnostics

## Local Verification Checklist

For a final local smoke test before packaging or review, verify the following in order:

1. `npm run build:web`
2. `cd services/local-api && mvn test`
3. `npm run dev:web`
4. `npm run dev:desktop`
5. In Electron:
   - refresh at least one subscription
   - confirm imported nodes appear in Proxies
   - change one proxy-group selection
   - execute `Start`, `Reload`, and `Stop` from Overview
   - confirm diagnostics shows runtime errors or core log lines when available

## Known Development Limitation

Electron development-mode point testing exposed an open bootstrap issue: in some runs the renderer does not receive the preload-provided `window.desktopRuntime` bridge even though the Electron window and local API are both up. When that happens, keep the local API session usable by starting Vite with explicit `VITE_LOCAL_API_BASE_URL` and `VITE_LOCAL_API_SESSION_TOKEN` overrides as shown above. This limitation affects developer bootstrap convenience, but it did not block the verified business flows listed in this document.

Build desktop distribution prerequisites:

```bash
npm run build:web
npm run build:local-api
npm run prepare:desktop-bundle
```

Build an unpacked desktop app:

```bash
npm run build:desktop
```

Build a macOS zip distributable:

```bash
npm run dist:desktop
```

Optional development environment overrides:

```bash
APP_CORE_CLASH_META_PATH=/absolute/path/to/clash-meta \
APP_RUNTIME_ROOT=/absolute/path/to/runtime \
npm run dev:desktop
```

Run the local API scaffold:

```bash
cd services/local-api
mvn spring-boot:run
```

When running the local API directly for debugging, prefer absolute paths for `APP_RUNTIME_ROOT` and `APP_CORE_CLASH_META_PATH` so they do not drift with the service working directory.

Verify the backend scaffold:

```bash
cd services/local-api
mvn test
```

## Development Asset Convention

The repository now reserves this canonical bundled core asset path:

```text
runtime-assets/clash-meta/bin/clash-meta
```

If that file exists, Electron uses it as the default `Clash.Meta` path during local development. The same file is also the required source for packaged desktop bundling. If it is missing, the backend remains in `NOT_CONFIGURED` or `MISSING_BINARY` state until a path is supplied, and desktop bundle preparation will stop.

The pinned vendor version lives in:

```text
runtime-assets/clash-meta/version.txt
```

The maintained repository sample for local subscription import verification lives in:

```text
.tmp-core-verify/sample.yaml
```

For packaged desktop builds, Electron now expects the following generated bundle staging layout before `electron-builder` runs:

```text
apps/desktop/.bundle/
  web/        # copied from apps/web/dist
  backend/    # contains local-api.jar
  core/       # contains bundled clash-meta executable
```

Prepare that staging area with:

```bash
./scripts/prepare_desktop_bundle.sh
```

Vendor or import the bundled core into that repository path before preparing the desktop bundle:

```bash
npm run vendor:core
```

```bash
npm run import:core -- /absolute/path/to/clash-meta
```

Tray assets follow a dual-track convention under `apps/desktop/assets/tray`:

```text
default PNGs:
tray-idle.png
tray-idle@2x.png
tray-on.png
tray-on@2x.png
tray-off.png
tray-off@2x.png
tray-pending.png
tray-pending@2x.png

macOS template PNGs:
macos-template-tray-idle.png
macos-template-tray-idle@2x.png
macos-template-tray-on.png
macos-template-tray-on@2x.png
macos-template-tray-off.png
macos-template-tray-off@2x.png
macos-template-tray-pending.png
macos-template-tray-pending@2x.png
```

Source SVG files live in `apps/desktop/assets/tray/svg/default` and `apps/desktop/assets/tray/svg/macos-template`. Regenerate all PNG outputs with:

```bash
./scripts/generate_tray_assets.sh
```

## Next Recommended Work

1. Isolate and fix the Electron development-mode `window.desktopRuntime` preload bridge gap
2. Add signed packaged builds that embed `Clash.Meta` and tray assets into the distributable app bundle
3. Add log rotation, exported diagnostics, and retention controls around runtime logs
4. Expand end-to-end verification for packaged desktop startup and macOS system proxy transitions
