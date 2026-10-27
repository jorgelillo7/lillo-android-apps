#!/usr/bin/env bash
# Captures the raw Play Store screenshots (store/raw/<locale>/) of ¿Te pilla la chancla?: home, a run
# in motion (tapping to jump, so croquetas, combos and near misses show), the result, the run of the
# day. Runs are live, so look at every picture before generating the graphics; run it again for
# better moments. Needs a running emulator; taps go by test tag (the jumps by the middle of the
# screen). Then: .venv/bin/python apps/slipper/tools/generate_store_assets.py
#
# Usage: apps/slipper/tools/capture_store_screenshots.sh
set -euo pipefail
source "$(dirname "$0")/../../../scripts/capture_lib.sh"

# Taps to jump for <seconds>, then a screenshot mid-run.
play_for() {
  local end=$((SECONDS + $1))
  while [ $SECONDS -lt $end ]; do "$ADB" shell input tap 540 1500; sleep 0.45; done
}

capture_start slipper com.jorgelillo.slipper
for locale in $CAPTURE_LOCALES; do
  capture_open "$locale" 4
  tap sound 1 # silent emulator
  shot "$locale" 01-home
  tap play 1
  play_for 4
  shot "$locale" 02-run
  play_for 2   # soon after: a run can end within a few more seconds
  shot "$locale" 03-run
  wait_for final_score
  sleep 1
  shot "$locale" 04-result
  tap home 2
  tap daily 1
  play_for 6
  shot "$locale" 05-daily
  wait_for final_score
done
