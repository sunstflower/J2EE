# mac-proxy-client

A macOS local proxy client built with a desktop shell, a local Spring Boot API, and a React management UI.

This repository is initialized for architecture scheme 2:

- Desktop shell: Electron
- Frontend: React
- Local backend: Spring Boot
- Persistence: MyBatis + SQLite
- Proxy dataplane: external core process such as Clash.Meta or sing-box

This phase is documentation-first. The repository is intentionally initialized without business implementation so the architecture, boundaries, and contributor workflow are stable before coding starts.

## Goals

- Provide a macOS-first proxy client similar in product shape to Clash clients
- Keep Java responsible for control-plane logic, not proxy protocol implementation
- Expose local RESTful APIs for UI orchestration and runtime control
- Support future system proxy mode, TUN mode, subscription management, and policy routing

## Non-Goals For Initialization

- No proxy core integration yet
- No production-ready UI yet
- No packaged desktop app yet
- No full build scripts yet

## Proposed Architecture

The application runs as three cooperating local processes:

1. Desktop shell process
   - Owns tray/menu bar, notifications, window lifecycle, startup integration
   - Launches and monitors the local API service
   - Hosts the React UI

2. Local API process
   - Spring Boot service bound to localhost only
   - Provides RESTful APIs for config management, runtime state, subscription sync, and core lifecycle
   - Uses MyBatis with SQLite for local persistence

3. Proxy core process
   - External executable, not reimplemented in Java
   - Receives generated runtime config from the local API
   - Handles real traffic forwarding

## Repository Layout

```text
mac-proxy-client/
  apps/
    desktop/        # Electron shell
    web/            # React UI
  services/
    local-api/      # Spring Boot + MyBatis local backend
  docs/
    architecture.md
    roadmap.md
  scripts/          # Development helper scripts
```

## Planned Module Boundaries

### apps/desktop

Responsibilities:

- Launch local backend on app startup
- Provide tray/menu integration
- Surface notifications and simple runtime status
- Coordinate window open/close behavior
- Handle desktop-only capabilities that should not live in React

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
- Manage runtime configuration generation
- Control proxy core process lifecycle
- Persist application config and subscription data
- Offer health and diagnostics endpoints

Suggested package layering later:

- `api`
- `application`
- `domain`
- `infrastructure`
- `integration`

## Initial Development Phases

### Phase 0: Initialization

- Establish architecture documents
- Create repository skeleton
- Define contributor rules

### Phase 1: Local API MVP

- Health endpoint
- App config persistence
- Subscription entity and CRUD
- Proxy core process abstraction

### Phase 2: Desktop Shell MVP

- Launch local API from Electron
- Basic tray integration
- Backend connectivity status
- Open local React UI

### Phase 3: Client Features

- System proxy mode
- Subscription refresh flow
- Node selection and policy groups
- Runtime logs panel

### Phase 4: Advanced Networking

- TUN mode validation on macOS
- Permission and entitlement strategy
- Secure credential storage via Keychain

## Key Design Decisions

### Why Electron

Electron is selected for the first implementation because it lowers desktop integration friction and keeps React reusable. If footprint becomes a priority later, the shell can be reevaluated against Tauri.

### Why Spring Boot

Spring Boot provides a stable control-plane service layer, testable REST boundaries, and a clean place to isolate system integration logic without forcing desktop-specific concerns into the UI.

### Why SQLite

This is a local-first desktop application. SQLite keeps deployment simple and matches the product shape better than introducing a client-server database.

### Why External Proxy Core

The proxy protocol dataplane is a separate concern with significant complexity and security risk. This project should integrate a mature core instead of reimplementing transport logic in Java.

## macOS-Specific Constraints To Validate Early

- System proxy toggle behavior and rollback
- Localhost port allocation and conflict detection
- Process relaunch and crash recovery
- TUN and network extension requirements
- App signing, entitlements, and notarization path

## Documentation Index

- [docs/architecture.md](./docs/architecture.md)
- [docs/roadmap.md](./docs/roadmap.md)
- [AGENTS.md](./AGENTS.md)

## Next Recommended Work

1. Lock the architecture vocabulary and runtime boundaries
2. Define REST resources and request/response conventions
3. Create minimal build scaffolding for Electron, React, and Spring Boot
4. Add a local development bootstrap flow
