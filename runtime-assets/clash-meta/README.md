# Clash.Meta Development Asset

Place the local development Clash.Meta executable here:

```text
runtime-assets/clash-meta/bin/clash-meta
```

Notes:

- This path is used only for local development bootstrap
- The binary is intentionally not committed to the repository
- Electron resolves this path first when `APP_CORE_CLASH_META_PATH` is not explicitly set
- Packaged application builds should ship their own bundled binary path instead of reusing the repository path
