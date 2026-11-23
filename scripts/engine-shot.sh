#!/usr/bin/env bash
# Screenshot of a game-engine project on the Mac, drawn by the real renderer:
#   scripts/engine-shot.sh [--landscape|--store|--tablet] [--progress <file.cfg>] engine/<game> <out.png> <seconds> [-- game options]
# Upright 540x1170 unless --landscape (1170x540) or --store (1080x2424, drawn off-screen at
# Play's phone size; the game's main scene must call LilloCapture.rehost) or --tablet (2560x1600,
# a 10" tablet sideways, the same way). --progress puts a settings.cfg in the game's
# user folder for the shot (e.g. stars for a level map) and restores the player's own afterwards.
# Needs the screen unlocked: with it locked Godot draws nothing (use the emulator instead).
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
resolution="540x1170"
progress=""
shot_size=""
while [ $# -gt 0 ]; do
  case "$1" in
    --landscape) resolution="1170x540"; shift ;;
    --store) shot_size=1080x2424; shift ;;
    --tablet) shot_size=2560x1600; resolution="1170x540"; shift ;;
    --progress) progress="$2"; shift 2 ;;
    *) break ;;
  esac
done
project="${1:?usage: scripts/engine-shot.sh [--landscape] [--progress f.cfg] engine/<game> <out.png> <seconds> [-- options]}"
out="$(cd "$(dirname "$2")" && pwd)/$(basename "$2")"
after="${3:?seconds after which to take the shot}"
shift 3
[ "${1:-}" = "--" ] && shift

name="$(sed -n 's/^config\/name="\(.*\)"/\1/p' "$project/project.godot")"
case "$(uname -s)" in
  Darwin) data="$HOME/Library/Application Support/Godot/app_userdata/$name" ;;
  *) data="${XDG_DATA_HOME:-$HOME/.local/share}/godot/app_userdata/$name" ;;
esac
if [ -n "$progress" ]; then
  mkdir -p "$data"
  [ -f "$data/settings.cfg" ] && mv "$data/settings.cfg" "$data/settings.cfg.player"
  cp "$progress" "$data/settings.cfg"
  trap 'rm -f "$data/settings.cfg"; [ -f "$data/settings.cfg.player" ] && mv "$data/settings.cfg.player" "$data/settings.cfg"' EXIT
fi
extra=()
[ -n "$shot_size" ] && extra=(--shot-size="$shot_size")
timeout 120 "$root/scripts/godot.sh" --path "$project" --resolution "$resolution" -- \
  "$@" ${extra[@]+"${extra[@]}"} --shot="$out" --after="$after" >/dev/null 2>&1
ls -lh "$out"
