# What do you vote? — Operations

Run from the **repo root** after `source scripts/env.sh`. Repo-wide commands and scripts:
[docs/operations.md](../../docs/operations.md). Backend operations (Firebase project, security
rules, moderation): **lillorepo** `packages/group_polls/OPERATIONS.md`.

Module paths: app `:apps:group-polls:app`, logic `:apps:group-polls:domain`.
Package: `com.jorgelillo.grouppolls` (minSdk 24: navigation-compose needs it).

## Develop

| Task | Command |
|---|---|
| Unit tests | `./gradlew :apps:group-polls:domain:test` |
| Lint | `./gradlew :apps:group-polls:app:lintDebug` |
| Install debug | `./gradlew :apps:group-polls:app:installDebug` |
| Smoke test (release, in-memory backend) | `apps/group-polls/tools/smoke_test.sh` |
| Regenerate icons | `.venv/bin/python apps/group-polls/tools/generate_icon.py` |
| Store screenshots | emulator running: `apps/group-polls/tools/capture_store_screenshots.sh`, check `store/raw/`, then `.venv/bin/python apps/group-polls/tools/generate_store_assets.py` |

UI test tags (`scripts/tap-text.sh`, dialogs included): `name_input`, `start`, `settings`,
`tab_public`, `tab_mine`, `create`, `poll_<code>`, `vote_red|blue`, `predict_red|blue`,
`change_side`, `share`, `result_red|blue`, `total_votes`, `voters_red|blue`, `close_poll`,
`confirm_close`, `report`, `hide_creator`, `confirm_report`, `question_input`, `red_input`,
`blue_input`, `visibility_private|public`, `duration_one_day|seven_days|no_limit`, `submit`,
`accept_terms`, `rename`, `rename_input`, `rename_save`, `predictions`, `terms`, `privacy`, `rate`,
`delete_data`, `confirm_delete`, `back`.

## Firebase config

`apps/group-polls/firebase.properties` (public identifiers, committed once the project exists):

```properties
projectId=…
appId=…
apiKey=…
```

Without it the app uses `FakePollRepository` (sample polls, nothing leaves the phone). With it,
`FirestorePollRepository`; App Check uses Play Integrity in release builds and the debug provider
in debug builds (register the debug token printed in logcat in the Firebase console).

## Invite links (App Links)

`https://jorgelillo7.github.io/q/?c=CODE` opens the poll. Verified by
`jorgelillo7.github.io/.well-known/assetlinks.json`, which must list the SHA-256 of **every**
certificate that signs an installed build:

| Certificate | Where to get it |
|---|---|
| Play App Signing (installs from Play) | Play Console → Test and release → App integrity → App signing key certificate |
| Upload key (local release builds) | `scripts/verify-bundle.sh` output, after `scripts/new-upload-key.sh group-polls` |
| Debug (development) | already listed (`~/.android/debug.keystore`) |

Check on a device: `adb shell pm get-app-links com.jorgelillo.grouppolls` → `verified`.

## Play Console answers

| Section | Answer |
|---|---|
| App or game | App · category **Social** |
| Ads | No |
| Content rating | Users can interact / share content: **yes** (polls are user-generated, public polls visible to everyone); no violence, sex, drugs, gambling, purchases, location |
| Target audience | 13+ (13–15, 16–17, 18+); not appealing to children |
| Data safety | Collected, not shared: **Name** (user-provided; app functionality), **User IDs** (anonymous Firebase ID; app functionality, fraud prevention), **Other user-generated content** (polls, votes, reports; app functionality). Encrypted in transit. Users can request deletion (in-app + web: privacy page) |
| Account deletion | No accounts (anonymous); in-app "Delete my data" + privacy page |
| UGC | Terms before posting, report on every public poll, hide a creator, auto-hide + manual review |
| Privacy policy | `https://jorgelillo7.github.io/privacy/group-polls/` |

## Release

Not published yet. Releases follow the `release-android-app` skill. Build the first bundle only
with `firebase.properties` in place: without it the app runs on sample polls.

Upload key (created 13 Oct 2026 with `scripts/new-upload-key.sh`, see [docs/signing.md](../../docs/signing.md)):

| | |
|---|---|
| File | `~/Projects/documentation/keys/group-polls/upload-group-polls.jks` (+ off-Mac backup) |
| Alias | `upload` |
| Serial | `894fef0886550d4b` |
| SHA-1 | `EF:FD:A3:9E:90:82:D7:30:06:7F:36:CE:26:AA:7E:F5:AF:C8:ED:8A` |
| SHA-256 | `29:AF:83:65:3D:54:C2:AD:1B:C1:5B:2F:CB:9F:F6:3F:CA:C0:33:9E:A6:77:DA:37:FE:D8:59:53:56:22:37:2B` |

The App Links in `assetlinks.json` need the **Play app signing** key's SHA-256 (Play Console →
App integrity), not this one: Play re-signs the bundle.

