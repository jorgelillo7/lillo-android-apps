#!/usr/bin/env bash
# Regenerates the baseline profile of one app, or of every app with a baselineprofile module, on the
# running emulator or connected phone (Android 13+). Uninstalls the app first: an installed debug or
# smoke-test build has another signature, and Gradle then reports success with an empty profile.
# Fails if the profile comes out empty. Commit apps/<app>/app/src/release/generated/baselineProfiles/.
#
# Usage: scripts/generate-baseline-profiles.sh <app|all>
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
source "$ROOT/scripts/env.sh" >/dev/null
cd "$ROOT"

if [ "${1:-}" = all ]; then
  apps=$(for d in apps/*/baselineprofile; do basename "$(dirname "$d")"; done)
elif [ -d "apps/${1:-none}/baselineprofile" ]; then
  apps=$1
else
  echo "usage: $0 <app|all> (apps with a baselineprofile module: $(ls -d apps/*/baselineprofile | cut -d/ -f2 | tr '\n' ' '))" >&2
  exit 1
fi

for app in $apps; do
  id=$(sed -nE 's/.*applicationId = "([^"]+)".*/\1/p' "apps/$app/app/build.gradle.kts")
  "$ADB" uninstall "$id" >/dev/null 2>&1 || true
  "$ADB" uninstall "$id.baselineprofile" >/dev/null 2>&1 || true
  profile="apps/$app/app/src/release/generated/baselineProfiles/baseline-prof.txt"
  rm -f "$profile"
  ./gradlew ":apps:$app:app:generateBaselineProfile" --console=plain -q
  rules=$(grep -c "$id" "$profile" 2>/dev/null || true)
  if [ "${rules:-0}" -eq 0 ]; then
    echo "$app: no profile rules for $id (see the Gradle output above)" >&2
    exit 1
  fi
  echo "$app: $(wc -l <"$profile" | tr -d ' ') rules, $rules of the app's own"
done
