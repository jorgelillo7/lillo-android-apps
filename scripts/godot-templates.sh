#!/usr/bin/env bash
# Installs the Android export templates of the pinned Godot (scripts/godot.sh) into Godot's data
# folder, from the official GitHub release, checksum verified. Of the 1.3 GB .tpz it keeps only
# what Android exports use: android_release.apk and android_debug.apk (APK exports) and
# android_source.zip (the Gradle build template, needed for the Play bundle, .aab). The download
# is deleted afterwards. Does nothing when they are already there.
#
# Upgrading Godot means changing VERSION and SHA512 below, from the release's SHA512-SUMS.txt.
set -euo pipefail
VERSION="4.7.2"
SHA512="ca4d71c4d7b81dfc15d1a98baa07534aa95b03fdda78a0075b06672e1648d2e5f40980c9adc28d23e1b92e732ee7bf3461997aa804af74ec2fcd7a93ccb84079"
FILES=(android_release.apk android_debug.apk android_source.zip version.txt)

case "$(uname -s)" in
  Darwin) dest="$HOME/Library/Application Support/Godot/export_templates/$VERSION.stable" ;;
  *) dest="${XDG_DATA_HOME:-$HOME/.local/share}/godot/export_templates/$VERSION.stable" ;;
esac
missing=()
for file in "${FILES[@]}"; do [ -f "$dest/$file" ] || missing+=("templates/$file"); done
[ "${#missing[@]}" = 0 ] && { echo "Android export templates for Godot $VERSION already installed"; exit 0; }

work="$(mktemp -d "${TMPDIR:-/tmp}/godot-templates.XXXXXX")"
trap 'rm -rf "$work"' EXIT
tpz="$work/templates.tpz"
echo "Downloading Godot $VERSION export templates (1.3 GB)"
curl -fSL --progress-bar -o "$tpz" \
  "https://github.com/godotengine/godot/releases/download/${VERSION}-stable/Godot_v${VERSION}-stable_export_templates.tpz"
echo "$SHA512  $tpz" | shasum -a 512 -c - >/dev/null || { echo "godot-templates.sh: checksum mismatch" >&2; exit 1; }
mkdir -p "$dest"
unzip -q -o -j "$tpz" "${missing[@]}" -d "$dest"
ls -lh "$dest"
