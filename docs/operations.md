# Operations — repo-wide runbook

Commands shared by every app. Per-app commands (module paths, release steps, signing) live in
`apps/<app>/OPERATIONS.md`; release history in `apps/<app>/release-notes.md`.

## Prerequisites

- Android Studio 2026.2+ with Android SDK 37 (see [setup/toolchain.md](setup/toolchain.md)).
- Every terminal session: `source scripts/env.sh` (Android Studio's JDK, `ADB`, `ANDROID_HOME`).
- Python tools (icons, store graphics): `scripts/setup-python.sh` once, then `.venv/bin/python <script>`.

## Build and test

```bash
./gradlew test                                   # every unit test
./gradlew :apps:<app>:domain:test --tests '*SomeTest*'
./gradlew :apps:<app>:app:lintDebug
./gradlew :apps:<app>:app:installDebug
./gradlew :apps:<app>:app:bundleRelease          # signed if apps/<app>/keystore.properties exists
```

## Shared scripts (`scripts/`)

| Script | Use |
|---|---|
| `env.sh` | `source` it: JDK, adb and SDK paths |
| `emulator.sh start [--headless]\|stop` | Boot (default `Pixel_9_API_36`; `--headless` without a window) / shut down the emulator |
| `emulator.sh date MMDDhhmmYYYY\|auto` | Freeze the emulator clock (e.g. to test December) / restore it |
| `emulator.sh demo on\|off` | Clean status bar for screenshots |
| `screenshot.sh <out.png>` | Screenshot, dismissing "isn't responding" dialogs first |
| `tap-text.sh "<label or tag>"` | Tap an element by its on-screen text or Compose test tag (any language, any layout) |
| `verify-bundle.sh <aab>` | Signature, versionCode, permissions and native libs of a bundle |
| `setup-python.sh` | Create `.venv` with Pillow/numpy |
| `smoke_lib.sh` | Sourced by every `apps/<app>/tools/smoke_test.sh`: release build, clean install, `tap`/`shot`, crash check |
| `store_assets.py` | Draws the Play feature graphic and framed screenshots; each app's `tools/generate_store_assets.py` passes its colours and headlines |
| `new-upload-key.sh <app>` | Create an app's upload key + `keystore.properties` (run in your own terminal; see [signing.md](signing.md)) |
| `set-release-secrets.sh <app> \| --play \| --approval` | Put an app's upload key (league-cards: and its private deck), the Play service account or the production approval into GitHub for the Release workflow (your own terminal) |

## Release (any app)

1. Bump `versionCode`/`versionName`, rewrite `app/src/main/play/release-notes/<locale>/default.txt`,
   add a row to `apps/<app>/release-notes.md`; open the PR. Its CI builds every release (R8) and
   smoke-tests the app's release build on an emulator.
2. Merged: **Actions → Release** (or `gh workflow run release.yml -f app=<app> …`) publishes it to
   internal testing, promotes it, or sends it straight to production (approval in GitHub).
3. By hand only for an app's first bundle and the App content forms:
   [publishing-on-google-play.md](publishing-on-google-play.md#6-release).

Locally, to look at a release build: `scripts/emulator.sh start && apps/<app>/tools/smoke_test.sh &&
scripts/emulator.sh stop`, and `scripts/verify-bundle.sh <aab>`.

The `release-android-app` skill walks through all of it.

## Machine limits

8 GB RAM: emulator + Android Studio + Gradle don't fit together. Close Chrome before emulator
work, stop the emulator during long builds, run emulator scripts in the foreground.

- Build first, then `./gradlew --stop`, then boot the emulator and run the smoke test (which
  only reuses the built APK when nothing changed). Never run Gradle tasks while it plays.
- `scripts/emulator.sh start --headless` for smoke tests: no window, screenshots still work.
- Gradle daemons exit after 10 idle minutes and the Kotlin daemon is capped at 1 GB
  (`gradle.properties`).
- Long-lived Claude Code sessions grow to several GB (one open for 34 days took 7.5 GB, mostly
  swap): start a new session per topic or `/clear`, and quit the ones you're not using.
- Swap above a few GB means it's time to reboot.
