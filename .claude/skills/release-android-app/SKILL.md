---
name: release-android-app
description: Prepare and ship a new version of one of the apps in this repo to Google Play: bump versionCode, write release notes in every language, build and verify the signed bundle, smoke test the release build, then guide the user through Play Console (internal testing, production, store listing). Use when the user wants to publish, release, upload or ship an app or a new version.
---

# Release an app to Google Play

Ask which app (`apps/<app>`) if there is more than one. Full background:
`docs/publishing-on-google-play.md`. App-specific commands: `apps/<app>/OPERATIONS.md`.

## 1. Version

- Current `versionCode`/`versionName`: `apps/<app>/app/build.gradle.kts`.
- The new `versionCode` must be higher than anything ever uploaded. Ask the user to confirm the
  latest in Play Console → App bundle explorer if unsure.
- Bump both values; add a row to `apps/<app>/release-notes.md`.

## 2. Release notes

Rewrite `apps/<app>/app/src/main/play/release-notes/<locale>/default.txt` for every locale
(≤ 500 chars, user-facing, no technical jargon; `generateReleasePlayResources` checks the length).
Publishing from the terminal uploads them; for a by-hand upload give the user the Play Console
block, one tag per language:

```
<es-ES>
…
</es-ES>
<en-US>
…
</en-US>
```

## 2b. Reuse check

Before building, look at what this version added (`git diff <last release tag or commit>..`): any
composable, helper, Gradle setting or script that another app also has (or core already offers)
goes to core / `build-logic` / `scripts` now, with the other apps switched over (CLAUDE.md, "Reuse
first"). Cheaper than a repo-wide clean-up later.

## 3. Build and verify

```bash
source scripts/env.sh
./gradlew test :apps:<app>:app:lintDebug :apps:<app>:app:bundleRelease
scripts/emulator.sh start          # ask the user to close Chrome first (8 GB RAM)
apps/<app>/tools/smoke_test.sh     # release build: must print SMOKE TEST PASSED
scripts/emulator.sh stop
scripts/verify-bundle.sh apps/<app>/app/build/outputs/bundle/release/app-release.aab
open -R apps/<app>/app/build/outputs/bundle/release/app-release.aab
```

Check in the `verify-bundle.sh` output:
- `versionCode` is the new one (a stale bundle from an earlier build is easy to upload by mistake).
- Signature serial matches the app's upload key (see `apps/<app>/OPERATIONS.md`). "NOT SIGNED"
  means `apps/<app>/keystore.properties` is missing: the user runs `scripts/new-upload-key.sh <app>`
  in their own terminal (docs/signing.md); never ask them to paste passwords into the chat.
- New permissions vs. the previous release → the Data safety form may need updating.

If the app has no `tools/smoke_test.sh` yet, create one on `scripts/smoke_lib.sh` like
`apps/tournaments/tools/smoke_test.sh` (taps by test tag, never coordinates).

## 4. Store listing (only if features changed)

- Update `full_description.txt`/`short_description.txt` per locale.
- New screenshots: `apps/<app>/tools/capture_store_screenshots.sh`, then
  `.venv/bin/python apps/<app>/tools/generate_store_assets.py`. Look at the images before handing
  them over.
- Tell the user to publish the listing **together with** the release (never before it).

## 5. Commit and PR

Commit version bump, notes and assets on a branch; open a PR; the user merges. Build the bundle
from the merged `master` if anything changed after the smoke test.

## 6. Publish

From the terminal when `~/.config/lillo-android-apps/play-service-account.json` exists (set-up and
details: docs/publishing-on-google-play.md §6). Confirm with the user before each command: they go
out to Google Play.

1. `./gradlew :apps:<app>:app:publishBundle` → internal testing. The user installs it on a real
   phone from the opt-in link.
2. When it's fine: `./gradlew :apps:<app>:app:promoteArtifact --from-track internal --promote-track production`.
3. If the listing changed: `./gradlew :apps:<app>:app:publishListing` (same go as the release).
4. Managed publishing on? The user presses "Publish" in Play Console after approval.

No key yet, or the app's very first bundle (the API can't create it): the same steps by hand in
Play Console (internal testing → upload the `.aab` → paste the notes → roll out; promote; listing;
*Send changes for review*).

Expected review warnings that are OK: "devices no longer supported" after raising `minSdk`;
"native code without debug symbols" (AndroidX `.so` files). Anything else: ask for a screenshot.

## 7. After approval

Ask the user to install from Play on a real phone; note anything learned in
`docs/lessons-learned.md` and `apps/<app>/release-notes.md`.
