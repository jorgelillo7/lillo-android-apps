#!/usr/bin/env bash
# Captures the raw Play Store screenshots (store/raw/es-ES/) of a season with the public SAMPLE deck:
# never the league's private one (real people), whatever is on this Mac. A card, the joker, the
# league chat after a matchday, the table, the ending and the trophies. Needs a running emulator;
# taps go by test tag. Then run: .venv/bin/python apps/league-cards/tools/generate_store_assets.py
#
# Usage: apps/league-cards/tools/capture_store_screenshots.sh
set -euo pipefail
export LOCALES="${LOCALES:-es-ES}"                                  # the game is in Spanish
export CAPTURE_GRADLE_ARGS="-PleagueCards.privateDir=/nonexistent"  # the sample deck, always
source "$(dirname "$0")/../../../scripts/capture_lib.sh"

on_screen() {
  "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || true
  [[ "$("$ADB" shell cat /sdcard/ui.xml)" == *"resource-id=\"$1\""* ]]
}

capture_start league-cards com.jorgelillo.leaguecards
for locale in $CAPTURE_LOCALES; do
  capture_open "$locale" 4
  tap new_season 3
  shot "$locale" 01-card
  tap joker 2
  shot "$locale" 02-joker
  chat=""; table=""
  for i in $(seq 1 150); do
    if on_screen next; then
      if [ -z "$chat" ]; then sleep 4; shot "$locale" 03-chat; chat=1; fi
      if [ -z "$table" ] && on_screen table; then tap table 2; shot "$locale" 04-table; back 1; table=1; fi
      tap next 2 || true
    elif on_screen choose_right; then
      if [ $((i % 2)) = 0 ]; then tap choose_left 2 || true; else tap choose_right 2 || true; fi
    elif on_screen ending; then
      break
    fi
  done
  sleep 2; shot "$locale" 05-ending
  tap trophies 2
  shot "$locale" 06-trophies
done
