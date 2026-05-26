# mac-proxy-client

A macOS local proxy client built with a desktop shell, a local Spring Boot API, and a React management UI.

This repository is initialized for architecture scheme 2:

- Desktop shell: Electron
- Frontend: React
- Local backend: Spring Boot
- Persistence: MyBatis + SQLite
- Proxy dataplane: bundled Clash.Meta core

This repository now includes formal scaffolds for the desktop shell, web UI, and local API. Business features are still intentionally minimal so the architecture, boundaries, and contributor workflow remain stable before deeper implementation starts.

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

## Non-Goals For Initialization

- No proxy core integration yet
- No production-ready UI yet
- No packaged desktop app yet
- No full build scripts yet
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

- `apps/desktop`: Electron main process, preload entry, renderer fallback page
- `apps/web`: React 19 + Vite + TypeScript + Tailwind CSS v4
- `services/local-api`: Spring Boot 3 + MyBatis + SQLite starter setup

Not implemented yet:

- Clash.Meta process integration
- session token validation
- subscription persistence
- desktop-to-backend bootstrap wiring
- packaged desktop distribution

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

Run the web scaffold:

```bash
npm run dev:web
```

Run the desktop scaffold:

```bash
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

## Next Recommended Work

1. Wire Electron startup to the Spring Boot process and runtime parameter passing
2. Implement session token validation in the local API
3. Add the first persisted settings and subscription tables
4. Introduce Clash.Meta binary discovery and runtime config generation
