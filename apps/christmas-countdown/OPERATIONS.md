# Christmas Countdown — Operations

Every command we use for this app. Run them from the **repo root** after:

```bash
source scripts/env.sh          # Android Studio's JDK, adb, SDK paths
```

Module paths: app `:apps:christmas-countdown:app`, logic `:apps:christmas-countdown:domain`.

## Develop

| Task | Command |
|---|---|
| Unit tests (domain logic) | `./gradlew :apps:christmas-countdown:domain:test` |
| All tests in the repo | `./gradlew test` |
| Lint | `./gradlew :apps:christmas-countdown:app:lintDebug` → `apps/christmas-countdown/app/build/reports/lint-results-debug.html` |
| Debug APK | `./gradlew :apps:christmas-countdown:app:assembleDebug` |
| Install debug on emulator/phone | `./gradlew :apps:christmas-countdown:app:installDebug` |
| Launch the app | `$ADB shell am start -n com.jorgelillo.christmascountdown/.MainActivity` |
| Clear app data (advent progress, settings) | `$ADB shell pm clear com.jorgelillo.christmascountdown` |
| Crash log | `$ADB logcat -d -b crash` |

## Emulator

| Task | Command |
|---|---|
| Start (Pixel 9, API 36) | `scripts/emulator.sh start` |
| Stop | `scripts/emulator.sh stop` |
| Pretend it's 5 December (test advent) | `scripts/emulator.sh date 120512002026` |
| Back to real time | `scripts/emulator.sh date auto` |
| Clean status bar for screenshots | `scripts/emulator.sh demo on` / `off` |
| Screenshot | `scripts/screenshot.sh build/shot.png` |
| Tap a button by its label | `scripts/tap-text.sh "Advent"` |
| Run app in Spanish only | `$ADB shell cmd locale set-app-locales com.jorgelillo.christmascountdown --locales es-ES` |

## Release

```bash
# 1. Bump versionCode / versionName in apps/christmas-countdown/app/build.gradle.kts
# 2. Release notes: store/metadata/android/{en-US,es-ES}/changelogs/<versionCode>.txt
# 3. Build, test and verify
./gradlew test :apps:christmas-countdown:app:lintDebug :apps:christmas-countdown:app:bundleRelease
apps/christmas-countdown/tools/smoke_test.sh        # needs a running emulator
scripts/verify-bundle.sh apps/christmas-countdown/app/build/outputs/bundle/release/app-release.aab
# 4. Reveal the bundle in Finder to drag it into Play Console
open -R apps/christmas-countdown/app/build/outputs/bundle/release/app-release.aab
```

Signing needs `apps/christmas-countdown/keystore.properties` (git-ignored) pointing to the upload
key, which is kept outside the repo (backed up off the Mac). Upload key certificate — use it to
check `verify-bundle.sh` output and to recognise the key in Play Console → App integrity:

| | |
|---|---|
| File | `~/Projects/documentation/keys/christmas-countdown/christmas.jks` (+ off-Mac backup) |
| Alias | `key0` (created 29 Aug 2021) |
| Serial | `2a72affc` |
| SHA-1 | `33:90:93:3B:B2:7C:97:B5:93:4C:24:2B:2C:4C:DD:FE:11:82:8F:3F` |
| SHA-256 | `12:EC:1D:3D:8B:56:8D:C9:C5:BE:AF:56:B8:ED:F4:3C:74:64:7A:BE:4A:8E:B0:8B:6A:A2:FE:DB:E3:48:EB:43` |

Lost key or password: [upload key reset](../../docs/publishing-on-google-play.md#1-package-name-and-signing-once-per-app).

Then follow [docs/publishing-on-google-play.md](../../docs/publishing-on-google-play.md#6-release).

## Store assets

```bash
scripts/setup-python.sh                                     # once
.venv/bin/python apps/christmas-countdown/tools/generate_icon.py          # launcher + store icon
apps/christmas-countdown/tools/capture_store_screenshots.sh               # raw captures (emulator running)
.venv/bin/python apps/christmas-countdown/tools/generate_store_assets.py  # framed screenshots + feature graphic
```

Release history and notes: [release-notes.md](release-notes.md).

## Reminders

Rules in `domain/Reminders.kt` (unit tested); `reminders/ReminderScheduler` keeps one WorkManager
job at the next slot (10:00 or 18:00 of a day with a possible reminder) and `ReminderWorker` shows
it if it still applies, then schedules the next. Re-planned on every app start.

Try one on the emulator: `scripts/emulator.sh date 120517582026` (5 Dec, 17:58), start the app,
accept the reminders card, wait two minutes for "Door 5 is still closed"; `date 122509592026` for
Christmas Day. `scripts/emulator.sh date auto` afterwards.
