#!/usr/bin/env bash
# Installs the release APK on the running emulator and checks it starts, shows every screen,
# plays music and does not crash. Run it before every Play upload: R8 only affects release builds.
#
# Usage: apps/christmas-countdown/tools/smoke_test.sh
set -euo pipefail
source "$(dirname "$0")/../../../scripts/smoke_lib.sh"
smoke_start christmas-countdown com.jorgelillo.christmascountdown 12
tap "Time"; shot countdown
tap "Sleeps"; shot sleeps
tap "Advent" 5; shot advent
tap "About" 5; shot about
back 2
tap "Countdown"
tap "Play carols" 6                                    # music on
pid="$("$ADB" shell pidof "$PKG" || true)"
audio="$("$ADB" shell dumpsys audio | grep "/$pid " | grep -c 'AudioTrack.*state:started' || true)"
tap "Stop carols" 2                                    # music off
smoke_finish "$([ "$audio" -gt 0 ] && echo 1 || echo 0)" "music playing: $([ "$audio" -gt 0 ] && echo yes || echo NO)"
