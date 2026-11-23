#!/usr/bin/env bash
# Exports a game-engine project for Android:
#   scripts/engine-export.sh [--arm64] engine/summandos [out.apk]
# --arm64 leaves out the x86_64 build (emulators only): half the size, for sending to a phone.
#
# Uses the project's "Android" preset (export_presets.cfg) and the pinned Godot. Needs, once per
# machine: the Android export templates of that Godot version installed (scripts/godot.sh
# --install-templates), and Godot's editor settings pointing at the Android SDK and a JDK 17+
# (Godot fills the SDK itself; JAVA_HOME from scripts/env.sh is written into it here). Without a
# release keystore the APK is signed with Godot's debug key: fine for testing, never for Play.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
arm64_only=false
if [ "${1:-}" = "--arm64" ]; then
  arm64_only=true
  shift
fi
project="$(cd "${1:?usage: scripts/engine-export.sh <project dir> [out.apk]}" && pwd)"
out="${2:-$project/export/$(basename "$project").apk}"
mkdir -p "$(dirname "$out")"
# shellcheck source=env.sh
source "$root/scripts/env.sh" >/dev/null

case "$(uname -s)" in
  Darwin) settings_dir="$HOME/Library/Application Support/Godot" ;;
  *) settings_dir="${XDG_DATA_HOME:-$HOME/.local/share}/godot" ;;
esac
settings="$(ls "$settings_dir"/editor_settings-4*.tres 2>/dev/null | head -1 || true)"
if [ -z "$settings" ]; then
  "$root/scripts/godot.sh" --headless --quit >/dev/null 2>&1 || true
  settings="$(ls "$settings_dir"/editor_settings-4*.tres | head -1)"
fi
if grep -q '^export/android/java_sdk_path' "$settings"; then
  sed -i.bak "s#^export/android/java_sdk_path = .*#export/android/java_sdk_path = \"$JAVA_HOME\"#" "$settings" && rm -f "$settings.bak"
else
  printf 'export/android/java_sdk_path = "%s"\n' "$JAVA_HOME" >> "$settings"
fi

keystore="$settings_dir/keystores/debug.keystore"
export GODOT_ANDROID_KEYSTORE_RELEASE_PATH="${GODOT_ANDROID_KEYSTORE_RELEASE_PATH:-$keystore}"
export GODOT_ANDROID_KEYSTORE_RELEASE_USER="${GODOT_ANDROID_KEYSTORE_RELEASE_USER:-androiddebugkey}"
export GODOT_ANDROID_KEYSTORE_RELEASE_PASSWORD="${GODOT_ANDROID_KEYSTORE_RELEASE_PASSWORD:-android}"

presets="$project/export_presets.cfg"
if $arm64_only; then
  cp "$presets" "$presets.full"
  trap 'mv "$presets.full" "$presets"' EXIT
  sed -i.bak 's#^architectures/x86_64=true#architectures/x86_64=false#' "$presets" && rm -f "$presets.bak"
fi

"$root/scripts/godot.sh" --headless --path "$project" --import >/dev/null 2>&1 || true
"$root/scripts/godot.sh" --headless --path "$project" --export-release "Android" "$out"
ls -lh "$out"
