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

- full Clash.Meta config generation from subscriptions and selections
- signed and notarized desktop distribution
- release update channel integration
- log retention and diagnostics workflows

Already initialized:

- Electron launches Spring Boot in development and captures a machine-readable ready line
- Backend binds to random localhost port and validates a session token on `/api/v1/**` except health
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
- Runtime root and Clash.Meta path can be injected explicitly through environment variables

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

Run the desktop scaffold:

```bash
npm run dev:desktop
```

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

1. Add signed packaged builds that embed `Clash.Meta` and tray assets into the distributable app bundle
2. Introduce actual Clash.Meta config generation and profile rendering instead of lifecycle-only backend wiring
3. Add subscription fetch, parse, and persistence flows with refresh scheduling
4. Expand end-to-end verification for backend startup, token-authenticated API access, and macOS system proxy transitions
