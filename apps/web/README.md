# web

React + Vite scaffold.

Current contents:

- React 19 application entry
- Vite configuration
- TypeScript configuration
- Tailwind CSS v4 via the Vite plugin
- frontend split into `features`, `shared`, and mock `data` modules
- local API client placeholder plus mock-backed dashboard service boundary
- application shell with overview, proxies, subscriptions, and settings placeholders
- settings panel now reads and writes real local API data
- subscriptions panel now reads and writes real local API data
- overview runtime cards now read real local API runtime summary data
- overview core manager panel now reads and drives real local API core lifecycle endpoints

Real local API mode can be enabled later with:

- `VITE_USE_REAL_LOCAL_API=true`
- `VITE_LOCAL_API_BASE_URL=http://127.0.0.1:<port>/api/v1`
- `VITE_LOCAL_API_SESSION_TOKEN=<token>`

Planned responsibilities:

- settings UI
- subscription management UI
- runtime status UI
- logs and diagnostics UI
