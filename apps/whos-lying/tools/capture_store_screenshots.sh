#!/usr/bin/env bash
# Captures the raw Play Store screenshots (store/raw/<locale>/) for every locale.
# Needs a running emulator; scripts/capture_lib.sh installs the app and does the rest. The curtain
# and the drawing are measured on their elements (bounds/in_box), so phones and tablets both work.
# Then run: .venv/bin/python apps/whos-lying/tools/generate_store_assets.py
#
# Usage: apps/whos-lying/tools/capture_store_screenshots.sh   (LOCALES="es-ES" for one)
set -euo pipefail
source "$(dirname "$0")/../../../scripts/capture_lib.sh"

ui() { "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null && "$ADB" shell cat /sdcard/ui.xml; }

# ASCII names only: `adb shell input text` can't type accents (macOS bash 3.2: no associative arrays).
names() {
  case "$1" in
    en-US) PLAYERS=(Alex Sam Jordan Maya Leo) ;;
    es-ES) PLAYERS=(Pablo Marta Dani Sara Hugo) ;;
  esac
}

# Holds the curtain up (the word only shows while the finger is down), runs "$@", then lets go.
peek() {
  local box x y
  box="$(bounds tap_to_reveal)"
  read -r x y <<<"$(in_box "$box" 500 850)"
  "$ADB" shell input motionevent DOWN "$x" "$y"
  for f in 750 650 550 450 350; do
    read -r x y <<<"$(in_box "$box" 500 "$f")"
    "$ADB" shell input motionevent MOVE "$x" "$y"
  done
  sleep 0.5
  "$@"
  "$ADB" shell input motionevent UP "$x" "$y"
  sleep 1
}

# Empty when the player got no word (blind impostor, drawing mode).
secret_word() { ui | grep -o 'text="[^"]*" resource-id="secret_word"' | sed 's/text="\([^"]*\)".*/\1/' || true; }

# Every player looks at their card; WORDS[i] is what player i saw ("" if they got no word).
reveal_all() {
  local shoot="${1:-}"
  WORDS=()
  for i in "${!PLAYERS[@]}"; do
    sleep 1
    if [ "$i" = 1 ] && [ -n "$shoot" ]; then
      peek shot "$locale" 01-reveal
      peek true
    fi
    word=""
    capture() { word="$(secret_word)"; }
    peek capture
    WORDS+=("$word")
    tap hide 2
  done
}

# The player whose word differs from everyone else's (classic mode, one impostor).
impostor() {
  for i in "${!WORDS[@]}"; do
    same=0
    for w in "${WORDS[@]}"; do [ "$w" = "${WORDS[$i]}" ] && same=$((same + 1)); done
    [ "$same" = 1 ] && { echo "${PLAYERS[$i]}"; return; }
  done
  echo "${PLAYERS[0]}"
}

# A little house with a sun, drawn on the shared canvas (thousandths of the canvas).
stroke() {
  local x1 y1 x2 y2
  read -r x1 y1 <<<"$(in_box "$CANVAS" "$1" "$2")"
  read -r x2 y2 <<<"$(in_box "$CANVAS" "$3" "$4")"
  "$ADB" shell input swipe "$x1" "$y1" "$x2" "$y2" "$5"
}
draw_house() {
  CANVAS="$(bounds canvas)"
  stroke 285 705 285 457 300     # walls
  stroke 285 457 716 457 300
  stroke 716 457 716 705 300
  stroke 285 705 716 705 300
  tap ink_6 1
  stroke 254 465 501 284 300     # roof
  stroke 501 284 747 465 300
  tap ink_4 1
  stroke 459 705 459 581 200     # door
  stroke 459 581 542 581 200
  stroke 542 581 542 705 200
  tap ink_1 1
  for d in "-72 0" "72 0" "0 -58" "0 58" "-51 -41" "51 41" "-51 41" "51 -41"; do   # sun
    set -- $d
    stroke 254 333 $((254 + $1)) $((333 + $2)) 150
  done
}

capture_start whos-lying com.jorgelillo.whoslying
for locale in $CAPTURE_LOCALES; do
  names "$locale"
  capture_open "$locale" 4
  tap welcome_ok 2   # first-launch welcome note
  shot "$locale" 07-home

  tap play 2
  for name in "${PLAYERS[@]}"; do
    tap player_name 1; "$ADB" shell input text "$name"; "$ADB" shell input keyevent KEYCODE_ENTER; sleep 1
  done
  hide_keyboard
  tap continue 2
  swipe_up 0.74 0.2    # scroll setup down to the timer
  for _ in 1 2 3 4 5; do tap timer_plus 1; done      # 5 min, the recommendation for 5 players
  tap choose_packs 2
  shot "$locale" 06-packs
  tap packs_done 2

  # Classic game: reveal, debate, vote the impostor out, result.
  tap start 3
  reveal_all shoot
  sleep 6
  shot "$locale" 02-debate
  tap go_vote 2
  target="$(impostor)"
  tap "vote_$target" 1
  shot "$locale" 03-vote
  tap confirm_vote 1
  sleep 3.5                                         # "Exposed in 3, 2, 1…" then the result
  shot "$locale" 04-result

  # Drawing mode.
  tap home 2
  tap play 2
  tap continue 2
  swipe_up 0.74 0.2
  tap drawing 1
  tap start 3
  reveal_all
  sleep 6
  draw_house
  sleep 1
  shot "$locale" 05-drawing
done
