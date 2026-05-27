# AGENTS.md

This document defines how human contributors and coding agents should work in this repository.

## Scope

This repository hosts a macOS local proxy client with the following shape:

- `apps/desktop`: Electron shell
- `apps/web`: React UI
- `services/local-api`: Spring Boot local backend

The repository has moved past documentation-only initialization. Prefer maintaining the documented architecture while extending the real scaffold in small, reviewable slices.

The following decisions are fixed unless explicitly revised by the project owner:

- Proxy core is `Clash.Meta`
- First implementation supports `system proxy` only
- Clash.Meta is bundled with the installation package
- `services/local-api` is the sole owner of the core lifecycle
- Backend baseline layering is `Controller / Service / DAO`
- Local API access uses a random localhost port plus a session token

## Working Rules

1. Do not implement proxy protocols in this repository.
2. Treat the Java backend as the control plane, not the traffic plane.
3. Keep all local API endpoints bound to localhost unless there is an explicit security-reviewed reason not to.
4. Prefer SQLite for local persistence unless a concrete requirement invalidates it.
5. Do not add cloud dependencies by default. This project is local-first.
6. Any macOS-specific privileged behavior must be documented before it is implemented.
7. Do not introduce TUN mode work unless the project owner explicitly reopens that decision.
8. Do not add alternate proxy core integrations unless the project owner explicitly reopens that decision.

## Architecture Guardrails

### Desktop Shell

- Owns tray/menu bar, notifications, app lifecycle, and startup behavior
- May launch and monitor the local backend
- Ships the bundled Clash.Meta executable as an application asset
- Must not absorb backend domain logic

### Web UI

- Talks only to the local API
- Must not directly manage OS-level side effects
- Should remain portable enough to be hosted by another shell if needed

### Local API

- Owns config persistence, runtime orchestration, and integration with the bundled Clash.Meta core
- Should generate runtime config artifacts for Clash.Meta
- Must isolate command execution and process management behind explicit abstractions
- Is the only process allowed to start, stop, reload, or inspect Clash.Meta
- Must validate a session token on UI-facing local API access
- Should keep generated runtime files under the configured runtime root instead of mutating packaged assets

## Documentation Requirements

Update documentation when changing any of the following:

- Repository structure
- Process model
- Module responsibilities
- External dependencies
- Security assumptions
- Startup or development workflow

At minimum, keep these files aligned:

- `README.md`
- `AGENTS.md`
- `docs/architecture.md`
- `docs/decisions.md`
- `docs/roadmap.md`

## Implementation Guidelines

### General

- Favor small, reviewable changes
- Keep boundaries explicit between desktop, frontend, and backend
- Avoid hidden coupling through ad hoc scripts or undocumented ports

### Backend

- Use `Controller / Service / DAO` as the baseline layering when code begins
- Keep MyBatis mappers and SQL close to DAO concerns
- Separate runtime state from persisted configuration
- Put Clash.Meta process management behind services instead of controllers

### Frontend

- Keep UI state and API client concerns separated
- Avoid embedding OS workflow assumptions deeply into components
- Model runtime state from backend responses instead of re-deriving it in the UI
- Do not cache Electron-provided local API base URLs or session tokens at module load time; desktop runtime may be injected after the first render, so local API clients must resolve runtime context at request time or reload when runtime becomes available

### Desktop

- Desktop startup should fail clearly if the backend is unavailable
- Process management must include health checks and predictable shutdown
- Do not silently restart privileged operations without surfacing status
- Do not move Clash.Meta lifecycle management into Electron
- Electron may resolve and pass the runtime root and Clash.Meta binary path, but must not absorb runtime orchestration logic

## Security Baseline

1. Never commit credentials, tokens, or subscription secrets
2. Plan for Keychain-backed secret storage on macOS
3. Expose only localhost services during development
4. Document every shell command or OS integration that affects networking
5. Treat bundled executable integrity and version compatibility as a documented concern
6. Do not replace random port plus session token with a weaker local API access pattern without explicit approval

## Current Scaffold Reality

The current repository state already includes:

- Electron development bootstrap for Spring Boot
- random-port readiness handoff via `LOCAL_API_READY port=<port>`
- Bearer-token protection on local API endpoints except health
- SQLite-backed settings and subscriptions slices
- early Clash.Meta lifecycle endpoints with runtime-root-backed generated config
- `system-proxy` endpoints backed by macOS `networksetup`, with restore snapshots stored under the runtime root
- a pinned bundled-core vendor flow via `npm run vendor:core` into `runtime-assets/clash-meta/bin/clash-meta`
- Electron Builder packaging that embeds the web build, Spring Boot jar, and bundled Clash.Meta binary into the desktop app
- packaged desktop runtime storage now lives under Electron `userData` and must remain writable outside packaged assets
- the Vite renderer dev server is expected to stay on `127.0.0.1:5173` with a strict port check, so desktop development should fail loudly on port conflicts instead of silently drifting

Contributors should extend these behaviors instead of reintroducing mock startup assumptions in new code or docs.

## Definition Of Ready

Before implementing a feature, confirm:

1. Which process owns the feature
2. What API boundary it crosses
3. Whether it needs privileged macOS behavior
4. Whether it changes persisted data, runtime state, or both
5. Which documents need updates
6. Whether it changes Clash.Meta startup, config generation, or packaging assumptions

## Definition Of Done

A change is not complete unless:

1. The relevant architecture and workflow docs remain accurate
2. New modules or directories are reflected in `README.md`
3. Cross-process behavior is described when applicable
4. Security-sensitive assumptions are made explicit
