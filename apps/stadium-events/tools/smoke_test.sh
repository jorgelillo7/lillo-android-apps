#!/usr/bin/env bash
# Installs the release APK on the running emulator and checks the one journey: the app asks for
# notifications once, downloads the real events file from jorgelillo7.github.io and answers
# tomorrow (busy or quiet, whatever the real agenda says), then the About sheet opens and closes.
# Run it before every Play upload (R8 only affects release builds). Taps go by test tag
# (testTagsAsResourceId), so any language works. Needs internet on the emulator.
#
# Usage: apps/stadium-events/tools/smoke_test.sh
set -euo pipefail
source "$(dirname "$0")/../../../scripts/smoke_lib.sh"
smoke_start stadium-events com.jorgelillo.stadiumevents

# First launch asks for the notifications permission (Android 13+): allow it.
tap "Allow" 3 || true
answered=0
if wait_for answer 30; then answered=1; fi
shot answer
tap about 3; shot about
back 2; shot back

smoke_finish "$answered" "answer shown: $answered"
