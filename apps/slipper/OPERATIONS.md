# ¿Te pilla la chancla? — Operations

Run from the **repo root** after `source scripts/env.sh`. Repo-wide commands and scripts:
[docs/operations.md](../../docs/operations.md).

Module paths: app `:apps:slipper:app`, logic `:apps:slipper:domain`.
Package: `com.jorgelillo.slipper` (**confirm before the first bundle**: it never changes after).
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

UI test tags: `play`, `about`, `best`, `challenge_banner`, `dismiss_challenge`, `game`, `score`, `chase`,
`final_score`, `verdict`, `retry`, `challenge_friend`, `home`.

## Release

Not published. First release: upload key (`scripts/new-upload-key.sh slipper`, then
`scripts/set-release-secrets.sh slipper`), create the app in Play Console (Game · Arcade), App
content forms, the first bundle by hand; later ones with the Release workflow. The `/c/` challenge
page and `assetlinks.json` entry go on the website with the Play signing SHA-256.

| versionCode | Version | Date | Track | Notes |
|---|---|---|---|---|
