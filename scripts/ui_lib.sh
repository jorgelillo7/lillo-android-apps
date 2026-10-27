# Shell side of scripts/ui_lib.py, shared by every app's smoke test (smoke_lib.sh) and store
# screenshot capture (capture_lib.sh). Source it; taps go by text or test tag, never coordinates.
#
#   tap <text|tag> [wait]          type_in <tag> <text> [hide]   (%s for spaces in the text)
#   hide_keyboard                  swipe_up [start end] / swipe_down [start end]  (fractions of the height)
#   back [wait]
#   on_screen <tag>                true if an element with that test tag is on screen now
#   wait_for <tag> [seconds]       waits for it (default 90 s, once a second); false if it never comes
#   bounds <tag>                   its "left top right bottom" in screen pixels (empty if not there)
#   in_box "<bounds>" <x‰> <y‰>    a point inside those bounds, in thousandths: "x y" pixels. Gestures
#                                  measured on the element, not the screen, work on phones and tablets

# shellcheck disable=SC2034  # ROOT is used by the scripts that source this one
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$ROOT/scripts/env.sh"

tap() { "$ROOT/scripts/tap-text.sh" "$@"; }
hide_keyboard() { python3 "$ROOT/scripts/ui_lib.py" hide-keyboard; }
type_in() {
  tap "$1" 1
  "$ADB" shell input text "$2"
  if [ "${3:-}" = hide ]; then hide_keyboard; fi
}
swipe_up() { python3 "$ROOT/scripts/ui_lib.py" swipe-up "${1:-0.75}" "${2:-0.25}"; sleep 1; }
swipe_down() { swipe_up "${1:-0.25}" "${2:-0.75}"; }
back() { "$ADB" shell input keyevent KEYCODE_BACK; sleep "${1:-2}"; }
on_screen() {
  "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || true
  [[ "$("$ADB" shell cat /sdcard/ui.xml)" == *"resource-id=\"$1\""* ]]
}
wait_for() {
  local _
  for _ in $(seq 1 "${2:-90}"); do on_screen "$1" && return 0; sleep 1; done
  return 1
}
bounds() {
  "$ADB" shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || true
  "$ADB" shell cat /sdcard/ui.xml | grep -o "resource-id=\"$1\"[^>]*bounds=\"[^\"]*\"" | head -1 |
    sed -E 's/.*bounds="\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]".*/\1 \2 \3 \4/'
}
in_box() {
  local l t r b
  read -r l t r b <<<"$1"
  echo "$((l + (r - l) * $2 / 1000)) $((t + (b - t) * $3 / 1000))"
}
