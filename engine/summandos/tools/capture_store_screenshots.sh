#!/usr/bin/env bash
# Captures Summandos' raw store screenshots (1080x2424, drawn off-screen on the Mac) into
# store/raw/<locale>/, for tools/generate_store_assets.py. Needs the models (scripts/engine-assets.sh).
# Usage (repo root): engine/summandos/tools/capture_store_screenshots.sh
set -euo pipefail

game=engine/summandos
progress=$game/tools/samples/progress-easy.cfg
for pair in es-ES:es en-US:en; do
  locale=${pair%%:*}
  lang=${pair##*:}
  out=$game/store/raw/$locale
  mkdir -p "$out"
  shot() { scripts/engine-shot.sh --store "$@" >/dev/null; }
  shot --progress $progress $game "$out/01-menu.png" 2.5 -- --lang=$lang
  shot $game "$out/02-gates.png" 1.3 -- --lang=$lang --mode=play --difficulty=normal --level=8 --autosteer
  shot $game "$out/02b-cast.png" 3 -- --lang=$lang --cast
  shot $game "$out/03-battle.png" 5 -- --lang=$lang --mode=stress
  shot $game "$out/04-boss.png" 17 -- --lang=$lang --mode=play --level=5 --autosteer
  shot $game "$out/05-think.png" 3 -- --lang=$lang --mode=play --difficulty=expert --level=3
  shot --progress $progress $game "$out/06-levels.png" 2.5 -- --lang=$lang --levels
  echo "$locale done"
done
