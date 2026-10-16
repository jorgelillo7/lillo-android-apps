#!/usr/bin/env bash
# Installs the release APK on the running emulator and plays one full game: players, setup, the
# pass-the-phone reveal, countdown, a vote and the result. Run it before every Play upload (R8
# only affects release builds). Taps go by test tag, so any language works.
#
# Usage: apps/whos-lying/tools/smoke_test.sh
set -euo pipefail
source "$(dirname "$0")/../../../scripts/smoke_lib.sh"
smoke_start whos-lying com.jorgelillo.whoslying
sleep 5; tap welcome_ok 2   # first-launch welcome note (slow to get its tags on a cold release start)

shot home
tap play 3
for name in Ana Bea Carlos; do
  tap player_name 1; "$ADB" shell input text "$name"; "$ADB" shell input keyevent KEYCODE_ENTER; sleep 1
done
"$ADB" shell input keyevent KEYCODE_ESCAPE; sleep 1
shot players
tap continue 3
tap mode_classic 1
shot setup
tap start 3
for _ in 1 2 3; do
  sleep 1
  "$ADB" shell input swipe 540 1700 540 700 800  # slide the curtain up; it drops on release
  sleep 1; tap hide 1
done
shot countdown
sleep 5
shot debate
tap go_vote 2
# Vote for the first player on screen (the emulator keyboard sometimes doubles a letter of a name).
first_vote="$("$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null; "$ADB" shell cat /sdcard/ui.xml | grep -o 'resource-id="vote_[^"]*"' | head -1 | sed 's/resource-id="//;s/"//')"
tap "$first_vote" 1
shot vote
tap confirm_vote 1
sleep 1; shot expose
sleep 4
tap keep_playing 2 2>/dev/null || true      # only shown when the game goes on
tap reveal_all 3 2>/dev/null || true        # end it if it's still running
sleep 1; shot result

smoke_finish
