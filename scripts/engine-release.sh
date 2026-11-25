#!/usr/bin/env bash
# Builds an engine game's signed Play bundle on this Mac and uploads it with its `play/` module:
#   scripts/engine-release.sh engine/<game> [--track internal|production] [--draft] [--listing] [--dry-run]
# A game's bundle is built here, not in the Release workflow: it carries models from bought packs,
# which never leave this machine, and the slim engine template built from Godot's source. The
# workflow promotes the uploaded release and publishes the listing like for any app.
#
# Needs the game's keystore.properties (scripts/new-upload-key.sh), the models
# (scripts/engine-assets.sh) and the Play service account (~/.config/lillo-android-apps/). Run the
# game's tools/smoke_test.sh first. Tags the commit as <game>/v<versionCode> and pushes the tag.
set -euo pipefail

root="$(cd "$(dirname "$0")/.." && pwd)"
project="${1:?usage: scripts/engine-release.sh engine/<game> [--track internal|production] [--draft] [--listing] [--dry-run]}"
shift
track=internal
draft=false
listing=false
dry_run=false
while [ $# -gt 0 ]; do
  case "$1" in
    --track) track="$2"; shift ;;
    --draft) draft=true ;;
    --listing) listing=true ;;
    --dry-run) dry_run=true ;;
    *) echo "unknown option $1" >&2; exit 1 ;;
  esac
  shift
done
case "$track" in internal|production) ;; *) echo "--track is internal or production" >&2; exit 1 ;; esac

cd "$root"
game="$(basename "$project")"
module=":engine:$game:play"
[ -f "$project/keystore.properties" ] || { echo "$project/keystore.properties missing: the bundle must be signed with the upload key" >&2; exit 1; }
[ -z "$(git status --porcelain -- "$project")" ] || { echo "$project has uncommitted changes: release from a clean master" >&2; exit 1; }
code="$(sed -n 's/^version\/code=//p' "$project/export_presets.cfg")"

scripts/engine-assets.sh "$project"
scripts/engine-export.sh --aab "$project"
scripts/verify-bundle.sh "$project/export/$game.aab"
$dry_run && { echo "Dry run: $game versionCode $code built and checked, nothing uploaded"; exit 0; }

# shellcheck source=env.sh
source "$root/scripts/env.sh" >/dev/null
publish=("$module:publishReleaseBundle" --artifact-dir "$project/export" --track "$track")
$draft && publish+=(--release-status draft)
./gradlew "${publish[@]}"
if $listing; then
  ./gradlew "$module:publishReleaseListing"
fi

tag="$game/v$code"
git tag "$tag" && git push origin "$tag" || echo "warning: $tag already exists"
echo "$game versionCode $code uploaded to $track$($draft && echo ' as a draft')"
