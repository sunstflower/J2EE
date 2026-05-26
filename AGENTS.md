# AGENTS.md

This document defines how human contributors and coding agents should work in this repository.

## Scope

This repository hosts a macOS local proxy client with the following shape:

- `apps/desktop`: Electron shell
- `apps/web`: React UI
- `services/local-api`: Spring Boot local backend

The repository is currently in documentation-first initialization. Prefer architectural clarity over premature implementation.

## Working Rules

1. Do not implement proxy protocols in this repository.
2. Treat the Java backend as the control plane, not the traffic plane.
3. Keep all local API endpoints bound to localhost unless there is an explicit security-reviewed reason not to.
4. Prefer SQLite for local persistence unless a concrete requirement invalidates it.
5. Do not add cloud dependencies by default. This project is local-first.
6. Any macOS-specific privileged behavior must be documented before it is implemented.

## Architecture Guardrails

### Desktop Shell

- Owns tray/menu bar, notifications, app lifecycle, and startup behavior
- May launch and monitor the local backend
- Must not absorb backend domain logic

### Web UI

- Talks only to the local API
- Must not directly manage OS-level side effects
- Should remain portable enough to be hosted by another shell if needed

### Local API

- Owns config persistence, runtime orchestration, and integration with the proxy core
- Should generate runtime config artifacts for the external core
- Must isolate command execution and process management behind explicit abstractions

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
- `docs/roadmap.md`

## Implementation Guidelines

### General

- Favor small, reviewable changes
- Keep boundaries explicit between desktop, frontend, and backend
- Avoid hidden coupling through ad hoc scripts or undocumented ports

### Backend

- Use layered packages when code begins:
  - `api`
  - `application`
  - `domain`
  - `infrastructure`
  - `integration`
- Keep MyBatis mappers and SQL close to infrastructure concerns
- Separate runtime state from persisted configuration

### Frontend

- Keep UI state and API client concerns separated
- Avoid embedding OS workflow assumptions deeply into components
- Model runtime state from backend responses instead of re-deriving it in the UI

### Desktop

- Desktop startup should fail clearly if the backend is unavailable
- Process management must include health checks and predictable shutdown
- Do not silently restart privileged operations without surfacing status

## Security Baseline

1. Never commit credentials, tokens, or subscription secrets
2. Plan for Keychain-backed secret storage on macOS
3. Expose only localhost services during development
4. Document every shell command or OS integration that affects networking

## Definition Of Ready

Before implementing a feature, confirm:

1. Which process owns the feature
2. What API boundary it crosses
3. Whether it needs privileged macOS behavior
4. Whether it changes persisted data, runtime state, or both
5. Which documents need updates

## Definition Of Done

A change is not complete unless:

1. The relevant architecture and workflow docs remain accurate
2. New modules or directories are reflected in `README.md`
3. Cross-process behavior is described when applicable
4. Security-sensitive assumptions are made explicit
