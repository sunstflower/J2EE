# Tray Assets

This directory now contains generated tray PNG assets plus SVG source files.

Generated default PNG filenames:

```text
tray-idle.png
tray-idle@2x.png
tray-on.png
tray-on@2x.png
tray-off.png
tray-off@2x.png
tray-pending.png
tray-pending@2x.png
```

Generated macOS template PNG filenames:

```text
macos-template-tray-idle.png
macos-template-tray-idle@2x.png
macos-template-tray-on.png
macos-template-tray-on@2x.png
macos-template-tray-off.png
macos-template-tray-off@2x.png
macos-template-tray-pending.png
macos-template-tray-pending@2x.png
```

Source SVG filenames:

```text
svg/default/tray-idle.svg
svg/default/tray-on.svg
svg/default/tray-off.svg
svg/default/tray-pending.svg

svg/macos-template/tray-idle.svg
svg/macos-template/tray-on.svg
svg/macos-template/tray-off.svg
svg/macos-template/tray-pending.svg
```

Behavior:

- on macOS, Electron first tries to load template-style tray files for the current state
- on other platforms, Electron first tries to load the default colored tray files
- if no packaged asset exists, the desktop shell falls back to generated vector tray icons
- this keeps development unblocked while allowing packaged builds to adopt stable production artwork later

Regeneration:

```bash
./scripts/generate_tray_assets.sh
```
