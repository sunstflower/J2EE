# Decisions

## Fixed Project Decisions

This document records design choices that are currently locked for the project.

## D-001 Proxy Core

- Status: accepted
- Decision: use `Clash.Meta` as the only proxy core for the first implementation

Reasoning:

- The target product shape is intentionally close to Clash-style clients
- Clash.Meta fits the expected rule group and configuration workflow
- A single core reduces configuration and maintenance drift during the first implementation

## D-002 Proxy Mode Scope

- Status: accepted
- Decision: support `system proxy` mode only in the first implementation

Reasoning:

- It is the shortest path to a usable macOS client
- It avoids early TUN-specific entitlement and privilege complexity
- It keeps the first runtime model narrow enough to stabilize the rest of the stack

## D-003 Core Distribution

- Status: accepted
- Decision: ship Clash.Meta inside the desktop application package

Reasoning:

- The runtime environment stays predictable
- Core version compatibility can be controlled with the application release
- First-run experience does not depend on downloads or user-provided executables

## D-004 Core Lifecycle Ownership

- Status: accepted
- Decision: `services/local-api` is the sole owner of Clash.Meta lifecycle management

Reasoning:

- Core startup, shutdown, reload, config generation, and log handling belong with backend orchestration
- Electron should remain focused on desktop shell concerns
- A single owner avoids split-brain runtime state

## D-005 Backend Layering

- Status: accepted
- Decision: use `Controller / Service / DAO` as the baseline backend structure

Reasoning:

- It matches the stated implementation preference
- It is easy to understand and maintain in the early project stage
- It fits cleanly with REST endpoints, MyBatis persistence, and process orchestration

## D-006 Persistence

- Status: accepted
- Decision: use `SQLite` with `MyBatis`

Reasoning:

- The application is local-first
- Deployment remains simple
- It avoids introducing an unnecessary separate database service

## D-007 Local API Access

- Status: accepted
- Decision: use a random localhost port plus a session token for local API access

Reasoning:

- It avoids relying on a fixed port that may conflict on user machines
- It improves the local attack surface compared with unauthenticated localhost endpoints
- It fits naturally with desktop-managed backend startup

## D-008 Runtime File Layout

- Status: accepted
- Decision: keep the bundled Clash.Meta binary inside packaged desktop resources, and store generated runtime config and logs in a separate writable application runtime directory

Reasoning:

- Bundled assets should remain immutable at runtime
- Generated config, logs, and state files need a writable location
- This layout maps cleanly onto packaging, upgrades, and troubleshooting

## D-009 Desktop-To-Backend Bootstrap

- Status: accepted
- Decision: Electron starts Spring Boot, obtains the chosen localhost port and session token, and passes that connection context to the renderer layer

Reasoning:

- It keeps backend discovery under desktop-shell control
- It avoids teaching the React layer how to discover backend internals on its own
- It supports random port allocation without sacrificing UI simplicity

## Current Follow-Up Questions

These are not yet locked, but they should be resolved before implementation expands:

1. How the bundled Clash.Meta binary is versioned and upgraded
2. Whether Electron should use preload plus renderer IPC from the first scaffold
3. Which exact macOS runtime directories are used in development and packaged runs
