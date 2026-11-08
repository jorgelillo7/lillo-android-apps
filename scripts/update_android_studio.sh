#!/usr/bin/env bash
# Updates Android Studio to the latest stable release, straight from Google's release list.
# Checks the download's SHA-256 and Google's signature before touching the installed app, and
# moves the old copy to the Trash so it can be restored. Android Studio must be closed.
#
#   scripts/update_android_studio.sh
set -euo pipefail

APP="/Applications/Android Studio.app"
GOOGLE_TEAM="EQHXZ8M8AV"
FEED="https://jb.gg/android-studio-releases-list.json"

if pgrep -f "$APP/Contents/MacOS/studio" >/dev/null; then
  echo "Android Studio is running: close it and try again." >&2
  exit 1
fi

installed=$(/usr/libexec/PlistBuddy -c 'Print CFBundleVersion' "$APP/Contents/Info.plist")
old_version=$(/usr/libexec/PlistBuddy -c 'Print CFBundleShortVersionString' "$APP/Contents/Info.plist")

# One tab-separated line (name, link, sha256), or nothing if the installed build is current.
release=$(curl -fsSL --max-time 60 "$FEED" | INSTALLED="$installed" python3 -c '
import json, os, re, sys
numbers = lambda build: [int(part) for part in re.findall(r"\d+", build)]
items = json.load(sys.stdin)["content"]["item"]
stable = next(item for item in items if item.get("channel") in ("Release", "Patch"))
if numbers(os.environ["INSTALLED"]) < numbers(stable["build"]):
    download = next(d for d in stable["download"] if "mac_arm" in d["link"])
    print(stable["name"].replace("\t", " "), download["link"], download["checksum"], sep="\t")
')

if [ -z "$release" ]; then
  echo "Android Studio is up to date ($installed)."
  exit 0
fi
IFS=$'\t' read -r name link checksum <<<"$release"
echo "Updating to $name"

work=$(mktemp -d)
mount="$work/mount"
mkdir "$mount"
cleanup() {
  hdiutil detach "$mount" -quiet 2>/dev/null || true
  rm -rf "$work"
}
trap cleanup EXIT

curl -fsSL --retry 2 -o "$work/studio.dmg" "$link"
got=$(shasum -a 256 "$work/studio.dmg" | cut -d' ' -f1)
if [ "$got" != "$checksum" ]; then
  echo "Checksum mismatch: expected $checksum, got $got" >&2
  exit 1
fi

hdiutil attach "$work/studio.dmg" -nobrowse -readonly -mountpoint "$mount" -quiet
new="$mount/Android Studio.app"
codesign --verify --deep --strict "$new"
codesign -dvv "$new" 2>&1 | grep -q "TeamIdentifier=$GOOGLE_TEAM"
spctl -a -t exec "$new"

trash="$HOME/.Trash/Android Studio $old_version $(date +%Y%m%d%H%M%S).app"
mv "$APP" "$trash"
if ! ditto "$new" "$APP"; then
  rm -rf "$APP"
  mv "$trash" "$APP"
  echo "Install failed: restored the previous version." >&2
  exit 1
fi

codesign --verify --deep --strict "$APP"
echo "Installed $name. The previous version is in the Trash."
