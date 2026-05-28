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

## Work Log Discipline

- Every meaningful work slice must leave a short record in `AGENTS.md`
- Record at least the date, baseline or branch context, and the main change or verification scope
- If the repository is reset, rolled back, or partially restored, record that state before continuing implementation
- Do not treat `AGENTS.md` as optional maintenance; update it as part of the change itself
- Any business-code bug found during implementation, debugging, or verification must be recorded in `AGENTS.md`
- Bug records should be maintained in-place when status changes, not left stale after the code has moved on

## Bug Maintenance

- Keep a short bug ledger in `AGENTS.md` for business-code defects that affect runtime behavior, data integrity, or user-visible workflow
- For each recorded bug, include: date, affected area, observed symptom, root cause if known, current status, and the change or verification that addressed it
- Update an existing bug entry when it is narrowed, reproduced, fixed, or regressed
- Do not remove historical bug entries silently; if a bug is resolved, mark it resolved and note the validating change

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
- Clash.Meta mixed and controller ports are runtime-assigned, so contributors must not hardcode `7890` or `9090` in new backend or frontend logic
- before Clash.Meta starts, its runtime ports should remain unset rather than exposing fake defaults, and UI state should render that as unknown or unallocated
- direct local-api debug sessions should use absolute runtime-root and Clash.Meta binary paths to avoid cwd-sensitive path resolution mistakes

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

## Work Log

### 2026-05-27

- Repository baseline was manually reset to commit `804296c93ddc4e125e187e1b1c5f381188129827` (`bug修复_风险维护`) before continuing work
- After the reset, the committed baseline includes the documented system-proxy, tray, bundled-core, and dynamic-port/runtime-root work already described in `README.md`
- The worktree still contains untracked follow-up files for subscription refresh, imported proxy nodes, proxy groups, and related frontend hooks; these should be treated as post-reset in-progress work until they are either integrated or discarded explicitly
- From this point onward, each new work slice must append or update a corresponding note in this section
- Post-reset inventory confirmed that the remaining untracked files are only a partial subscription/proxy feature slice
- Present untracked files cover frontend proxy hooks plus a few backend controller/DAO/DTO/test additions, but they do not include the matching backend service layer, imported-node persistence layer, or the updated committed frontend view/type files needed for a runnable end-to-end feature
- Practical consequence: the repository should treat these untracked files as incomplete follow-up work, not as an already-usable subscriptions/proxies implementation baseline
- New work slice begins from `804296c` baseline with the explicit goal of fully rebuilding the `subscriptions/proxies` line end-to-end instead of trying to preserve the earlier partial slice
- Immediate implementation target is: local subscription refresh, imported proxy node persistence, proxy group selection, Clash.Meta config generation from imported nodes, matching frontend views, and regression tests
- Rebuild work now in progress has already restored the backend first-pass structure for imported proxy nodes, subscription refresh, proxy group selection, and config rendering inputs
- Rebuild work now in progress has also reconnected the frontend subscriptions/proxies panels to real local API hooks instead of the older static proxy-group-only panel
- First-pass rebuild verification completed successfully for this slice: `mvn -q test --file services/local-api/pom.xml` passed and `npm run build:web` passed
- The rebuilt slice currently covers file-backed subscription refresh, imported proxy node persistence, proxy group selection persistence, dashboard integration, and Clash.Meta config generation from imported nodes with placeholder protocol fields
- Next follow-up slice starts from the rebuilt subscriptions/proxies baseline and targets real `http/https` subscription fetching instead of `file://`-only fixtures
- `SubscriptionContentFetcher` has now been extended to support `http/https` fetching using JDK `HttpClient`, while keeping `file://` fixture support for local verification
- Subscription URL validation has been widened accordingly to allow `http`, `https`, and `file`
- Added focused backend coverage for file fetch, successful HTTP fetch, and non-2xx HTTP failure handling; `mvn -q test --file services/local-api/pom.xml` passed again after this extension
- Next follow-up slice now targets replacing the fragile line-based subscription parser with structured YAML parsing while keeping the current imported-node schema intentionally minimal
- Structured YAML parsing has now replaced the earlier line-based subscription parser using `SnakeYAML`, while still limiting imported fields to the current minimal node schema
- Post-change verification for this parsing upgrade passed again with `mvn -q test --file services/local-api/pom.xml`

