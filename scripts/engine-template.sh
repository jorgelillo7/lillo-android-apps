#!/usr/bin/env bash
# Builds a game's slim Android release template: Godot compiled from its pinned source with the
# game's engine-profile.py (modules it does not use left out), arm64 only, dropped into the official
# template APK of the same version in place of the stock engine. scripts/engine-export.sh then uses
# it on its own. Usage: scripts/engine-template.sh engine/<game>
#
# Writes engine/<game>/export/template-release.apk (git-ignored). Rebuilds only when the profile or
# the Godot version changed. Needs, once per machine: the official export templates of the pinned
# version (as for any export) and the Android NDK below (installed here if missing). Godot's own
# Gradle template build needs JDK 17, which is why the official APK is reused instead: same version,
# same Java side, only libgodot_android.so differs.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
project="$(cd "${1:?usage: scripts/engine-template.sh engine/<game>}" && pwd)"
profile="$project/engine-profile.py"
[ -f "$profile" ] || { echo "$profile missing: the game exports with the stock engine"; exit 1; }
python3 "$root/scripts/engine_profile_check.py" "$project"

VERSION="$(sed -n 's/^VERSION="\(.*\)"/\1/p' "$root/scripts/godot.sh")"
# From the release's godot-$VERSION-stable.tar.xz.sha256 and platform/android/detect.py.
SOURCE_SHA256="a18ce0ccec3ecc40b0dd6c4f5132ca934e9fb7c2979717940ff32aee1eb35481"
NDK="29.0.14206865"
SCONS="4.9.1"

case "$(uname -s)" in
  Darwin) cache="$HOME/Library/Caches/lillo-engine"; templates="$HOME/Library/Application Support/Godot/export_templates" ;;
  *) cache="${XDG_CACHE_HOME:-$HOME/.cache}/lillo-engine"; templates="${XDG_DATA_HOME:-$HOME/.local/share}/godot/export_templates" ;;
esac
official="$templates/$VERSION.stable/android_release.apk"
[ -f "$official" ] || { echo "Install the official $VERSION export templates first (engine/README.md)"; exit 1; }
out="$project/export/template-release.apk"
stamp="$out.profile"
key="$VERSION $(shasum -a 256 "$profile" | cut -d' ' -f1)"
if [ -f "$out" ] && [ "$(cat "$stamp" 2>/dev/null)" = "$key" ]; then
  echo "$out is up to date"; exit 0
fi

# shellcheck source=env.sh
source "$root/scripts/env.sh" >/dev/null 2>&1 || true
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
mkdir -p "$cache"
src="$cache/godot-$VERSION-stable"
if [ ! -d "$src" ]; then
  archive="$cache/godot-$VERSION-stable.tar.xz"
  curl -fsSL "https://github.com/godotengine/godot/releases/download/$VERSION-stable/godot-$VERSION-stable.tar.xz" -o "$archive"
  echo "$SOURCE_SHA256  $archive" | shasum -a 256 -c - >/dev/null
  tar xf "$archive" -C "$cache" && rm "$archive"
fi
if [ ! -x "$cache/venv/bin/scons" ]; then
  python3 -m venv "$cache/venv"
  "$cache/venv/bin/pip3" install -q "scons==$SCONS"
fi
if [ ! -d "$ANDROID_HOME/ndk/$NDK" ]; then
  # sdkmanager is deprecated (and `yes | sdkmanager` dies of SIGPIPE under pipefail).
  "$ANDROID_HOME/cmdline-tools/latest/bin/android" sdk install "ndk/$NDK"
fi

game="$(basename "$project")"
(cd "$src" && "$cache/venv/bin/scons" platform=android target=template_release arch=arm64 \
  profile="$profile" -j"$(sysctl -n hw.ncpu 2>/dev/null || nproc)" ANDROID_HOME="$ANDROID_HOME" \
  >"$cache/build-$game.log" 2>&1) || { tail -20 "$cache/build-$game.log"; exit 1; }

work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
mkdir -p "$work/lib/arm64-v8a" "$(dirname "$out")"
cp "$src/platform/android/java/lib/libs/release/arm64-v8a/libgodot_android.so" "$work/lib/arm64-v8a/"
cp "$official" "$work/template.apk"
(cd "$work" && zip -q template.apk lib/arm64-v8a/libgodot_android.so)
mv "$work/template.apk" "$out"
echo "$key" > "$stamp"
ls -lh "$out"
