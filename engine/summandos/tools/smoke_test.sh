#!/usr/bin/env bash
# Exports the release APK (slim engine, arm64), installs it clean on the running emulator and plays
# through the real screens: menu, Recruit's level map, level 1 to its result card, "Again", the
# level map again, then the menu turned sideways. Fails on a crash, the process gone, or any
# error Godot logs (a script error, a missing resource the export left out). Run it before every
# Play upload: the editor and the tests never run the exported build.
#
# Godot draws everything itself (no accessibility tree to tap by text) and its launcher passes no
# command-line options, so taps go by position, as fractions of the screen, on the upright
# 1080x2424 layout of the Pixel_9_API_37 AVD (scripts/emulator.sh). An arm64 emulator is needed.
#
# Usage: engine/summandos/tools/smoke_test.sh [--no-export]
set -euo pipefail
source "$(dirname "$0")/../../../scripts/smoke_lib.sh"   # ROOT, ADB, shot, smoke_finish
PKG=com.jorgelillo.summandos
OUT="$ROOT/build/smoke-test/summandos"
APK="$ROOT/engine/summandos/export/summandos.apk"
mkdir -p "$OUT"

[ "${1:-}" = "--no-export" ] || "$ROOT/scripts/engine-export.sh" engine/summandos >/dev/null
# Godot's export restarts adb, which leaves the emulator offline for a few seconds.
"$ADB" wait-for-device
until [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; do sleep 1; done
read -r width height < <("$ADB" shell wm size | sed -n 's/.*: \([0-9]*\)x\([0-9]*\)/\1 \2/p' | tail -1)
# Taps a point given as fractions of the upright screen.
at() { "$ADB" shell input tap "$(awk "BEGIN{print int($1*$width)}")" "$(awk "BEGIN{print int($2*$height)}")"; sleep "${3:-3}"; }

"$ADB" uninstall "$PKG" >/dev/null 2>&1 || true
"$ADB" install -r "$APK" | tail -1
"$ADB" shell settings put system accelerometer_rotation 0
"$ADB" shell settings put system user_rotation 0
"$ADB" logcat -c
"$ADB" shell am start -W -n "$PKG/com.godot.game.GodotAppLauncher" >/dev/null
sleep 8
shot menu
at 0.50 0.45          # Recruit
shot levels
at 0.27 0.24 4        # level 1
shot run
# A drag steers: to the right, through level 1's first gate (+17 beats ÷2).
"$ADB" shell input swipe "$((width / 2))" "$((height * 3 / 4))" "$((width * 2 / 3))" "$((height * 3 / 4))" 400
sleep 45              # long enough for level 1 to end, won or lost
shot result
at 0.50 0.585 4       # Again (the first button on the card)
shot run_again
sleep 45
shot result_again
at 0.50 0.64 3        # the second button: Levels on a lost card, Again on a won one
"$ADB" shell input keyevent KEYCODE_BACK; sleep 2   # once, back to the menu (on the menu it leaves the game)
shot after_result
"$ADB" shell settings put system user_rotation 1
sleep 3
shot menu_sideways
"$ADB" shell settings put system user_rotation 0
sleep 2

errors="$("$ADB" logcat -d | grep -cE "(E|W) godot +: (ERROR|SCRIPT ERROR|USER ERROR|USER SCRIPT ERROR)" || true)"
[ "$errors" = 0 ] || "$ADB" logcat -d | grep -E "(E|W) godot +: " | head -20
smoke_finish "$([ "$errors" = 0 ] && echo 1 || echo 0)" "Godot errors logged: $errors"
