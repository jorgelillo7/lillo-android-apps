#!/usr/bin/env bash
# Taps the on-screen element whose text, content description or test tag matches exactly (any
# app, any language), instead of fixed coordinates that break when the layout or locale changes.
# Compose test tags show up as resource ids when the app sets testTagsAsResourceId = true.
# Usage: scripts/tap-text.sh "<text or tag>" [wait-seconds-after, default 4]
set -euo pipefail
exec python3 "$(dirname "$0")/ui_lib.py" tap "${1:?usage: tap-text.sh <text> [wait]}" "${2:-4}"
