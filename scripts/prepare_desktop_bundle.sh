#!/bin/zsh
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
DESKTOP_DIR="$REPO_ROOT/apps/desktop"
BUNDLE_DIR="$DESKTOP_DIR/.bundle"
WEB_DIST_DIR="$REPO_ROOT/apps/web/dist"
BACKEND_TARGET_DIR="$REPO_ROOT/services/local-api/target"
BACKEND_OUTPUT_DIR="$BUNDLE_DIR/backend"
WEB_OUTPUT_DIR="$BUNDLE_DIR/web"
CORE_OUTPUT_DIR="$BUNDLE_DIR/core"
CLASH_META_SOURCE="$REPO_ROOT/runtime-assets/clash-meta/bin/clash-meta"
BACKEND_JAR_SOURCE=$(find "$BACKEND_TARGET_DIR" -maxdepth 1 -type f -name '*.jar' ! -name 'original-*.jar' | head -n 1)

if [[ ! -d "$WEB_DIST_DIR" ]]; then
  echo "Missing web build output at $WEB_DIST_DIR. Run npm run build:web first." >&2
  exit 1
fi

if [[ -z "${BACKEND_JAR_SOURCE:-}" || ! -f "$BACKEND_JAR_SOURCE" ]]; then
  echo "Missing packaged local API jar in $BACKEND_TARGET_DIR. Run npm run build:local-api first." >&2
  exit 1
fi

if [[ ! -f "$CLASH_META_SOURCE" ]]; then
  echo "Missing Clash.Meta binary at $CLASH_META_SOURCE." >&2
  echo "Vendor it with: npm run vendor:core" >&2
  echo "Or import it manually with: npm run import:core -- /absolute/path/to/clash-meta" >&2
  exit 1
fi

rm -rf "$BUNDLE_DIR"
mkdir -p "$WEB_OUTPUT_DIR" "$BACKEND_OUTPUT_DIR" "$CORE_OUTPUT_DIR"

cp -R "$WEB_DIST_DIR"/. "$WEB_OUTPUT_DIR/"
cp "$BACKEND_JAR_SOURCE" "$BACKEND_OUTPUT_DIR/local-api.jar"
cp "$CLASH_META_SOURCE" "$CORE_OUTPUT_DIR/clash-meta"
chmod +x "$CORE_OUTPUT_DIR/clash-meta"

echo "Prepared Electron bundle resources in $BUNDLE_DIR"
