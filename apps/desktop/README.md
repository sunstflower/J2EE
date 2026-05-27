# desktop

Electron shell scaffold.

Current contents:

- `src/main/main.js`: Electron main process entry
- `src/main/backend.js`: development-time Spring Boot launcher and runtime handoff
- `src/main/trayIcon.js`: generated multi-size tray icon helper
- `src/main/trayAssets.js`: tray asset resolver with generated-icon fallback
- `assets/tray/`: generated tray PNG assets plus SVG source files
- `src/preload/preload.js`: preload bridge placeholder
- `renderer/index.html`: fallback renderer page for packaging or standalone shell checks
- `.bundle/`: generated packaging staging area for web assets, backend jar, and bundled core

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
- packaged tray image files are now generated in-repo and preferred when present; Electron still falls back to generated vector icons if they are missing
- packaged builds now load the renderer from `Contents/Resources/web/index.html`
- packaged builds now launch `Contents/Resources/backend/local-api.jar` with `java -jar`
- packaged builds now prefer `Contents/Resources/core/clash-meta` as the bundled core path

Development asset convention:

- default runtime root: `<repo>/.runtime`
- default Clash.Meta binary path: `<repo>/runtime-assets/clash-meta/bin/clash-meta`
- preferred bundled core workflow: `npm run vendor:core`
- `APP_CORE_CLASH_META_PATH` can override the default when testing another binary

Packaging scaffold:

- `npm run build:web` builds the React renderer into `apps/web/dist`
- `npm run build:local-api` packages the Spring Boot app jar under `services/local-api/target`
- `npm run vendor:core` downloads the pinned `MetaCubeX/mihomo` macOS binary into `runtime-assets/clash-meta/bin/clash-meta`
- `npm run import:core -- /absolute/path/to/clash-meta` imports the bundled core into `runtime-assets/clash-meta/bin/clash-meta`
- `npm run prepare:desktop-bundle` stages those outputs into `apps/desktop/.bundle`
- `npm run build:desktop` runs `electron-builder --dir`
- `npm run dist:desktop` runs `electron-builder --mac zip`

Current packaging scope:

- unsigned local packaging only
- macOS `dir` and `zip` targets
- no notarization, auto-update, or signing secrets wired yet

Planned responsibilities:

- app lifecycle
- tray/menu integration
- backend process launch
- session bootstrap handoff
- notifications
- window management
