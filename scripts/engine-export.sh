#!/usr/bin/env bash
# Exports a game-engine project for Android:
#   scripts/engine-export.sh [--arm64] engine/summandos [out.apk]
# --arm64 leaves out the x86_64 build (emulators only): half the size, for sending to a phone.
# A game with an engine-profile.py and a built export/template-release.apk (scripts/engine-template.sh)
# exports with that slim engine, arm64 only, after checking it uses nothing the profile removes.
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
template="$project/export/template-release.apk"
use_template=false
if [ -f "$project/engine-profile.py" ] && [ -f "$template" ]; then
  python3 "$root/scripts/engine_profile_check.py" "$project" >/dev/null || {
    python3 "$root/scripts/engine_profile_check.py" "$project"; exit 1; }
  use_template=true
  arm64_only=true   # the slim template carries arm64 only
fi
if $arm64_only || $use_template; then
  cp "$presets" "$presets.full"
  trap 'mv "$presets.full" "$presets"' EXIT
fi
if $arm64_only; then
  sed -i.bak 's#^architectures/x86_64=true#architectures/x86_64=false#' "$presets" && rm -f "$presets.bak"
fi
if $use_template; then
  python3 - "$presets" "$template" <<'PY'
import sys
path, template = sys.argv[1], sys.argv[2]
text = open(path).read()
text = text.replace("[preset.0.options]\n", f'[preset.0.options]\n\ncustom_template/release="{template}"\n', 1)
open(path, "w").write(text)
PY
  echo "Exporting with the slim engine ($template)"
fi

"$root/scripts/godot.sh" --headless --path "$project" --import >/dev/null 2>&1 || true
"$root/scripts/godot.sh" --headless --path "$project" --export-release "Android" "$out"
ls -lh "$out"
