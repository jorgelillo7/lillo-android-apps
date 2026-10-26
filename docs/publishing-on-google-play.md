# Publishing an app on Google Play

Checklist distilled from relaunching Christmas Countdown in 2026. Follow it top to bottom for a new
app; for an update, jump to [6. Release](#6-release).

## 0. Developer account (once)

- [ ] Personal account, **identity verified** (Play Console → Account details).
- [ ] Log in at least once a year and publish something: inactive accounts get closed and their
      apps removed. Recovering it needs a support ticket.
- [ ] Developer profile (Play Console → Developer page): icon 512×512, header 4096×2304,
      promotional text ≤ 140 chars. Assets: [`branding/developer-profile/`](../branding/developer-profile).
- [ ] **Android developer verification** (2026+): each package name must be registered to the
      verified identity. Play Console → Android developer verification → Register package name.
      For an app using Play App Signing, register the **app signing key** fingerprint (Integrity →
      App signing), not the upload key. Proving ownership may need an APK signed by that key with
      a token file in `assets/adi-registration.properties`.

## 1. Package name and signing (once per app)

- [ ] Choose `applicationId` (`com.jorgelillo.<app>`). **It can never change.**
- [ ] Create the **upload key** and `apps/<app>/keystore.properties`: run
      `scripts/new-upload-key.sh <app>` in your own terminal and follow [signing.md](signing.md)
      (password manager + off-Mac backup of the `.jks`).
- [ ] Use **Play App Signing** (default): Google keeps the app signing key; you only hold the upload key.
- [ ] Lost upload key? See [signing.md](signing.md#lost-key-or-forgotten-password).

## 2. Build the release

```bash
source scripts/env.sh
./gradlew test :apps:<app>:app:lintDebug :apps:<app>:app:bundleRelease
apps/<app>/tools/smoke_test.sh            # release build on the emulator (R8 only runs here)
scripts/verify-bundle.sh apps/<app>/app/build/outputs/bundle/release/app-release.aab
```

- [ ] `versionCode` higher than anything uploaded before (Play Console → App bundle explorer).
- [ ] Signing certificate matches the upload key (`verify-bundle.sh` prints it).
- [ ] targetSdk meets the current requirement (API 36 since 31 Aug 2026):
      https://developer.android.com/google/play/requirements/target-sdk

## 3. Privacy policy

- [ ] Add `privacy/<app>/index.html` to the site repo `jorgelillo7/jorgelillo7.github.io`
      (copy the Christmas Countdown one). URL: `https://jorgelillo7.github.io/privacy/<app>/`.
- [ ] Also link it from inside the app (About screen).
- [ ] Never host it in the app's own repo: renaming the repo would break the URL.

## 4. App content declarations (Policy → App content)

| Declaration | Answer for a no-data, no-ads app |
|---|---|
| Privacy policy | The URL above |
| Ads | No ads |
| App access | All functionality available without special access |
| Content rating | Questionnaire: utility/other, "No" to everything |
| Target audience | 13+ (avoids the Families policy unless the app is for children) |
| Data safety | Does not collect or share data |
| Advertising ID | No |
| Government apps | No |
| Financial features | My app doesn't provide financial features |
| Health apps | My app doesn't have health features |

Check the merged release manifest for permissions before answering Data safety
(`verify-bundle.sh` lists them). Libraries add some (WorkManager adds WAKE_LOCK, etc.).

## 5. Store listing (Grow users → Store presence → Main store listing)

Everything is tracked in `apps/<app>/app/src/main/play/` (the [Gradle Play Publisher](https://github.com/Triple-T/gradle-play-publisher)
layout) and uploaded from the terminal with `publishListing` (§6):

| Field | File (under `listings/<locale>/`) | Limit |
|---|---|---|
| App name | `title.txt` | 30 |
| Short description | `short-description.txt` | 80 |
| Full description | `full-description.txt` | 4000 |
| Icon | `graphics/icon/icon.png` | 512×512 |
| Feature graphic | `graphics/feature-graphic/feature-graphic.png` | 1024×500 |
| Phone screenshots | `graphics/phone-screenshots/01..NN.png` | 2–8, ≥1080 px for promotion |
| Release notes (this version) | `../../release-notes/<locale>/default.txt` | 500 |

The graphics are generated (`tools/generate_store_assets.py`, §2 of the app's OPERATIONS.md); the
history of past notes is `apps/<app>/release-notes.md` plus git.

- [ ] **Don't publish a listing describing features the live version doesn't have** (misrepresentation).
      Ship the listing together with the release that adds them.

## 6. Release

### Publishing from the terminal (once: a service account)

One Google Cloud service account publishes every app of the developer account:

1. Google Cloud Console → a project (any) → *APIs & Services* → enable **Google Play Android Developer API**.
2. *IAM & Admin → Service accounts* → create one (e.g. `play-publisher`) → *Keys* → add a JSON key.
3. Save it **outside the repo**: `~/.config/lillo-android-apps/play-service-account.json`
   (`chmod 600`; or pass `-Pplay.credentials=<path>`, or the `ANDROID_PUBLISHER_CREDENTIALS` env var).
4. Play Console → *Users and permissions* → *Invite new users* → the service account's e-mail →
   app permissions for the apps (*Release to testing tracks*, *Release to production*, *Manage
   store presence*) → invite. It can take a few minutes to work.
5. A brand-new app's **first** bundle still goes through Play Console by hand (the API can't create it),
   and so do the App content forms (§4); every later release goes from CI or the terminal.

### From CI: the Release workflow (once: secrets)

Every later release can go out without opening Play Console: **Actions → Release → Run workflow**
(also from the GitHub mobile app), or `gh workflow run release.yml -f app=<app> …`. It builds the
signed bundle from `master`, checks it (`scripts/verify-bundle.sh`), publishes with Gradle Play
Publisher and tags the commit `<app>/v<versionCode>`.

| Choose | What happens |
|---|---|
| `upload` + `internal` | the usual first step: install it from the opt-in link and try it |
| `promote` + `production` | moves that same bundle on (no new build) |
| `upload` + `production` | straight to production, when no testing round is needed |
| `listing` | also publishes the store texts and graphics, with the release that needs them |
| `draft` | the release is created as a draft: needed for an app's **first** production release (Google answers "Only releases with status draft may be created on draft app" until the app has been published once; internal testing doesn't count). Then Play Console → *Publishing overview* → *Send changes for review* |
| `dry_run` | builds and checks everything, publishes nothing |

Production waits for your approval in GitHub (*Review deployments* on the run).

**Play status** (Actions → Play status, also from the GitHub app): a table of every app's version
in this repo next to each Play track's releases and their status (`completed`, `draft`,
`inProgress`), marking the apps whose repo version is ahead of production. Read only
(`scripts/play_status.py`). "In review" isn't in Google's API: Play Console shows it.

Once, in your own terminal (values go to GitHub through stdin, never through the chat):

```bash
scripts/set-release-secrets.sh --play            # the service account key above
scripts/set-release-secrets.sh <app>             # each app's upload key (+ league-cards' private deck, group-polls' Firebase config)
scripts/set-release-secrets.sh --approval        # production waits for your approval (already set)
```

Each app's upload key lives in a GitHub environment named like the app folder; the service
account in the repository secret `PLAY_SERVICE_ACCOUNT_JSON`. With Play App Signing, a leaked
upload key can be reset from Play Console: Google keeps the key that signs what users install.
Run the script again after changing a key or league-cards' private deck.

### Each release from the terminal

Changes are committed as one edit and sent for review on their own; with **Managed publishing**
on (Publishing overview) you still press "Publish" after approval. Without credentials the same
steps work by hand in Play Console: upload the `.aab`, paste the notes (`<es-ES>…</es-ES><en-US>…</en-US>`),
then *Send changes for review*.

Expected review warnings that are fine to accept:
- *Devices no longer supported*: after raising `minSdk`.
- *Native code without debug symbols*: the `.so` files come from AndroidX, not our code.

## 7. After release

- [ ] Install from Play on a real phone and check the app starts.
- [ ] Watch Android vitals (crashes/ANRs) for a few days.
- [ ] Add anything new learned to `docs/lessons-learned.md` and a row in `apps/<app>/release-notes.md`.
