# ¿Quién llora esta jornada? — Operations

Run from the **repo root** after `source scripts/env.sh`. Repo-wide commands and scripts:
[docs/operations.md](../../docs/operations.md).

Module paths: app `:apps:league-cards:app`, logic `:apps:league-cards:domain`.
Package: `com.jorgelillo.leaguecards`. Spanish only.

## Develop

| Task | Command |
|---|---|
| Unit tests (+ balance report, + private deck check when present) | `./gradlew :apps:league-cards:domain:test -i \| grep deck:` |
| Lint | `./gradlew :apps:league-cards:app:lintDebug` |
| Install debug | `./gradlew :apps:league-cards:app:installDebug` |
| Smoke test (release) | `apps/league-cards/tools/smoke_test.sh` |
| Regenerate icons | `.venv/bin/python apps/league-cards/tools/generate_icon.py` |
| Check which deck a build carries | `unzip -l apps/league-cards/app/build/outputs/apk/release/app-release.apk \| grep assets/league` |

UI test tags: `new_season`, `continue`, `about`, `card`, `choose_left|right`, `joker`, `outcome`,
`meter_money|squad|patience|tears`, `table`, `standings`, `chat`, `next`, `ending`, `share`, `back`.

## Editing the deck

Cards are JSON (see `domain/League.kt` for every field). After editing, run the domain tests:
they fail on broken references (unknown character, missing follow-up) and print how often a
careful and a random player reach the last matchday. A season that is too hard or too easy shows
there first. A season saved on a phone keeps working while the deck's `name` stays the same.

## Release

Upload key: not created yet. Create it with `scripts/new-upload-key.sh league-cards` in your own
terminal (see [docs/signing.md](../../docs/signing.md)), then record its serial and SHA-256 here.

Internal testing only, for the league: the bundle carries the private deck, so it is never
published openly while it does.

| versionCode | Version | Notes |
|---|---|---|
| 1 | 0.1 | First test build for the league |
