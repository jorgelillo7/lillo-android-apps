---
name: release-android-app
description: Prepare and ship a new version of one of the apps in this repo to Google Play: bump versionCode, write release notes in every language, open the release PR (CI builds, tests and smoke tests it), then publish with the Release workflow (internal testing, production, promotion, store listing) or from the terminal. Use when the user wants to publish, release, upload or ship an app or a new version.
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

The release PR's CI already builds every release (R8), runs the tests and smoke-tests the touched
app's release build on an emulator. Locally, before opening it:

```bash
source scripts/env.sh
./gradlew :apps:<app>:app:lintDebug :apps:<app>:app:testDebugUnitTest :apps:<app>:app:bundleRelease
scripts/verify-bundle.sh apps/<app>/app/build/outputs/bundle/release/app-release.aab
```

Check in the `verify-bundle.sh` output:
- `versionCode` is the new one (a stale bundle from an earlier build is easy to upload by mistake).
- Signature serial matches the app's upload key (see `apps/<app>/OPERATIONS.md`). "NOT SIGNED"
  means `apps/<app>/keystore.properties` is missing: the user runs `scripts/new-upload-key.sh <app>`
  in their own terminal (docs/signing.md); never ask them to paste passwords into the chat.
- New permissions vs. the previous release → the Data safety form may need updating.

The emulator smoke test (`apps/<app>/tools/smoke_test.sh`) also runs locally when a change needs a
look on screen (`scripts/emulator.sh start`; ask the user to close Chrome first, 8 GB RAM).

## 4. Store listing (only if features changed)

- Update `full_description.txt`/`short_description.txt` per locale.
- New screenshots: `apps/<app>/tools/capture_store_screenshots.sh`, then
  `.venv/bin/python apps/<app>/tools/generate_store_assets.py`. Look at the images before handing
  them over.
- Tell the user to publish the listing **together with** the release (never before it).

## 5. Commit and PR

Commit version bump, notes and assets on a branch; open a PR; it is merged once CI is green
(Lint, Test, Release, Smoke). The release ships what is on `master`.

## 6. Publish

**With the Release workflow** (the normal way; set-up and details: docs/publishing-on-google-play.md
§6). It builds the signed bundle from `master`, checks it, publishes and tags `<app>/v<versionCode>`.
Confirm with the user before running it: it goes out to Google Play.

```bash
gh workflow run release.yml -f app=<app> -f action=upload -f track=internal     # → internal testing
# the user installs it from the opt-in link on a real phone; when it's fine:
gh workflow run release.yml -f app=<app> -f action=promote -f track=production  # waits for their approval
```

- Straight to production (no testing round): `-f action=upload -f track=production`.
- With the store listing (texts, graphics): add `-f listing=true` to the run that ships the change.
- `-f dry_run=true` builds and checks everything and publishes nothing.
- Production runs wait in GitHub (Actions → the run → *Review deployments*) until the user approves.
- Follow it with `gh run watch`; the run's summary shows the bundle check.
- "No upload key" / "No Play service account": the user runs `scripts/set-release-secrets.sh <app>`
  or `--play` in their own terminal.

**From the terminal** (same tasks, the key and service account from this Mac):
`./gradlew :apps:<app>:app:publishBundle` (internal), `promoteArtifact --from-track internal
--promote-track production`, `publishListing`.

**By hand in Play Console**: the app's very first bundle (the API can't create an app), and the App
content forms. Upload the `.aab`, paste the notes, roll out; *Send changes for review*.

Managed publishing on? The user presses "Publish" in Play Console after approval.

Expected review warnings that are OK: "devices no longer supported" after raising `minSdk`;
"native code without debug symbols" (AndroidX `.so` files). Anything else: ask for a screenshot.

## 7. After approval

Ask the user to install from Play on a real phone; note anything learned in
`docs/lessons-learned.md` and `apps/<app>/release-notes.md`.
