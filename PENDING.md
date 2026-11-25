# Pending work

The index of what is still open. **One line per item.** If it needs more than that, the
reasoning lives where it belongs (the app's docs, a PR, `git log`) and this file links to it.

Lines are pruned as items ship. What has **shipped** lives in `apps/<app>/release-notes.md`.

**👤 needs you** · **⏳ waiting on a trigger or on data** · **🔨 ready to pick up** ·
**🚧 blocked on you, and parked by your call**

---

## christmas-countdown

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | Grow the advent pool (activities, songs, public-domain villancicos) between January and November, never during an Advent in progress | January · ideas from you · `AdventActivity` / `AdventSong` in the domain |

## whos-lying

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | Version 14 (1.10: Original, Mysterious and Chaos modes, journalists, setup in cards, continue a match from the home) sent to production with the listing on 16/11, nothing left to approve in GitHub; once Google's review puts it live, play every mode on a real phone (Original with and without a clue, Mysterious with journalists, Chaos, "Seguir partida" from the home) | Google's review (only Play Console shows it), then you |

## stadium-events (new app, v1 built 2026-11; feed live at `jorgelillo7.github.io/stadium/events.json`)

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | Try it with the family for a couple of weeks: do the evening and morning reminders arrive (1.2 fixed reminders that were lost with the app closed), is the widget clear? | Real use |

## tournaments (new app)

| | What is missing | Waiting on |
|---|---|---|
| 🚧 | Double elimination and round robin | Parked by your call until someone uses the app |

## fake-ad games (idea, a collection)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Fake-ad games, game one **Summandos** (`com.jorgelillo.summandos`, `engine/summandos`): built and on master (four ranks with help, 100 levels with stars, Legend mode, KayKit crossbowmen vs skeletons, aiming, squad colours, sound and music, store graphics for phone and tablet, slim engine 27 → 19 MB). Golem and Necromancer bosses (bought KayKit Skeletons EXTRA, copied in from `~/Projects/documentation/assets/`, never the public repo) and Kenney weapons are in. **On Google Play since 1.0**; releases: `scripts/engine-release.sh` and the Release workflow. Next: an **endless mode** unlocked by finishing every level: generated levels one after another (difficulty past the last level, the squad carried over), until the squad dies; the score is the levels cleared, the best kept on the device. Levels made ahead (`SummandosLevelsAhead`) so there is no wait between them. Game two: pull the pin (lava, gold, a trapped hero; its engine profile keeps 2D physics) | Ready · [docs/plans/fake-ad-games.md](docs/plans/fake-ad-games.md) |

## repo

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | Engine games target API 36, from Godot 4.7.2's Gradle template (the apps target 37). Play requires 36 today; before Play's next target-SDK deadline (usually August), move to a Godot whose template targets 37, re-export with `--aab` and smoke test | Summer 2027 |
| ⏳ | Play's new app-quality thresholds (memory and code optimisation) are enforced from February 2027: in January, run `scripts/play_vitals.py` and compare with the thresholds once published (2026-11-07: no app has enough users for Google to report vitals; R8 is on everywhere and the apps are 1–2 MB) | January 2027 |
| ⏳ | Release round of 2026-11-15 approved and with Google (production 11, 8, 13, 7, 9, 5, 4 for christmas-countdown, decision-wheel, whos-lying, tournaments, league-cards, slipper, stadium-events; tracks match the repo per `scripts/play_status.py`). Once live: check About in landscape on the handheld, and that a reminder arrives with the app closed (christmas-countdown, stadium-events) | Google's review, then you · same phone session: the new light and dark looks (whos-lying's night stages under light status-bar icons, tournaments' bracket image and christmas-countdown's widget kept night-coloured, hand-picked light palettes) with a debug build, `./gradlew :apps:<app>:app:installDebug` |
| ⏳ | Release workflow without a stored key: Workload Identity Federation (GitHub OIDC → the `play-publisher` service account), then delete its JSON key | The first CI releases working with the key |
