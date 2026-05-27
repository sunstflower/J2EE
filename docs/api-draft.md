# API Draft

## Purpose

This document defines the first draft of the local REST API exposed by `services/local-api`.

The API is:

- localhost only
- bound to a random port
- protected with a session token
- consumed by the Electron-hosted React frontend

## Conventions

### Base Rules

1. All responses use JSON unless explicitly documented otherwise
2. All UI-facing requests require the session token
3. API versioning starts with `/api/v1`
4. Controllers should remain thin and delegate to services

### Suggested Headers

- `Authorization: Bearer <session-token>`
- `Content-Type: application/json`

### Suggested Response Envelope

Success:

```json
{
  "success": true,
  "data": {}
}
```

Failure:

```json
{
  "success": false,
  "error": {
    "code": "INTERNAL_ERROR",
    "message": "Human-readable message"
  }
}
```

This envelope is a draft. The exact shape can still be adjusted before coding begins.

## Resource Areas

The first implementation should keep the API grouped around these areas:

1. App health and session
2. Application settings
3. Subscriptions
4. Proxy nodes and groups
5. Runtime and diagnostics
6. System proxy control
7. Clash.Meta lifecycle

## 1. App Health And Session

### `GET /api/v1/health`

Purpose:

- Verify the local API is running
- Return basic runtime readiness information

Suggested response fields:

- application status
- Clash.Meta manager readiness
- database readiness
- current mode summary

### `GET /api/v1/session`

Purpose:

- Return non-secret session metadata for the renderer
- Confirm authenticated connectivity

Suggested response fields:

- app version
- backend version
- current runtime mode
- current authenticated timestamp

## 2. Application Settings

### `GET /api/v1/settings`

Purpose:

- Read persisted application settings relevant to the UI

Suggested fields:

- mixed port or proxy ports if enabled later
- system proxy preferences
- startup preferences
- log preferences

### `PUT /api/v1/settings`

Purpose:

- Update persisted application settings

Notes:

- Validate settings in the controller layer
- Apply side effects through services only

## 3. Subscriptions

### `GET /api/v1/subscriptions`

Purpose:

- List configured subscriptions

### `POST /api/v1/subscriptions`

Purpose:

- Create a subscription entry

Suggested request fields:

- name
- source URL
- enabled

### `PUT /api/v1/subscriptions/{subscriptionId}`

Purpose:

- Update an existing subscription

### `DELETE /api/v1/subscriptions/{subscriptionId}`

Purpose:

- Remove a subscription

### `POST /api/v1/subscriptions/{subscriptionId}/refresh`

Purpose:

- Refresh one subscription and update imported nodes

### `POST /api/v1/subscriptions/refresh`

Purpose:

- Refresh all enabled subscriptions

## 4. Proxy Nodes And Groups

### `GET /api/v1/proxies/nodes`

Purpose:

- List imported proxy nodes

Suggested filters for later:

- source subscription
- enabled status
- keyword

### `GET /api/v1/proxies/groups`

Purpose:

- List proxy groups derived from current runtime model

### `PUT /api/v1/proxies/groups/{groupName}/selection`

Purpose:

- Change the selected node for a group

Suggested request fields:

- selected node name or identifier

## 5. Runtime And Diagnostics

### `GET /api/v1/runtime`

Purpose:

- Return current runtime summary

Suggested response fields:

- backend status
- Clash.Meta process status
- current active config metadata
- current selected mode
- last refresh timestamps

### `GET /api/v1/runtime/logs`

Purpose:

- Return recent runtime log lines or paged log entries

### `GET /api/v1/runtime/errors`

Purpose:

- Return recent actionable runtime errors

## 6. System Proxy Control

### `GET /api/v1/system-proxy`

Purpose:

- Return current system proxy state known to the application

Current scaffold fields:

- `enabled`
- `managed`
- `mode`
- `statusLabel`
- `capability`
- `scope`
- `selectedServices`
- `confirmedServices`
- `targetHost`
- `targetPort`
- `serviceCount`
- `services`
- `activeServices`
- `recommendationPending`
- `lastAction`
- `lastError`

### `PUT /api/v1/system-proxy`

Purpose:

- Enable or disable system proxy behavior

Suggested request fields:

- enabled
- scope
- services

Current scaffold behavior:

- starts Clash.Meta before enabling system proxy if needed
- applies Web, Secure Web, and SOCKS proxy settings through macOS `networksetup`
- supports `ALL_ENABLED` and `SELECTED` service targeting modes
- defaults to `SELECTED` mode for new settings records
- falls back to backend-recommended primary services when selected mode has no explicit saved targets
- gives priority to currently active non-VPN services when building the recommendation set
- recommendation order uses macOS network service order before name-based tie-breaking
- remembers the last confirmed recommended selected-service set and surfaces pending recommendation changes
- stores a pre-change snapshot under the runtime root for later restore
- restores prior proxy settings when disabling system proxy

## 7. Clash.Meta Lifecycle

### `GET /api/v1/core`

Purpose:

- Return Clash.Meta lifecycle status

Suggested response fields:

- installed version
- binary path summary
- running state
- pid if available
- last start time
- last error summary

### `POST /api/v1/core/start`

Purpose:

- Start Clash.Meta if it is not running

### `POST /api/v1/core/stop`

Purpose:

- Stop Clash.Meta if it is running

### `POST /api/v1/core/reload`

Purpose:

- Regenerate config and reload or restart Clash.Meta as required

## Suggested DAO-Oriented Data Areas

The current API draft implies at least these persistence areas:

1. app settings
2. subscriptions
3. imported proxy nodes
4. proxy group preferences
5. operation or diagnostics history

Runtime-only values should stay out of DAO persistence unless there is a clear reason to store them.

## Error Code Draft

Suggested initial error codes:

- `UNAUTHORIZED`
- `VALIDATION_ERROR`
- `NOT_FOUND`
- `CONFLICT`
- `CORE_NOT_AVAILABLE`
- `CORE_START_FAILED`
- `SYSTEM_PROXY_UPDATE_FAILED`
- `SUBSCRIPTION_REFRESH_FAILED`
- `INTERNAL_ERROR`

## Non-Goals For First API Draft

1. No TUN endpoints
2. No multi-user auth model
3. No remote cloud sync endpoints
4. No alternate core management endpoints
