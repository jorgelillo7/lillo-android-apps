#!/usr/bin/env bash
# Installs the release APK on the running emulator and plays a whole season: new season, cards
# answered with the buttons (and the joker once), the matchday group chat, the table, the ending
# and the trophies (endings, achievements, history).
# Run it before every Play upload (R8 only affects release builds: it would break the JSON deck).
# Taps go by test tag; a failed tap inside the loop is retried by the next round (UI dumps are
# flaky on a loaded emulator).
#
# Usage: apps/league-cards/tools/smoke_test.sh
set -euo pipefail
source "$(dirname "$0")/../../../scripts/smoke_lib.sh"
smoke_start league-cards com.jorgelillo.leaguecards

shot home
tap new_season 3
shot card
tap joker 2
shot joker
reports=0
for i in $(seq 1 120); do
  if on_screen next; then
    reports=$((reports + 1))
    [ "$reports" = 1 ] && { sleep 3; shot chat; }
    tap next 2 || true
  elif on_screen choose_right; then
    if [ $((i % 2)) = 0 ]; then tap choose_left 2 || true; else tap choose_right 2 || true; fi
  elif on_screen ending; then
    break
  fi
  if [ "$reports" = 3 ] && [ "${table_shot:-}" = "" ] && on_screen table; then
    table_shot=1; tap table 2; shot table; back 1
  fi
done
ended=0
if on_screen ending; then
  ended=1; shot ending
  tap trophies 2; shot trophies
fi
smoke_finish "$ended" "matchdays played: $reports, season ended: $ended"
