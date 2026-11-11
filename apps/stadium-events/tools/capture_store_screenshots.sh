#!/usr/bin/env bash
# Captures the raw Play Store screenshots (store/raw/<locale>/) for every locale, from the real
# feed: a match tomorrow, a quiet day, a LaLiga match with its day not fixed yet, and the 2027
# Champions League final. The app downloads the events once at the real date; then the emulator's
# clock is moved to each day (moving it first would break HTTPS with a far-off date). Needs a
# running emulator with internet; taps go by test tag. Then run:
#   .venv/bin/python apps/stadium-events/tools/generate_store_assets.py
#
# The dates follow the agenda published on 2026-11-11: if it changes, pick days that show the same.
#
# Usage: apps/stadium-events/tools/capture_store_screenshots.sh   (CAPTURE_DEVICE=tablet for tablets)
set -euo pipefail
source "$(dirname "$0")/../../../scripts/capture_lib.sh"

restart() {
  "$ADB" shell am force-stop "$PKG"
  "$ADB" shell am start -W -n "$PKG/.MainActivity" >/dev/null
  sleep "${1:-4}"
}

# shot_on <MMDDhhmmYYYY> <locale> <name>: the screen as it looks on that day.
shot_on() {
  capture_date "$1" >/dev/null
  restart 4
  shot "$2" "$3"
}

capture_start stadium-events com.jorgelillo.stadiumevents
first=1
for locale in $CAPTURE_LOCALES; do
  if [ "$first" = 1 ]; then
    # Clean install at the real date: grant notifications (no system dialog over the screen) and
    # let it download the feed.
    capture_open "$locale" 2
    "$ADB" shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS
    restart 2
    wait_for answer 120    # the first download on a fresh install can take a while
    first=0
  else
    capture_open "$locale" 4 keep    # same downloaded events, another language
  fi
  shot_on 112410002026 "$locale" 01-busy      # tomorrow: Atlético – Viking, Champions, 21:00
  shot_on 112610002026 "$locale" 02-calm      # tomorrow: nothing at the stadium
  shot_on 120410002026 "$locale" 03-maybe     # Betis: Saturday 5 or Sunday 6, not fixed yet
  shot_on 060410002027 "$locale" 04-final     # tomorrow: the Champions League final
done
