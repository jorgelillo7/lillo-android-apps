#!/usr/bin/env bash
# Captures Summandos' raw store screenshots, drawn off-screen on the Mac: phone (1080x2424) into
# store/raw/<locale>/ and 10" tablet sideways (2560x1600) into store/raw-tablet/<locale>/, for
# tools/generate_store_assets.py. Needs the models (scripts/engine-assets.sh), the bought pack included:
# the boss shot is the Golem. The battle and Legend shots show other squad colours, the last one
# night mode.
# Usage (repo root): engine/summandos/tools/capture_store_screenshots.sh
set -euo pipefail

game=engine/summandos
progress=$game/tools/samples/progress-easy.cfg
for device in store tablet; do
for pair in es-ES:es en-US:en; do
  locale=${pair%%:*}
  lang=${pair##*:}
  out=$game/store/raw/$locale
  [ "$device" = tablet ] && out=$game/store/raw-tablet/$locale
  mkdir -p "$out"
  shot() { scripts/engine-shot.sh --$device "$@" >/dev/null; }
  shot --progress $progress $game "$out/01-menu.png" 2.5 -- --lang=$lang
  shot $game "$out/02-gates.png" 1.3 -- --lang=$lang --mode=play --difficulty=normal --level=8 --autosteer
  shot $game "$out/02b-cast.png" 3 -- --lang=$lang --cast
  shot $game "$out/03-battle.png" 5 -- --lang=$lang --mode=stress --squad=orange
  shot $game "$out/04-boss.png" 26 -- --lang=$lang --mode=play --level=15 --autosteer
  shot $game "$out/05-think.png" 3 -- --lang=$lang --mode=play --difficulty=expert --level=3 --squad=lime
  shot --progress $progress $game "$out/06-levels.png" 2.5 -- --lang=$lang --levels
  shot $game "$out/07-night.png" 10 -- --lang=$lang --mode=play --difficulty=normal --level=8 --autosteer --palette=night
  echo "$device $locale done"
done
done
