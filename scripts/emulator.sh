#!/usr/bin/env bash
# Starts, stops and prepares the Android emulator for testing and store screenshots.
#
#   scripts/emulator.sh start [--headless] [avd]
#                                          boot (default AVD: Pixel_9_API_36) and wait until ready;
#                                          --headless: no window (smoke tests, screenshots still work)
#   scripts/emulator.sh stop               shut the emulator down
#   scripts/emulator.sh date MMDDhhmmYYYY  freeze the clock at a date (e.g. 121210002026 = 12 Dec 2026 10:00)
#   scripts/emulator.sh date auto          restore the automatic clock
#   scripts/emulator.sh demo on|off        clean status bar for screenshots (12:25, full battery and signal)
#
# The machine has little RAM: close Chrome before booting, and stop the emulator before long builds.
# The emulator draws with the Mac's GPU (-gpu host): software rendering kept the CPU busy and made
# System UI stop responding.
set -euo pipefail
source "$(dirname "$0")/env.sh"

wait_boot() {
  "$ADB" wait-for-device
  until [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; do sleep 3; done
}

demo() { "$ADB" shell am broadcast -a com.android.systemui.demo -e command "$@" >/dev/null; }

case "${1:-}" in
  start)
    shift
    window=()
    if [ "${1:-}" = --headless ]; then window=(-no-window); shift; fi
    avd="${1:-Pixel_9_API_36}"
    nohup "$EMULATOR" -avd "$avd" -no-boot-anim -no-snapshot-save -gpu host ${window[@]+"${window[@]}"} >/tmp/emulator.log 2>&1 &
    wait_boot
    echo "Emulator ready ($avd)"
    ;;
  stop)
    "$ADB" emu kill >/dev/null 2>&1 || true
    ;;
  date)
    "$ADB" root >/dev/null; sleep 2; "$ADB" wait-for-device
    if [ "${2:-}" = auto ]; then
      "$ADB" shell settings put global auto_time 1
    else
      "$ADB" shell settings put global auto_time 0
      "$ADB" shell date "${2}.00" >/dev/null
    fi
    "$ADB" shell date
    ;;
  demo)
    if [ "${2:-on}" = on ]; then
      "$ADB" shell settings put global sysui_demo_allowed 1
      demo enter
      demo clock -e hhmm 1225
      demo battery -e level 100 -e plugged false
      demo network -e wifi show -e level 4 -e mobile show -e datatype none -e level 4
      demo notifications -e visible false
    else
      demo exit
    fi
    ;;
  *)
    sed -n '2,15p' "$0"; exit 1
    ;;
esac
