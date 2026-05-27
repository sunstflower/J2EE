#!/bin/zsh
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
CORE_DIR="$REPO_ROOT/runtime-assets/clash-meta"
VERSION_FILE="$CORE_DIR/version.txt"
TARGET_DIR="$CORE_DIR/bin"
TARGET_PATH="$TARGET_DIR/clash-meta"
MANIFEST_PATH="$CORE_DIR/import-manifest.txt"

if [[ $# -gt 1 ]]; then
  echo "Usage: npm run vendor:core -- [version]" >&2
  exit 1
fi

VERSION="${1:-$(tr -d '[:space:]' < "$VERSION_FILE")}"
if [[ -z "$VERSION" ]]; then
  echo "Missing Clash.Meta version. Set runtime-assets/clash-meta/version.txt or pass one explicitly." >&2
  exit 1
fi

ARCH=$(uname -m)
case "$ARCH" in
  arm64)
    ASSET_NAME="mihomo-darwin-arm64-${VERSION}.gz"
    ;;
  x86_64)
    ASSET_NAME="mihomo-darwin-amd64-compatible-${VERSION}.gz"
    ;;
  *)
    echo "Unsupported macOS architecture: $ARCH" >&2
    exit 1
    ;;
esac

ASSET_URL="https://github.com/MetaCubeX/mihomo/releases/download/${VERSION}/${ASSET_NAME}"
TMP_DIR=$(mktemp -d)
TMP_ARCHIVE="$TMP_DIR/$ASSET_NAME"
TMP_BINARY="$TMP_DIR/clash-meta"

cleanup() {
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

mkdir -p "$TARGET_DIR"

curl -fL "$ASSET_URL" -o "$TMP_ARCHIVE"
gunzip -c "$TMP_ARCHIVE" > "$TMP_BINARY"
chmod +x "$TMP_BINARY"
cp "$TMP_BINARY" "$TARGET_PATH"
chmod +x "$TARGET_PATH"

{
  echo "source_type=vendor"
  echo "version=$VERSION"
  echo "architecture=$ARCH"
  echo "asset_name=$ASSET_NAME"
  echo "asset_url=$ASSET_URL"
  echo "imported_at=$(date -u '+%Y-%m-%dT%H:%M:%SZ')"
  echo "size_bytes=$(stat -f '%z' "$TARGET_PATH")"
  echo "sha256=$(shasum -a 256 "$TARGET_PATH" | awk '{print $1}')"
} > "$MANIFEST_PATH"

echo "Vendored Clash.Meta $VERSION into $TARGET_PATH"
echo "Updated manifest at $MANIFEST_PATH"
