#!/usr/bin/env bash
# Saves a screenshot of the connected device, closing "isn't responding" system dialogs first
# (the emulator shows them often while it is busy). Works in any orientation.
# Usage: scripts/screenshot.sh <output.png>
set -euo pipefail
exec python3 "$(dirname "$0")/ui_lib.py" screenshot "${1:?usage: screenshot.sh <output.png>}"
