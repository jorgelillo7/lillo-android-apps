#!/usr/bin/env bash
# Checks a release bundle before uploading it to Play: signing certificate, version, permissions
# and the baseline profile.
# Usage: scripts/verify-bundle.sh <path/to/app-release.aab>
set -euo pipefail
source "$(dirname "$0")/env.sh"
aab="${1:?usage: verify-bundle.sh <app-release.aab>}"

echo "== File"
ls -lh "$aab" | awk '{print $5, $9}'

# keytool is forced to English: JDK 25 keytool crashes formatting some localized (e.g. Spanish) messages.
echo "== Signing certificate (must match the upload key registered in Play Console)"
"$JAVA_HOME/bin/keytool" -J-Duser.language=en -printcert -jarfile "$aab" | grep -E "Owner|Serial|SHA1:|SHA256:" || {
  echo "NOT SIGNED: run scripts/new-upload-key.sh <app> (docs/signing.md)"; exit 1; }

# The bundle manifest is protobuf: repack it as a minimal APK so aapt2 can print it.
aapt2="$(ls -d "$ANDROID_HOME"/build-tools/*/aapt2 | sort -V | tail -1)"
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT
unzip -q -j "$aab" base/manifest/AndroidManifest.xml base/resources.pb -d "$tmp"
(cd "$tmp" && zip -q manifest.apk AndroidManifest.xml resources.pb)
manifest="$("$aapt2" dump xmltree --file AndroidManifest.xml "$tmp/manifest.apk")"

echo "== Version"
echo "$manifest" | grep -oE '(versionCode|versionName)\([^)]*\)=[^ ]+' | sed -E 's/\([^)]*\)//'

echo "== Permissions"
echo "$manifest" | grep -A1 'E: uses-permission' | grep -oE '"[^"]+"' | tr -d '"' | sort -u

echo "== Native libraries (Play warns about missing debug symbols if any)"
unzip -l "$aab" | awk '/\.so$/ {print $4}' | sed 's#.*/##' | sort -u

echo "== Baseline profile (precompiled startup; scripts/generate-baseline-profiles.sh)"
# grep without -q: with pipefail, -q's early exit fails unzip (SIGPIPE) and the check reads "none".
if unzip -l "$aab" | grep "BUNDLE-METADATA/com.android.tools.build.profiles/baseline.prof" >/dev/null; then
  echo "included"
else
  echo "none (apps with a baselineprofile module should have one)"
fi
