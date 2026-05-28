# Architecture

## Current Direction

This project follows a local multi-process desktop architecture:

- Electron desktop shell
- React frontend
- Spring Boot local API
- Bundled Clash.Meta core executable
- SQLite local persistence

## Process Model

### Desktop Shell

- Entry point for the end user
- Starts and monitors the local API service
- Ships the bundled Clash.Meta binary within the desktop application
- Renders the React application
- Provides tray, notification, and window management
- Supplies the renderer with the local API base URL and session token through preload instead of exposing backend discovery logic to React

### Local API

- Exposes localhost-only REST endpoints
- Binds to a random localhost port at startup
- Requires a session token for desktop-to-backend access
- Manages application configuration
- Tracks runtime state
- Integrates with and manages Clash.Meta
- Owns core startup, shutdown, reload, health checks, and log capture
- Exposes runtime diagnostics for the renderer, including runtime summary, recent surfaced errors, and core log tail

### Proxy Core

- Uses Clash.Meta in the first implementation
- Runs as a managed local process
- Consumes generated runtime configuration
- Produces logs and lifecycle state for the API to inspect

## Control Plane vs Data Plane

- Control plane: Electron + React + Spring Boot
- Data plane: Clash.Meta

This separation should remain strict.

## Backend Layering

The Spring Boot service follows a baseline layered structure:

- `controller`: REST endpoints and request/response handling
- `service`: business orchestration, runtime control, and config generation
- `dao`: persistence access through MyBatis

Additional support packages may be introduced later, but this layered model is the default for the project.

## Storage Strategy

- SQLite for structured local persistence
- Generated files for Clash.Meta runtime config
- macOS Keychain for secrets later

## Runtime Defaults

The project uses the following default runtime layout unless later adjusted for packaging constraints:

1. Bundled Clash.Meta binary
   - Packaged app: shipped inside the desktop application resources
   - Local development: stored under a repository-managed runtime asset path

2. Generated runtime files
   - Stored in an application runtime directory outside the bundled assets
   - Includes generated Clash.Meta config, PID state, and rolling logs

3. Desktop-to-backend communication
   - Spring Boot binds to a random localhost port
   - Electron receives the chosen port and session token during backend startup
   - React only talks through the desktop-managed connection context

## Fixed Decisions

1. Proxy core is `Clash.Meta`
2. Only `system proxy` mode is in scope for the first implementation
3. Clash.Meta is bundled with the desktop application
4. Spring Boot local API is the sole manager of Clash.Meta
5. Local API access uses a random localhost port plus a session token

## Remaining Open Questions

1. How the bundled Clash.Meta binary is versioned and upgraded
2. What packaging layout best fits macOS signing and notarization
3. Why the Electron development-mode renderer occasionally fails to observe the expected preload-provided `window.desktopRuntime` bridge during point testing
