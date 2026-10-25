#!/usr/bin/env bash
# Installs the release APK on the running emulator and plays three runs: one with a few jumps, one
# without (a few slippers catch the kid), and the run of the day, each to the result screen. Run it
# before every Play upload (R8 only affects release builds). Taps go by test tag.
#
# Usage: apps/slipper/tools/smoke_test.sh
set -euo pipefail
source "$(dirname "$0")/../../../scripts/smoke_lib.sh"
smoke_start slipper com.jorgelillo.slipper

shot home
tap play 1
# A few jumps by tapping the middle of the screen; the run ends by itself sooner or later.
for _ in 1 2 3; do "$ADB" shell input tap 540 1500; sleep 0.6; done
shot run
wait_for final_score
shot result
tap retry 1
wait_for final_score
shot result_2
tap home 2
tap daily 1
wait_for final_score
shot result_daily
tap home 2
shot home_best
best="$("$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null; "$ADB" shell cat /sdcard/ui.xml | grep -c 'resource-id="best"' || true)"
smoke_finish "$([ "$best" -ge 1 ] && echo 1 || echo 0)" "best score shown: $best"
