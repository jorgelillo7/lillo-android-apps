#!/usr/bin/env bash
# Runs a game-engine project's GDScript tests (GUT) headless: scripts/engine-test.sh engine/core
#
# GUT is downloaded from its GitHub tag (checksum verified) into the project's addons/gut, which is
# git-ignored. Tests are res://tests/test_*.gd; the exit code is non-zero when one fails.
set -euo pipefail

GUT_VERSION="9.6.1"
GUT_SHA256="3eddacf23d69109e0bef4efa6c9194b1c9e5f812b5124eca438850c9142a2372"

root="$(cd "$(dirname "$0")/.." && pwd)"
project="$(cd "${1:?usage: scripts/engine-test.sh <project dir>}" && pwd)"
godot="$root/scripts/godot.sh"

if [ ! -f "$project/addons/gut/gut_cmdln.gd" ]; then
  cache="${GODOT_HOME:-$HOME/.cache/lillo-android-apps}/gut-$GUT_VERSION.tar.gz"
  mkdir -p "$(dirname "$cache")"
  [ -f "$cache" ] || curl -fsSL -o "$cache" "https://github.com/bitwes/Gut/archive/refs/tags/v${GUT_VERSION}.tar.gz"
  echo "$GUT_SHA256  $cache" | shasum -a 256 -c - >/dev/null || { rm -f "$cache"; echo "engine-test.sh: GUT checksum mismatch" >&2; exit 1; }
  mkdir -p "$project/addons"
  tar xzf "$cache" -C "$project/addons" --strip-components=2 "Gut-${GUT_VERSION}/addons/gut"
fi

# A first import builds .godot/ (class names, resources); without it class_name lookups fail.
"$godot" --headless --path "$project" --import >/dev/null 2>&1 || true
"$godot" --headless --path "$project" -s res://addons/gut/gut_cmdln.gd -gdir=res://tests -ginclude_subdirs -gexit
