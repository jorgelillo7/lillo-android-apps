#!/usr/bin/env bash
# Installs the release APK on the running emulator and plays a 4-player knockout end to end: new
# tournament, results (with a comment), champion, bracket, share sheet with the QR code, image
# export, hall of fame. Run it before every Play upload (R8 only affects release builds). Taps go
# by test tag, so any language works.
#
# Usage: apps/tournaments/tools/smoke_test.sh
set -euo pipefail
source "$(dirname "$0")/../../../scripts/smoke_lib.sh"
smoke_start tournaments com.jorgelillo.tournaments
# Seed order for 4 players: slot 0 = seeds 1-4, slot 1 = seeds 2-3.
result() { tap "$1" 3; tap "$2" 1; tap "$3" 1; tap save 3; }

shot home
tap new 2
type_in name "Smoke%scup" hide
tap player 1
"$ADB" shell input text "Ann,Bob,Cat,Dan"
"$ADB" shell input keyevent KEYCODE_ENTER; sleep 1
hide_keyboard
shot setup
tap start 3
tap tab_matches 2
tap match_elimination_1_0 3; tap winner_a 1; tap score_1 1
type_in comment "20%slives%sto%s0" hide
tap save 3
result match_elimination_1_1 winner_b score_0
result match_elimination_2_0 winner_a score_1
shot champion
tap tab_bracket 2
shot bracket
tap share 3
shot share
back 1
tap back 2
tap stats 2
shot stats

champion="$("$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null; "$ADB" shell cat /sdcard/ui.xml | grep -c 'leaderboard' || true)"
smoke_finish "$([ "$champion" -ge 1 ] && echo 1 || echo 0)" "hall of fame shown: $champion"
