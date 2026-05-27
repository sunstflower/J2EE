# Clash.Meta Bundled Asset

The repository reserves this exact path for the bundled macOS `Clash.Meta` executable:

```text
runtime-assets/clash-meta/bin/clash-meta
```

Preferred vendor workflow:

```bash
npm run vendor:core
```

This downloads the pinned version from:

- release repository: `MetaCubeX/mihomo`
- version file: `runtime-assets/clash-meta/version.txt`

Manual import workflow:

```bash
npm run import:core -- /absolute/path/to/clash-meta
```

Notes:

- This repository path is the canonical source for both local development bootstrap and packaged desktop bundling
- `scripts/prepare_desktop_bundle.sh` copies this binary into `apps/desktop/.bundle/core/clash-meta`
- Electron resolves this path first when `APP_CORE_CLASH_META_PATH` is not explicitly set in development
- Packaging currently requires this file to exist before `npm run prepare:desktop-bundle`
- `npm run vendor:core` chooses the macOS asset that matches the current machine architecture
- The vendor or import workflow writes `runtime-assets/clash-meta/import-manifest.txt` with source metadata, import time, file size, and SHA-256
