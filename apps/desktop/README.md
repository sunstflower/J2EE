# desktop

Electron shell scaffold.

Current contents:

- `src/main/main.js`: Electron main process entry
- `src/main/backend.js`: development-time Spring Boot launcher and runtime handoff
- `src/main/trayIcon.js`: generated multi-size tray icon helper
- `src/preload/preload.js`: preload bridge placeholder
- `renderer/index.html`: fallback renderer page for packaging or standalone shell checks

Current startup contract:

- Electron launches `mvn spring-boot:run` in development
- Electron generates a fresh session token per run
- Electron passes `APP_SESSION_TOKEN`, `APP_RUNTIME_ROOT`, and `APP_CORE_CLASH_META_PATH` to Spring Boot
- Spring Boot emits `LOCAL_API_READY port=<port>` when ready
- Electron parses that line and forwards `baseUrl + sessionToken` to the renderer
- renderer can subscribe to runtime updates from preload instead of assuming they exist on first paint
- renderer can request a one-shot desktop notification when system proxy target recommendations change
- renderer can update tray state and receive tray-triggered recommendation acceptance events
- tray title and tooltip now reflect current system proxy status and pending recommendation state
- tray icon now switches visual state for `On`, `Off`, `Pending`, and idle conditions

Development asset convention:

- default runtime root: `<repo>/.runtime`
- default Clash.Meta binary path: `<repo>/runtime-assets/clash-meta/bin/clash-meta`
- `APP_CORE_CLASH_META_PATH` can override the default when testing another binary

Planned responsibilities:

- app lifecycle
- tray/menu integration
- backend process launch
- session bootstrap handoff
- notifications
- window management
