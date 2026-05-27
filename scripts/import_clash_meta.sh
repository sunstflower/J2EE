#!/bin/zsh
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: npm run import:core -- /absolute/path/to/clash-meta" >&2
  exit 1
fi

SOURCE_PATH="$1"
if [[ "$SOURCE_PATH" != /* ]]; then
  echo "Clash.Meta source path must be absolute." >&2
  exit 1
fi

if [[ ! -f "$SOURCE_PATH" ]]; then
  echo "Clash.Meta source file does not exist: $SOURCE_PATH" >&2
  exit 1
fi

if [[ ! -x "$SOURCE_PATH" ]]; then
  echo "Clash.Meta source file is not executable: $SOURCE_PATH" >&2
  exit 1
fi

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
TARGET_DIR="$REPO_ROOT/runtime-assets/clash-meta/bin"
TARGET_PATH="$TARGET_DIR/clash-meta"
MANIFEST_PATH="$REPO_ROOT/runtime-assets/clash-meta/import-manifest.txt"

mkdir -p "$TARGET_DIR"
cp "$SOURCE_PATH" "$TARGET_PATH"
chmod +x "$TARGET_PATH"

{
  echo "source_path=$SOURCE_PATH"
  echo "imported_at=$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  echo "size_bytes=$(stat -f '%z' "$TARGET_PATH")"
  echo "sha256=$(shasum -a 256 "$TARGET_PATH" | awk '{print $1}')"
} > "$MANIFEST_PATH"

echo "Imported Clash.Meta into $TARGET_PATH"
echo "Updated manifest at $MANIFEST_PATH"