### 2026-05-28

- Next follow-up slice begins with the goal of extending the imported-node schema from minimal routing fields to protocol-critical `ss` / `vmess` fields so generated Clash.Meta config can stop relying on hardcoded placeholder credentials
- Imported-node schema has now been extended with protocol-critical `ss` / `vmess` fields: `cipher`, `password`, `uuid`, `alterId`, and `tls`
- SQLite imported-node storage and DAO mappings were expanded compatibly for those fields, and subscription parsing now persists them from structured YAML
- Clash.Meta config generation now consumes imported protocol fields before falling back to placeholders, reducing the amount of hardcoded protocol data in generated configs
- Post-change verification for the schema/config upgrade passed with `mvn -q test --file services/local-api/pom.xml`
- Next follow-up slice begins with the goal of reducing `vmess` transport loss during import by persisting transport-related fields from nested YAML and emitting them into generated Clash.Meta config
- Imported-node schema has now been extended again with `vmess` transport-related fields: `network`, `serverName`, `wsPath`, and `wsHost`
- Subscription YAML parsing now preserves nested `ws-opts` and common SNI/server-name aliases instead of flattening everything into string-only top-level keys
- Clash.Meta config generation now emits `network`, `servername`, and nested `ws-opts.headers.Host` for imported `vmess` nodes when those fields are present
- Integration coverage was expanded to assert both transport-field persistence and generated config output for the new `vmess` transport fields
- Post-change verification for the transport-field upgrade passed with `mvn -q test --file services/local-api/pom.xml`
- UI-side Electron point verification exposed a live runtime defect: refreshing multiple subscriptions that contain the same proxy names caused generated Clash.Meta config to emit duplicate `proxies[].name` entries, and core `start/reload` then failed with `Parse config error: proxy JP-Test-2 is the duplicate name`
- Root cause was in backend config generation, not in the UI buttons: imported nodes from different subscriptions were merged into the runtime config without any de-duplication by effective Clash.Meta proxy name
- Fix slice completed: generated Clash.Meta `proxies` entries are now deduplicated by effective node name at config-render time so core lifecycle actions can tolerate overlapping subscription content instead of producing invalid config
- Verification for the duplicate-name fix passed in two layers: `mvn -q test --file services/local-api/pom.xml` passed with new coverage for cross-subscription duplicate names, and a live `POST /api/v1/core/start` against the existing `.runtime` dataset returned `RUNNING` instead of reproducing the previous Clash.Meta parse failure
- `AGENTS.md` maintenance rules were tightened in the same slice so future business-code bugs must be recorded here when discovered and updated as their status changes
- Follow-up runtime defect also confirmed: repeated local-api restarts could leave orphaned Clash.Meta processes under the same runtime root because the service only tracked the in-memory `Process` handle and had no persisted runtime ownership or cleanup path after backend restarts
- Fix slice now extends core runtime ownership with runtime-root-scoped PID tracking and startup/stop cleanup of matching Clash.Meta processes so `start/stop/reload` can recover control of the managed core after backend restarts instead of accumulating orphan processes
- Verification for the orphan-process fix passed against the live `.runtime` dataset: before the fix, the same runtime root still had 3 stale Clash.Meta processes; after the fix, one `core/start` request cleaned those stale instances and started a single managed core, and `core/stop` then reduced the runtime-root process count to 0 while removing the persisted `core.pid`
- Repository health convergence slice completed: previously deleted but still expected regression tests and top-level runtime docs were restored so the worktree no longer contains unexplained missing controller/test/doc files
- Post-convergence verification passed with `mvn -q test --file services/local-api/pom.xml`, including the restored subscription-controller and core-reload integration coverage
- Rules/implementation alignment slice completed: `README.md` and `docs/runtime.md` now explicitly reflect the current development-mode CORS/preflight behavior, generated-config duplicate-name de-duplication, runtime-root pid tracking, and stale core-process cleanup semantics already present in the backend implementation
