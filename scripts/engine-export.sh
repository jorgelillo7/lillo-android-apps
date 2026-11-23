#!/usr/bin/env bash
# Exports a game-engine project for Android:
#   scripts/engine-export.sh [--arm64|--aab] engine/summandos [out.apk|out.aab]
# --arm64 leaves out the x86_64 build (emulators only): half the size, for sending to a phone.
# --aab builds the Play bundle (arm64) through Godot's Gradle build: it reinstalls the project's
# Android build template (engine/<game>/android/, git-ignored) from android_source.zip and needs a
# JDK 17 to 23 for the template's Gradle ($ENGINE_JAVA_HOME, else Homebrew's openjdk@21).
# A game with an engine-profile.py and a built export/template-release.apk (scripts/engine-template.sh)
# exports with that slim engine, arm64 only, after checking it uses nothing the profile removes.
#
# Uses the project's "Android" preset (export_presets.cfg) and the pinned Godot. Needs, once per
# machine: the Android export templates of that Godot version installed
# (scripts/godot-templates.sh), and Godot's editor settings pointing at the Android SDK and a JDK 17+
# (Godot fills the SDK itself; JAVA_HOME from scripts/env.sh is written into it here). The build
# is signed with the game's upload key when <project>/keystore.properties exists
# (scripts/new-upload-key.sh), else with Godot's debug key: fine for testing, never for Play. An APK
# signed with one key does not install over one signed with the other: uninstall first.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
arm64_only=false
aab=false
case "${1:-}" in
  --arm64) arm64_only=true; shift ;;
  --aab) aab=true; arm64_only=true; shift ;;
esac
project="$(cd "${1:?usage: scripts/engine-export.sh [--arm64|--aab] <project dir> [out]}" && pwd)"
out="${2:-$project/export/$(basename "$project").$($aab && echo aab || echo apk)}"
mkdir -p "$(dirname "$out")"
out="$(cd "$(dirname "$out")" && pwd)/$(basename "$out")"
# shellcheck source=env.sh
source "$root/scripts/env.sh" >/dev/null
if $aab; then
  JAVA_HOME="${ENGINE_JAVA_HOME:-$(brew --prefix openjdk@21 2>/dev/null)/libexec/openjdk.jdk/Contents/Home}"
  [ -x "$JAVA_HOME/bin/java" ] || { echo "--aab needs a JDK 17 to 23: brew install openjdk@21, or set ENGINE_JAVA_HOME" >&2; exit 1; }
  export JAVA_HOME
fi

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

props="$project/keystore.properties"
if [ -f "$props" ] && [ -z "${GODOT_ANDROID_KEYSTORE_RELEASE_PATH:-}" ]; then
  property() { python3 -c 'import sys
for line in open(sys.argv[1], encoding="latin-1"):
    key, sep, value = line.partition("=")
    if sep and key.strip() == sys.argv[2]:
        print(value.strip().encode("latin-1").decode("unicode_escape"))' "$props" "$1"; }
  export GODOT_ANDROID_KEYSTORE_RELEASE_PATH="$(property storeFile)"
  export GODOT_ANDROID_KEYSTORE_RELEASE_USER="$(property keyAlias)"
  export GODOT_ANDROID_KEYSTORE_RELEASE_PASSWORD="$(property storePassword)"
  [ -f "$GODOT_ANDROID_KEYSTORE_RELEASE_PATH" ] || { echo "The upload key $GODOT_ANDROID_KEYSTORE_RELEASE_PATH is missing" >&2; exit 1; }
  echo "Signing with the upload key ($GODOT_ANDROID_KEYSTORE_RELEASE_PATH)"
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
template_args=()
if $aab; then
  sed -i.bak -e 's#^gradle_build/use_gradle_build=false#gradle_build/use_gradle_build=true#' \
    -e 's#^gradle_build/export_format=0#gradle_build/export_format=1#' "$presets" && rm -f "$presets.bak"
  rm -rf "$project/android"
  "$root/scripts/godot.sh" --headless --path "$project" --install-android-build-template --quit >/dev/null 2>&1 || true
  if [ ! -d "$project/android/build" ]; then
    # Godot installs the template only alongside an export; the first export then needs a second run.
    template_args=(--install-android-build-template)
  fi
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
if $aab && [ "${#template_args[@]}" -gt 0 ]; then
  "$root/scripts/godot.sh" --headless --path "$project" "${template_args[@]}" --export-release "Android" "$out" >/dev/null 2>&1 || true
fi
if $aab && $use_template; then
  # The Gradle build links the engine from the template's .aar: swap in the slim library.
  work="$(mktemp -d "${TMPDIR:-/tmp}/engine-aab.XXXXXX")"
  (cd "$work" && unzip -q "$template" lib/arm64-v8a/libgodot_android.so && mkdir -p jni/arm64-v8a \
    && mv lib/arm64-v8a/libgodot_android.so jni/arm64-v8a/ \
    && zip -q "$project/android/build/libs/release/godot-lib.template_release.aar" jni/arm64-v8a/libgodot_android.so)
  rm -rf "$work"
fi
rm -f "$out"
"$root/scripts/godot.sh" --headless --path "$project" --export-release "Android" "$out"
[ -f "$out" ] || { echo "Godot reported no error but wrote no $out" >&2; exit 1; }
ls -lh "$out"
