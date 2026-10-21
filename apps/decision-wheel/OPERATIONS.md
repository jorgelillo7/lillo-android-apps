# What next? (decision wheel) — Operations

Run from the **repo root** after `source scripts/env.sh`. Repo-wide commands and scripts:
[docs/operations.md](../../docs/operations.md).

Module paths: app `:apps:decision-wheel:app`, logic `:apps:decision-wheel:domain`.
Package: `com.jorgelillo.decisionwheel` (minSdk 24: navigation-compose needs it).

## Develop

| Task | Command |
|---|---|
| Unit tests | `./gradlew :apps:decision-wheel:domain:test` |
| Lint | `./gradlew :apps:decision-wheel:app:lintDebug` |
| Install debug | `./gradlew :apps:decision-wheel:app:installDebug` |
| Launch | `$ADB shell am start -n com.jorgelillo.decisionwheel/.MainActivity` |
| Reset to presets (clears wheels and history) | `$ADB shell pm clear com.jorgelillo.decisionwheel` |
| Run in Spanish | `$ADB shell cmd locale set-app-locales com.jorgelillo.decisionwheel --locales es-ES` |
| Smoke test (release build, emulator running) | `apps/decision-wheel/tools/smoke_test.sh` |
| Regenerate icons | `.venv/bin/python apps/decision-wheel/tools/generate_icon.py` |

UI test tags (usable with `scripts/tap-text.sh`): `new_wheel`, `more`, `spin`, `accept`, `again`,
`history`, `edit`, `back`, `name`, `new_option`, `save`, `favorite_<wheel id>` (home cards and
the wheel's top bar), `restore_presets` (overflow menu). Launcher shortcuts open
`MainActivity` with the extra `wheel_id`.

## Release

Live on Google Play since October 2026 (v1, production). Releases follow the
`release-android-app` skill.

Upload key (created 4 Oct 2026 with `scripts/new-upload-key.sh`, see [docs/signing.md](../../docs/signing.md)):

| | |
|---|---|
| File | `~/Projects/documentation/keys/decision-wheel/upload-decision-wheel.jks` (+ off-Mac backup) |
| Alias | `upload` |
| Serial | `da62bb8484e5ec9d` (valid until Feb 2054) |
| SHA-1 | `E4:78:C6:53:CB:3A:06:AD:F1:94:C0:AC:3E:73:E4:36:BD:32:31:30` |
| SHA-256 | `52:DD:2C:AB:AF:2E:B2:0B:3B:74:5F:B0:AC:5F:0A:01:E9:B7:B4:60:BB:B7:65:B7:5A:BB:99:7E:FD:5D:6D:3C` |

```bash
./gradlew :apps:decision-wheel:app:bundleRelease
apps/decision-wheel/tools/smoke_test.sh
scripts/verify-bundle.sh apps/decision-wheel/app/build/outputs/bundle/release/app-release.aab
```

Store listing texts and images: `app/src/main/play/listings/<locale>/`. Publish from CI:
`gh workflow run release.yml -f app=decision-wheel -f action=upload -f track=internal` (then
`-f action=promote -f track=production`); the `release-android-app` skill /
[docs/publishing-on-google-play.md](../../docs/publishing-on-google-play.md) have the details.
