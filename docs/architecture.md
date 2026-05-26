# Architecture

## Current Direction

This project follows a local multi-process desktop architecture:

- Electron desktop shell
- React frontend
- Spring Boot local API
- External proxy core executable
- SQLite local persistence

## Process Model

### Desktop Shell

- Entry point for the end user
- Starts and monitors the local API service
- Renders the React application
- Provides tray, notification, and window management

### Local API

- Exposes localhost-only REST endpoints
- Manages application configuration
- Tracks runtime state
- Integrates with the external proxy core

### Proxy Core

- Runs as a child or managed local process
- Consumes generated runtime configuration
- Produces logs and lifecycle state for the API to inspect

## Control Plane vs Data Plane

- Control plane: Electron + React + Spring Boot
- Data plane: external proxy core

This separation should remain strict.

## Storage Strategy

- SQLite for structured local persistence
- Generated files for proxy runtime config
- macOS Keychain for secrets later

## Open Questions

1. Whether to use Electron main process only or preload plus renderer IPC split
2. Whether backend lifecycle should be owned entirely by Electron or partially self-managed
3. Whether the proxy core should be bundled, downloaded, or user-provided
4. How to handle TUN-related privileges on macOS
