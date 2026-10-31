# ¿Te pilla la chancla? — Operations

Run from the **repo root** after `source scripts/env.sh`. Repo-wide commands and scripts:
[docs/operations.md](../../docs/operations.md).

Module paths: app `:apps:slipper:app`, logic `:apps:slipper:domain`.
Package: `com.jorgelillo.slipper` (confirmed 2026-10-25; it never changes).
English and Spanish.

## Develop

| Task | Command |
|---|---|
| Unit tests | `./gradlew :apps:slipper:domain:test :apps:slipper:app:testDebugUnitTest` |
| Re-record screenshots | `./gradlew :apps:slipper:app:recordRoborazziDebug` |
| Lint | `./gradlew :apps:slipper:app:lintDebug` |
| Install debug | `./gradlew :apps:slipper:app:installDebug` |
| Smoke test (release) | `apps/slipper/tools/smoke_test.sh` |
| Regenerate icons (from the sprites) | `.venv/bin/python apps/slipper/tools/generate_icon.py` |
| Tune the game | `domain/.../GameConfig.kt` (every number in one place) |
| Edit the pixel art | `app/src/main/assets/sprites.txt` |

UI test tags: `play`, `daily`, `sound`, `about`, `best`, `skin_<classic|checked|flip_flop|bunny>`,
`challenge_banner`, `dismiss_challenge`, `game`, `score`, `combo`, `power`, `chase`, `final_score`,
`run_stats`, `verdict`, `unlocked`, `retry`, `challenge_friend`, `home`.

| Also | Command |
|---|---|
| Balance report (bots over 200 seeds) | `./gradlew :apps:slipper:domain:test -i \| grep balance:` |
| Regenerate the sounds | `python3 apps/slipper/tools/generate_sounds.py` |

Changing anything that alters runs (GameConfig, House, Engine) bumps `GameConfig.VERSION`: challenge
links carry it, and a seed only replays the same run on the same version.

## Play Console answers

| Section | Answer |
|---|---|
| App or game | Game · category **Arcade** (tags: casual, runner) |
| Ads | No |
| Content rating | Cartoon or fantasy violence: **yes**, mild (a grandma throws slippers at a cartoon kid, nobody gets hurt); no sex, language, drugs, gambling, purchases; users can't interact or share content (a challenge link only carries a score) |
| Target audience | 13+ (13–15, 16–17, 18+). Recommended: the theme suits all ages, but including under-13s brings in the Families policy (teacher-approved rules, more review). Revisit later if wanted |
| Data safety | **No data collected, no data shared.** No permissions; vibration and sound need none |
| Privacy policy | `https://jorgelillo7.github.io/privacy/slipper/` (live once the website PR is merged) |
| Countries | All (178), like the other apps |

## Release

Not published. Releases follow the `release-android-app` skill. **First release, straight to
production** (an app's first production release goes up as a draft and is sent for review by hand):

1. Merge the website PR (privacy page, `/c/` page, games card): the policy must be live first.
2. Upload key, in your own terminal: `scripts/new-upload-key.sh slipper` (asks for a password), then
   copy serial / SHA-1 / SHA-256 into the table below and back the `.jks` up off the Mac.
3. Signed bundle and its check: `./gradlew :apps:slipper:app:bundleRelease`, then
   `scripts/verify-bundle.sh apps/slipper/app/build/outputs/bundle/release/app-release.aab`
   (versionCode 1, the upload key's serial, no permissions).
4. Play Console → Create app: "¿Te pilla la chancla?", Spanish (Spain), Game, Free. Answer the App
   content forms (table above), store listing from `app/src/main/play/listings/` (texts and
   `graphics/`), production countries.
5. Production → Create release → upload the `.aab`, release notes from
   `app/src/main/play/release-notes/`, → Send for review.
6. `scripts/set-release-secrets.sh slipper`: later releases go out with the Release workflow.
7. Done 2026-10-31: the **app signing** key's SHA-256
   (`10:07:4B:28:4B:B6:0F:2F:E6:E9:A2:3D:B4:ED:35:EA:D1:13:DC:58:9A:D3:54:60:EE:9A:35:F6:83:E0:F0:97`)
   is in the website's `/.well-known/assetlinks.json` (with the upload and debug keys): challenge
   links open the app directly.

Upload key (created 25 Oct 2026 with `scripts/new-upload-key.sh`, see [docs/signing.md](../../docs/signing.md)):

| | |
|---|---|
| File | `~/Projects/documentation/keys/slipper/upload-slipper.jks` (+ off-Mac backup) |
| Alias | `upload` |
| Serial | `44628c9d022825cd` (valid until Mar 2054) |
| SHA-1 | `D5:D7:7E:8B:9B:EE:D4:48:17:B7:71:71:86:40:0D:86:99:80:3E:E3` |
| SHA-256 | `C5:A5:4E:F1:9D:BE:B1:EC:B2:D2:E0:9A:FB:EC:D9:D2:0A:F8:0B:3C:86:CA:FA:89:6D:4C:94:AA:52:5F:9C:6F` |

| versionCode | Version | Date | Track | Notes |
|---|---|---|---|---|
| 1 | 1.0 | — | production (first) | The run, warnings, near misses, combos, power-ups, the cat, run of the day, skins, challenges by link |
