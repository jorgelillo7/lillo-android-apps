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

UI test tags: `new_season`, `new_short_season`, `continue`, `autosave`, `about`, `card_text`, `card`, `choose_left|right`, `joker`, `outcome`,
`meter_money|squad|patience|tears`, `table`, `standings`, `chat`, `next`, `ending`, `share`,
`trophies`, `ending_<key>`, `achievement_<id>`, `achievement_unlocked`, `back`.

## Editing the deck

Cards are JSON (see `domain/League.kt` for every field). A choice's `next` deals another card
straight after it (stories); `followUp` deals one at the start of the next matchday; story cards
that should never come up on their own are `followUpOnly`. After editing, run the domain tests:
they fail on broken references (unknown character, missing follow-up) and print how often a
careful and a random player reach the last matchday. A season that is too hard or too easy shows
there first. A season saved on a phone keeps working while the deck's `name` stays the same.

Group-chat lines can say `{points}` (your matchday), `{position}` (yours in the table), `{leader}`
(the best rival) and `{last}` (the worst). A season sends each line once before repeating any.
A short season (half the matchdays) runs through the deck's whole calendar, so cards with a
`fromMatchday`/`toMatchday` window still come up, earlier.

Achievements are part of the deck (`achievements`, and `achievement` on a choice or a gamble);
the ids `survivor`, `podium`, `champion` and `champion_no_joker` are unlocked by how the season
ends. Renaming an achievement id loses it on the phones that had it.

## Release

Upload key (created 21 Oct 2026 with `scripts/new-upload-key.sh`, see [docs/signing.md](../../docs/signing.md)):

| | |
|---|---|
| File | `~/Projects/documentation/keys/league-cards/upload-league-cards.jks` (+ off-Mac backup) |
| Alias | `upload` |
| Serial | `8a0e28359f63b675` |
| SHA-1 | `6D:C9:AA:F8:3C:13:94:FF:E7:25:D8:70:5D:AC:AD:EA:4B:4E:26:09` |
| SHA-256 | `21:3F:3F:F8:8D:25:5B:FD:1A:C5:5B:11:33:8B:D1:4D:0E:C5:65:51:F7:24:FD:08:6C:2C:E0:8B:5E:38:FE:BA` |

App created in Play Console on 21 Oct 2026. The first bundle (versionCode 1, with the private deck)
goes to internal testing by hand; later ones from the Release workflow.

Published to production with the private deck since versionCode 2 (2026-10-24): every league
member gave their OK to appear in it. Anyone new in the deck needs the same OK first. CI's Release workflow takes the deck from a GitHub secret
(`scripts/set-release-secrets.sh league-cards`, again after each deck change) and refuses to
publish a bundle without it.

| versionCode | Version | Notes |
|---|---|---|
| 1 | 0.1 | First test build for the league |
