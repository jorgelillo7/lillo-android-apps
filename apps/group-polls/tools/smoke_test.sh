#!/usr/bin/env bash
# Installs the release APK on the running emulator and walks the main paths: name, public feed,
# vote + prediction, results, a new private poll (share sheet), its result with names, closing it
# and settings. Run it before every Play upload (R8 only affects release builds). Taps go by test
# tag, so any language works. Without firebase.properties it runs on the in-memory backend.
#
# Usage: apps/group-polls/tools/smoke_test.sh
set -euo pipefail
source "$(dirname "$0")/../../../scripts/smoke_lib.sh"
smoke_start group-polls com.jorgelillo.grouppolls

shot welcome
type_in name_input "Alex"
tap start 3
shot feed
# Open the first poll of the feed by its card (test tags start with poll_).
first="$("$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null && "$ADB" shell cat /sdcard/ui.xml | grep -o 'resource-id="poll_[A-Za-z0-9]*"' | head -1 | sed 's/resource-id="//;s/"//')"
tap "$first" 2
shot vote
tap vote_red 1
tap predict_blue 2
shot result
tap back 2

tap create 2
type_in question_input "Friday%sdinner"
type_in red_input "Pizza"
type_in blue_input "Sushi"
hide_keyboard
tap submit 3
shot share
back 1
tap vote_blue 1
tap predict_blue 2
shot private_result
tap close_poll 1
tap confirm_close 2
tap back 2
tap tab_mine 2
shot mine
tap settings 2
shot settings

smoke_finish
