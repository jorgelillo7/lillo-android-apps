#!/usr/bin/env bash
# Captures the raw Play Store screenshots (store/raw/<locale>/) for every locale: an 8-player
# Magic knockout played to the end, the result sheet, the bracket, a Swiss league's standings,
# the share sheet with the QR code and the hall of fame. Needs a running emulator; taps go by
# test tag. Then run: .venv/bin/python apps/tournaments/tools/generate_store_assets.py
#
# Usage: apps/tournaments/tools/capture_store_screenshots.sh
set -euo pipefail
source "$(dirname "$0")/../../../scripts/capture_lib.sh"


players() {
  # In Swiss the rounds and top-cut rows push the field below the fold.
  swipe_up 0.70 0.25
  tap player 1; "$ADB" shell input text "$1"
  "$ADB" shell input keyevent KEYCODE_ENTER; sleep 1; back 1
}
# result <match tag> <winner_a|winner_b> <loser games> [comment]
result() {
  tap "$1" 3; tap "$2" 1; tap "score_$3" 1
  if [ -n "${4:-}" ]; then type_in comment "$4" hide; fi
  tap save 3
}

capture_start tournaments com.jorgelillo.tournaments
for locale in $CAPTURE_LOCALES; do
  case "$locale" in
    en-US) CUP="Friday%sMagic"; LEAGUE="Office%sping-pong"; COMMENT="20%slife%sto%s0"
           P8="Alex,Sam,Jordan,Taylor,Morgan,Casey,Riley,Jamie"; P6="Chris,Robin,Max,Kim,Lee,Pat" ;;
    es-ES) CUP="Viernes%sde%sMagic"; LEAGUE="Ping-pong%sde%sla%soficina"; COMMENT="20%svidas%sa%s0"
           P8="Pepe,Luis,Ana,Marta,Javi,Sara,Diego,Lucia"; P6="Pablo,Elena,Carlos,Irene,Jorge,Nuria" ;;
  esac
  capture_open "$locale" 4

  # Swiss league first, so the hall of fame later has two tournaments.
  tap new 2; type_in name "$LEAGUE" hide; tap "Ping-pong" 1; tap format_1 1
  players "$P6"; tap start 3
  result match_swiss_1_0 winner_a 1; result match_swiss_1_1 winner_b 0; result match_swiss_1_2 winner_a 1
  tap next_round 3
  result match_swiss_2_0 winner_b 1; result match_swiss_2_1 winner_a 0; result match_swiss_2_2 winner_a 1
  tap tab_standings 2
  shot "$locale" 04-standings
  tap back 2

  # 8-player knockout. Round-1 slots follow the seed order: 0 (1-8), 1 (4-5), 2 (2-7), 3 (3-6).
  tap new 2; type_in name "$CUP" hide; tap Magic 1
  players "$P8"; tap start 3
  tap tab_matches 2
  result match_elimination_1_0 winner_a 1; result match_elimination_1_1 winner_b 0
  result match_elimination_1_2 winner_a 0; result match_elimination_1_3 winner_b 1
  result match_elimination_2_0 winner_a 1; result match_elimination_2_1 winner_b 1
  # The final: capture the sheet with the sentence preview and the comment before saving.
  swipe_up 0.74 0.29
  tap match_elimination_3_0 3; tap winner_a 1; tap score_1 1; type_in comment "$COMMENT" hide
  shot "$locale" 02-result
  tap save 4
  swipe_down 0.29 0.74
  shot "$locale" 01-champion
  # Zoom in twice (semifinals, final and part of the quarters fill the width), centre on the final.
  tap tab_bracket 2; tap zoom_in 1; tap zoom_in 1
  "$ADB" shell input swipe 700 900 460 900 1500; sleep 1
  shot "$locale" 03-bracket
  tap share 3
  shot "$locale" 05-share
  back 1
  tap back 2
  tap stats 2
  shot "$locale" 06-stats
done
