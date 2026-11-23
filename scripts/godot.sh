#!/usr/bin/env bash
# Runs the pinned Godot, downloading it first from the official GitHub release (checksum verified).
#
#   scripts/godot.sh --version
#   scripts/godot.sh --headless --path engine/core --import
#   scripts/godot.sh --print-path        # the binary's path, installing it if needed
#
# The binary lives in $GODOT_HOME (default ~/.cache/lillo-android-apps/godot/<version>), never in
# the repo. Upgrading Godot means changing VERSION and both SHA-512 lines below, from the release's
# SHA512-SUMS.txt.
set -euo pipefail

VERSION="4.7.2"
SHA512_MACOS="38aa16e5bba2083941fc5b3e54be0089bd4cc35e32415f5b9fd9a8a6a7b9818255d44532ea8ef94b5aef56c4b407c2d634fa4f657e4ebe681ebbf59b7bac69ca"
SHA512_LINUX="9aa00f7a605200940bce3027a567b782f49bd8e940dd06ae9e987bd65aee1b1467edd56ed84fcdcbdd44354bf613bdbb4e5d2913e925850368e150c59ed54c65"

home="${GODOT_HOME:-$HOME/.cache/lillo-android-apps/godot/$VERSION}"
case "$(uname -s)" in
  Darwin) asset="Godot_v${VERSION}-stable_macos.universal.zip"; sha="$SHA512_MACOS"
          bin="$home/Godot.app/Contents/MacOS/Godot" ;;
  Linux)  asset="Godot_v${VERSION}-stable_linux.x86_64.zip"; sha="$SHA512_LINUX"
          bin="$home/Godot_v${VERSION}-stable_linux.x86_64" ;;
  *) echo "godot.sh: unsupported system $(uname -s)" >&2; exit 1 ;;
esac

if [ ! -x "$bin" ]; then
  mkdir -p "$home"
  zip="$home/$asset"
  echo "godot.sh: downloading Godot $VERSION" >&2
  curl -fsSL -o "$zip" "https://github.com/godotengine/godot/releases/download/${VERSION}-stable/$asset"
  echo "$sha  $zip" | shasum -a 512 -c - >/dev/null || { rm -f "$zip"; echo "godot.sh: checksum mismatch for $asset" >&2; exit 1; }
  unzip -q -o "$zip" -d "$home"
  rm -f "$zip"
  chmod +x "$bin"
fi

if [ "${1:-}" = "--print-path" ]; then echo "$bin"; exit 0; fi
exec "$bin" "$@"
