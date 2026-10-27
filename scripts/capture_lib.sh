# Shared frame of every app's tools/capture_store_screenshots.sh: raw Play Store screenshots,
# upright, demo status bar (fixed clock, full battery). Source it, then:
#
#   capture_start <app-folder> <package>   (CAPTURE_DEVICE=tablet: as a 10" tablet) clean install of the debug build (a release build from a
#                                          smoke test has another signature), demo mode on; on exit,
#                                          even after a failed step: the
#                                          app's language back to the phone's, demo off, clock back
#                                          (CAPTURE_GRADLE_ARGS: extra arguments for that build)
#   capture_date MMDDhhmmYYYY              optional: a fixed date (scripts/emulator.sh date)
#   for locale in $CAPTURE_LOCALES; do     en-US es-ES, or LOCALES="es-ES" to redo one
#     capture_open "$locale" [wait] [keep] clean data (not with keep), the locale, launch
#     ... tap / type_in / swipe_up / back / shot "$locale" <name>, the app's own steps ...
#   done
#
# Then tools/generate_store_assets.py frames them into the listing graphics.

source "$(dirname "${BASH_SOURCE[0]}")/ui_lib.sh"   # ROOT, tap, type_in, hide_keyboard, swipe_up, back

CAPTURE_LOCALES="${LOCALES:-en-US es-ES}"

# CAPTURE_DEVICE=tablet: the emulator turned into a 10" landscape tablet (2560x1600, 320 dpi) for
# the listing's tablet screenshots, into store/raw-tablet/ (put back on exit).
CAPTURE_DEVICE="${CAPTURE_DEVICE:-phone}"

# shot <locale> <name>: store/raw/<locale>/<name>.png
shot() { "$ROOT/scripts/screenshot.sh" "$RAW/$1/$2.png" >/dev/null; echo "shot $1/$2"; }

capture_start() {
  APP="$1"
  PKG="$2"
  RAW="$ROOT/apps/$APP/store/raw"
  if [ "$CAPTURE_DEVICE" = tablet ]; then
    RAW="$ROOT/apps/$APP/store/raw-tablet"
    "$ADB" shell wm size 2560x1600
    "$ADB" shell wm density 320
  fi
  "$ADB" uninstall "$PKG" >/dev/null 2>&1 || true
  # CAPTURE_GRADLE_ARGS: extra Gradle arguments for the build captured (league-cards: the sample deck).
  # shellcheck disable=SC2086
  (cd "$ROOT" && ./gradlew -q ":apps:$APP:app:installDebug" ${CAPTURE_GRADLE_ARGS:-})
  trap capture_finish EXIT
  "$ROOT/scripts/emulator.sh" demo on
}

capture_date() {
  "$ROOT/scripts/emulator.sh" date "$1"
  CAPTURE_DATE_SET=1
}

capture_open() {
  local locale="$1" wait="${2:-4}"
  [ "${3:-}" = keep ] || "$ADB" shell pm clear "$PKG" >/dev/null
  "$ADB" shell cmd locale set-app-locales "$PKG" --locales "$locale"
  "$ADB" shell am force-stop "$PKG"
  "$ADB" shell am start -W -n "$PKG/.MainActivity" >/dev/null
  sleep "$wait"
}

capture_finish() {
  if [ "$CAPTURE_DEVICE" = tablet ]; then "$ADB" shell wm size reset; "$ADB" shell wm density reset; fi
  "$ADB" shell cmd locale set-app-locales "$PKG" --locales ""
  "$ROOT/scripts/emulator.sh" demo off
  if [ -n "${CAPTURE_DATE_SET:-}" ]; then "$ROOT/scripts/emulator.sh" date auto; fi
  echo "Raw screenshots in $RAW. Check them before generating the store graphics."
}
