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
| 🔨 | Fake-ad games, game one **Summandos** (`com.jorgelillo.summandos`, `engine/summandos`): a shooting squad grown by maths gates, weapon power-ups that cost a gate, maths for adults with a think pause on the hardest gates, nothing to sell. Engine Godot 4.7.2 per the [technology study](docs/plans/fake-ad-games-tech.md); S1 and S2 done (60 fps on a real handheld, Godot confirmed); design pass, three maths modes and a 30-level map per mode in PRs #220/#221; CC0 3D models (crossbowmen against skeletons) and an About card in #222; next **you** okay the music (ours, or a CC0 track you download) and buy KayKit Skeletons EXTRA (Golem, Necromancer; files go to `~/Projects/documentation/assets/`, not the public repo); then the store's "Giant bosses" screenshot needs a bigger boss (a closer shot, or the Golem), a **smaller engine build** (Godot export templates compiled without unused modules: the engine is ~24 of the 26 MB, expected 10–15 MB) before S4, S3 and S4. Game two: pull the pin (lava, gold, a trapped hero) | Ready · [docs/plans/fake-ad-games.md](docs/plans/fake-ad-games.md) |

## repo

| | What is missing | Waiting on |
|---|---|---|
| 🔨 | Renovate's AndroidX bump ([#208](https://github.com/jorgelillo7/lillo-android-apps/pull/208): Compose BOM 2026.09 → 2026.11, Lifecycle 2.11.1) fails every check: Compose foundation 1.13.0 needs `compileSdk` 37.1 (today `37`). Raise it, check the apps on a phone, then merge | — |
| 🔨 | The other bottom sheets (decision-wheel's and league-cards' own; whos-lying's and tournaments' already expanded) still open half-way: move them onto core `LilloSheet` | — |
| ⏳ | Play's new app-quality thresholds (memory and code optimisation) are enforced from February 2027: in January, run `scripts/play_vitals.py` and compare with the thresholds once published (2026-11-07: no app has enough users for Google to report vitals; R8 is on everywhere and the apps are 1–2 MB) | January 2027 |
| ⏳ | Release round of 2026-11-15 approved and with Google (production 11, 8, 13, 7, 9, 5, 4 for christmas-countdown, decision-wheel, whos-lying, tournaments, league-cards, slipper, stadium-events; tracks match the repo per `scripts/play_status.py`). Once live: check About in landscape on the handheld, and that a reminder arrives with the app closed (christmas-countdown, stadium-events) | Google's review, then you · same phone session: the new light and dark looks (whos-lying's night stages under light status-bar icons, tournaments' bracket image and christmas-countdown's widget kept night-coloured, hand-picked light palettes) with a debug build, `./gradlew :apps:<app>:app:installDebug` |
| ⏳ | Release workflow without a stored key: Workload Identity Federation (GitHub OIDC → the `play-publisher` service account), then delete its JSON key | The first CI releases working with the key |
