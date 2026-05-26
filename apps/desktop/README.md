# desktop

Electron shell scaffold.

Current contents:

- `src/main/main.js`: Electron main process entry
- `src/main/backend.js`: development-time Spring Boot launcher and runtime handoff
- `src/preload/preload.js`: preload bridge placeholder
- `renderer/index.html`: fallback renderer page for packaging or standalone shell checks

Current startup contract:

- Electron launches `mvn spring-boot:run` in development
- Spring Boot emits `LOCAL_API_READY port=<port>` when ready
- Electron parses that line and forwards `baseUrl + sessionToken` to the renderer

Planned responsibilities:

- app lifecycle
- tray/menu integration
- backend process launch
- session bootstrap handoff
- notifications
- window management
