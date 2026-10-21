#!/usr/bin/env bash
# Puts what the Release workflow (.github/workflows/release.yml) needs into GitHub, from this Mac.
# Run it in your own terminal: values go to `gh secret set` through stdin, so they never appear in
# arguments, shell history, the process list or a chat.
#
# Usage:
#   scripts/set-release-secrets.sh <app-folder>    the app's upload key (apps/<app>/keystore.properties)
#                                                  into the GitHub environment named like the app;
#                                                  league-cards also gets its private league deck,
#                                                  group-polls its firebase.properties
#   scripts/set-release-secrets.sh --play          the Play service account key
#                                                  (~/.config/lillo-android-apps/play-service-account.json)
#   scripts/set-release-secrets.sh --approval      the "production" environment: releases to
#                                                  production wait for your approval
#
# Run it again after changing a key or the league deck. See docs/publishing-on-google-play.md §6.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
repo="$(cd "$ROOT" && gh repo view --json nameWithOwner -q .nameWithOwner)"

environment() {
  gh api -X PUT "repos/$repo/environments/$1" --silent "${@:2}"
}

# key=value from a Java .properties file (keystore.properties), \uXXXX escapes decoded.
property() {
  python3 - "$1" "$2" <<'PY'
import sys
for line in open(sys.argv[1], encoding="latin-1"):
    key, sep, value = line.partition("=")
    if sep and key.strip() == sys.argv[2]:
        print(value.strip().encode("latin-1").decode("unicode_escape"))
PY
}

case "${1:?usage: set-release-secrets.sh <app-folder> | --play | --approval}" in
  --play)
    key="$HOME/.config/lillo-android-apps/play-service-account.json"
    [ -f "$key" ] || { echo "No service account key at $key (docs/publishing-on-google-play.md §6)" >&2; exit 1; }
    gh secret set PLAY_SERVICE_ACCOUNT_JSON --repo "$repo" < "$key"
    ;;
  --approval)
    me="$(gh api user -q .id)"
    environment production --input - <<EOF
{"reviewers": [{"type": "User", "id": $me}], "deployment_branch_policy": null}
EOF
    echo "Releases to production now wait for your approval."
    ;;
  *)
    app="$1"
    props="$ROOT/apps/$app/keystore.properties"
    [ -f "$props" ] || { echo "No $props: scripts/new-upload-key.sh $app first (docs/signing.md)" >&2; exit 1; }
    jks="$(property "$props" storeFile)"
    [ -f "$jks" ] || { echo "The upload key $jks is missing" >&2; exit 1; }
    environment "$app"
    base64 < "$jks" | tr -d '\n' | gh secret set UPLOAD_KEYSTORE_BASE64 --repo "$repo" --env "$app"
    for name in storePassword keyAlias keyPassword; do
      secret="$(printf '%s' "$name" | sed -E 's/([A-Z])/_\1/g' | tr '[:lower:]' '[:upper:]')"  # storePassword → STORE_PASSWORD
      property "$props" "$name" | tr -d '\n' | gh secret set "$secret" --repo "$repo" --env "$app"
    done
    if [ "$app" = league-cards ]; then
      deck="${LEAGUE_PRIVATE_DIR:-$ROOT/../lloros-league/content}/league-private.json"
      [ -f "$deck" ] || { echo "No private deck at $deck (set LEAGUE_PRIVATE_DIR)" >&2; exit 1; }
      gzip -9 -c "$deck" | base64 | tr -d '\n' | gh secret set LEAGUE_DECK_GZ_BASE64 --repo "$repo" --env "$app"
    fi
    if [ "$app" = group-polls ]; then
      firebase="$ROOT/apps/group-polls/firebase.properties"
      [ -f "$firebase" ] || { echo "No $firebase (apps/group-polls/OPERATIONS.md)" >&2; exit 1; }
      gh secret set FIREBASE_PROPERTIES --repo "$repo" --env "$app" < "$firebase"
    fi
    echo "Release secrets for $app are in GitHub (environment '$app')."
    ;;
esac
