#!/usr/bin/env bash
# Captures the raw Play Store screenshots (store/raw/<locale>/) for every locale, on the
# in-memory backend (sample polls; build without firebase.properties).
# Needs a running emulator; scripts/capture_lib.sh installs the app and does the rest.
# Then run: .venv/bin/python apps/group-polls/tools/generate_store_assets.py
#
# Usage: apps/group-polls/tools/capture_store_screenshots.sh   (LOCALES="es-ES" for one)
set -euo pipefail
source "$(dirname "$0")/../../../scripts/capture_lib.sh"
PRIVATE_LINK="https://jorgelillo7.github.io/q/?c=DemPrivateX2" # FakePollRepository.SAMPLE_PRIVATE


first_poll() {
  "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null
  "$ADB" shell cat /sdcard/ui.xml | grep -o 'resource-id="poll_[A-Za-z0-9]*"' | head -1 | sed 's/resource-id="//;s/"//'
}

capture_start group-polls com.jorgelillo.grouppolls
for locale in $CAPTURE_LOCALES; do
  case "$locale" in
    en-US) NAME=Alex; Q="Netflix%sor%sa%sboard%sgame?"; R=Netflix; B="Board%sgame" ;;
    es-ES) NAME=Pablo; Q="Peli%so%sjuego%sde%smesa"; R=Peli; B="Juego%sde%smesa" ;;
  esac
  capture_open "$locale" 3
  type_in name_input "$NAME"; tap start 3
  shot "$locale" 01-feed

  tap "$(first_poll)" 2
  shot "$locale" 02-vote
  tap vote_red 1
  shot "$locale" 03-predict
  tap predict_red 3
  shot "$locale" 04-result
  tap back 2

  "$ADB" shell am start -W -a android.intent.action.VIEW -d "$PRIVATE_LINK" "$PKG" >/dev/null
  sleep 2
  tap vote_blue 1; tap predict_red 3
  shot "$locale" 05-private
  tap back 2

  tap create 2
  type_in question_input "$Q"
  type_in red_input "$R"
  type_in blue_input "$B" hide
  shot "$locale" 06-create
done
