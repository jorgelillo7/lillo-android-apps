# Shared start and finish of every app's tools/smoke_test.sh. Source it, then:
#
#   smoke_start <app-folder> <package> [wait-seconds]   build + clean install of the RELEASE apk
#   ... tap / type_in / swipe_up / back / shot / $ADB, the app's own steps ...
#   smoke_finish [extra-check-ok] [extra-message]       crashes + process alive → PASSED/FAILED
#
# Release only: R8 only affects release builds. The app is uninstalled first (a debug build on the
# emulator has another signature) and runs in English, so the steps can tap English labels.

# shellcheck disable=SC2034  # OUT/PKG are used by the calling script
source "$(dirname "${BASH_SOURCE[0]}")/ui_lib.sh"   # ROOT, tap, type_in, hide_keyboard, swipe_up, back

shot() { "$ROOT/scripts/screenshot.sh" "$OUT/$1.png" >/dev/null; echo "screen: $1"; }

smoke_start() {
  local app="$1"
  PKG="$2"
  OUT="$ROOT/build/smoke-test/$app"
  (cd "$ROOT" && ./gradlew -q ":apps:$app:app:assembleRelease")
  "$ADB" uninstall "$PKG" >/dev/null 2>&1 || true
  "$ADB" install -r "$ROOT/apps/$app/app/build/outputs/apk/release/app-release.apk" | tail -1
  "$ADB" shell cmd locale set-app-locales "$PKG" --locales en-US
  "$ADB" logcat -c
  "$ADB" shell am start -W -n "$PKG/.MainActivity" >/dev/null
  sleep "${3:-4}"
}

smoke_finish() {
  local extra_ok="${1:-1}" extra_msg="${2:-}"
  local crashes pid
  crashes="$("$ADB" logcat -d -b crash | grep -c "Process: $PKG" || true)"   # this app's, not uiautomator's
  pid="$("$ADB" shell pidof "$PKG" || true)"
  "$ADB" shell cmd locale set-app-locales "$PKG" --locales ""
  [ -n "$extra_msg" ] && echo "$extra_msg"
  echo "crashes: $crashes"
  echo "screenshots: $OUT"
  if [ -n "$pid" ] && [ "$crashes" = 0 ] && [ "$extra_ok" = 1 ]; then
    echo "SMOKE TEST PASSED"
  else
    echo "SMOKE TEST FAILED"
    exit 1
  fi
}
