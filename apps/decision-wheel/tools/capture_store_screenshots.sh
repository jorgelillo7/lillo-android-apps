#!/usr/bin/env bash
# Captures the raw Play Store screenshots (store/raw/<locale>/) for every locale.
# Needs a running emulator; scripts/capture_lib.sh installs the app and does the rest.
# Then run: .venv/bin/python apps/decision-wheel/tools/generate_store_assets.py
#
# Usage: apps/decision-wheel/tools/capture_store_screenshots.sh   (LOCALES="es-ES" for one)
set -euo pipefail
source "$(dirname "$0")/../../../scripts/capture_lib.sh"

# Wheel to open and veto candidates, per locale (macOS bash 3.2: no associative arrays).
# The winner's chip gets a "⏱" suffix, so it is skipped automatically (its exact label no longer
# matches); the first two candidates that do match are vetoed.
labels() {
  case "$1" in
    en-US) WHEEL="Where should we eat?"; VETOES=("Indian" "Chinese" "Thai" "Tacos") ;;
    es-ES) WHEEL="¿Dónde comemos?"; VETOES=("Ginos" "Lizarran" "TGB" "Goiko") ;;
  esac
}

capture_start decision-wheel com.jorgelillo.decisionwheel
for locale in $CAPTURE_LOCALES; do
  labels "$locale"
  capture_open "$locale" 5                                     # fresh presets, empty history

  shot "$locale" 04-home
  tap "$WHEEL" 5
  shot "$locale" 01-wheel
  tap spin 7                                                   # spin lasts ~5 s
  shot "$locale" 02-result
  tap accept 3                                                 # winner now has a smaller slice
  vetoed=0
  for option in "${VETOES[@]}"; do
    [ "$vetoed" -lt 2 ] || break
    if tap "$option" 2 2>/dev/null; then vetoed=$((vetoed + 1)); fi
  done
  sleep 1
  shot "$locale" 03-repeats-vetoes
  tap back 3
done
