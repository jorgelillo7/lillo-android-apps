#!/usr/bin/env bash
# Installs the release APK on the running emulator and plays the first matchdays: new season,
# cards answered with the buttons (and the joker once), the matchday group chat, the table.
# Run it before every Play upload (R8 only affects release builds: it would break the JSON deck).
# Taps go by test tag; a failed tap inside the loop is retried by the next round (UI dumps are
# flaky on a loaded emulator).
#
# Usage: apps/league-cards/tools/smoke_test.sh
set -euo pipefail
source "$(dirname "$0")/../../../scripts/smoke_lib.sh"
smoke_start league-cards com.jorgelillo.leaguecards
on_screen() {
  "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || true
  local ui
  ui="$("$ADB" shell cat /sdcard/ui.xml)"
  [[ "$ui" == *"resource-id=\"$1\""* ]]
}

shot home
tap new_season 3
shot card
tap joker 2
shot joker
reports=0
for i in $(seq 1 16); do
  if on_screen next; then
    reports=$((reports + 1))
    [ "$reports" = 1 ] && { sleep 3; shot chat; }
    tap next 2 || true
  elif on_screen choose_right; then
    if [ $((i % 2)) = 0 ]; then tap choose_left 2 || true; else tap choose_right 2 || true; fi
  else
    break
  fi
done
if on_screen table; then tap table 2; shot table; "$ADB" shell input keyevent KEYCODE_BACK; sleep 1; fi
ended=0
if on_screen ending; then ended=1; shot ending; fi
smoke_finish "$([ "$reports" -ge 2 ] || [ "$ended" = 1 ] && echo 1 || echo 0)" "matchdays played: $reports"
