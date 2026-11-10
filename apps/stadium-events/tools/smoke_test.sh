#!/usr/bin/env bash
# Installs the release APK on the running emulator and checks the one journey: the app downloads
# the real events file from jorgelillo7.github.io and answers tomorrow (busy or quiet, whatever the
# real agenda says), then the About sheet opens and closes. Run it before every Play upload (R8 only
# affects release builds). Taps go by test tag (testTagsAsResourceId), so any language works.
# Needs internet on the emulator.
#
# The notifications permission is granted with adb and the app restarted, so the system dialog the
# first launch shows never covers the screen (on CI's ATD image it renders black).
#
# Usage: apps/stadium-events/tools/smoke_test.sh
set -euo pipefail
source "$(dirname "$0")/../../../scripts/smoke_lib.sh"
smoke_start stadium-events com.jorgelillo.stadiumevents

"$ADB" shell pm grant "$PKG" android.permission.POST_NOTIFICATIONS
"$ADB" shell am force-stop "$PKG"
"$ADB" shell am start -W -n "$PKG/.MainActivity" >/dev/null
sleep 4

answered=0
if wait_for answer 30; then answered=1; fi
shot answer
tap about 3; shot about
back 2; shot back

smoke_finish "$answered" "answer shown: $answered"
