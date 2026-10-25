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

## Release

Not published. First release: upload key (`scripts/new-upload-key.sh slipper`, then
`scripts/set-release-secrets.sh slipper`), create the app in Play Console (Game · Arcade), App
content forms, the first bundle by hand; later ones with the Release workflow. The `/c/` challenge
page and `assetlinks.json` entry go on the website with the Play signing SHA-256.

| versionCode | Version | Date | Track | Notes |
|---|---|---|---|---|
