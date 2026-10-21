#!/usr/bin/env bash
# Captures the raw Play Store screenshots (store/raw/<locale>/) for every locale.
# Needs a running emulator; scripts/capture_lib.sh installs the app and does the rest.
# Then run: .venv/bin/python apps/christmas-countdown/tools/generate_store_assets.py
#
# Usage: apps/christmas-countdown/tools/capture_store_screenshots.sh   (LOCALES="es-ES" for one)
set -euo pipefail
source "$(dirname "$0")/../../../scripts/capture_lib.sh"

# On-screen labels per locale (taps go by text, so layout differences between languages don't matter).
label() {
  case "$1:$2" in
    en-US:time) echo "Time" ;;        es-ES:time) echo "Tiempo" ;;
    en-US:sleeps) echo "Sleeps" ;;    es-ES:sleeps) echo "Noches" ;;
    en-US:advent) echo "Advent" ;;    es-ES:advent) echo "Adviento" ;;
    en-US:home) echo "Countdown" ;;   es-ES:home) echo "Cuenta atrás" ;;
    en-US:about) echo "About" ;;      es-ES:about) echo "Información" ;;
  esac
}

capture_start christmas-countdown com.jorgelillo.christmascountdown
capture_date 121810002026   # 18 December 2026: most doors open, music days 17 and 18 unlocked
"$ADB" shell pm clear "$PKG" >/dev/null   # once: the doors opened in English stay open in Spanish

first=1
for locale in $CAPTURE_LOCALES; do
  capture_open "$locale" 30 keep

  tap "$(label "$locale" time)";   shot "$locale" 01-countdown
  tap "$(label "$locale" sleeps)"; shot "$locale" 02-sleeps
  tap "$(label "$locale" advent)" 6
  if [ "$first" = 1 ]; then
    # Open some unlocked doors (the layout changes every year) so the grid shows gifts; doors 15-18
    # of the 2026 calendar stay closed for the plan and music screenshots (18 is today).
    for door in 1 2 3 4 5 6 7 8 9 10 11 12 13 14; do
      if tap "$door" 4 2>/dev/null; then back; fi
    done
    first=0
  fi
  shot "$locale" 03-advent
  # A plan: movie night (door 15) in English, the sock snowman (door 16) in Spanish.
  if [ "$locale" = en-US ]; then tap "15" 5; else tap "16" 5; fi
  shot "$locale" 04-plan; back
  # A music day: Jingle Bell Rock (door 18) in English, Campana sobre campana (door 17) in Spanish.
  if [ "$locale" = en-US ]; then tap "18" 5; else tap "17" 5; fi
  shot "$locale" 05-music; back
  tap "$(label "$locale" about)" 6; shot "$locale" 06-about; back
  tap "$(label "$locale" home)"
done
