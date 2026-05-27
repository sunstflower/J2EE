#!/bin/zsh
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)
SVG_DIR="$REPO_ROOT/apps/desktop/assets/tray/svg"
OUT_DIR="$REPO_ROOT/apps/desktop/assets/tray"

for state in idle on off pending; do
  default_svg="$SVG_DIR/default/tray-$state.svg"
  default_png="$OUT_DIR/tray-$state.png"
  default_png_2x="$OUT_DIR/tray-$state@2x.png"

  macos_svg="$SVG_DIR/macos-template/tray-$state.svg"
  macos_png="$OUT_DIR/macos-template-tray-$state.png"
  macos_png_2x="$OUT_DIR/macos-template-tray-$state@2x.png"

  sips -s format png "$default_svg" --out "$default_png" >/dev/null
  sips -z 18 18 "$default_png" --out "$default_png" >/dev/null

  sips -s format png "$default_svg" --out "$default_png_2x" >/dev/null
  sips -z 36 36 "$default_png_2x" --out "$default_png_2x" >/dev/null

  sips -s format png "$macos_svg" --out "$macos_png" >/dev/null
  sips -z 18 18 "$macos_png" --out "$macos_png" >/dev/null

  sips -s format png "$macos_svg" --out "$macos_png_2x" >/dev/null
  sips -z 36 36 "$macos_png_2x" --out "$macos_png_2x" >/dev/null
done

echo "Generated tray PNG assets in $OUT_DIR"
