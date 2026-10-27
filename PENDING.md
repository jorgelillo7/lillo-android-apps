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
| ⏳ | v5 (4.1, new name and listing) sent to production on 2026-10-22: check it on a real phone after Google's review (advent, music days, sharing, reminders) | Google's review |
| 👤 | Spanish carols (Los peces en el río, Campana sobre campana, Fum fum fum, Marimorena) | Reliable public-domain scores from you · *El burrito sabanero* is copyrighted (1972), not allowed |
| ⏳ | Grow the advent pool (activities and songs) between January and November, never during an Advent in progress | Ideas from you · `AdventActivity` / `AdventSong` in the domain |
| ⏳ | Raise `targetSdk` to 37 (Android 17 behaviour changes, test on an API 37 emulator) | Google Play announcing API 37 as the requirement (usually each August) |

## decision-wheel

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | v2 (1.1) sent to production on 2026-10-22 with the new listing: check it on a real phone after Google's review | Google's review |

## whos-lying

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | 1.2 (versionCode 6) sent to production on 2026-10-24 with the testers' feedback (names on the result, 24-player cap, colour wheel, no hint in Blind): ask the tester to check it after Google's review | Google's review |
| ⏳ | A tester got "Item not found" on the opt-in link: likely the first review still pending or the wrong Google account in Play | Their next try |

## group-polls (new app, parked until 2026-10-23)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Create the app in Play Console (`com.jorgelillo.grouppolls`, App · Social); the upload key exists. Listing texts and graphics are ready in `app/src/main/play/listings`; don't upload a bundle until Firebase exists (without it the app runs on sample polls) | You · answers in `apps/group-polls/OPERATIONS.md` → "Play Console answers" |
| 👤 | Create the Firebase project and give the OK | You · lillorepo `packages/group_polls/OPERATIONS.md` → "Create the project" |
| 🔨 | Then: `firebase.properties`, App Check (Play Integrity + debug token), Play signing SHA-256 in `assetlinks.json`, publish `/q/` and `assetlinks.json` on the website (privacy page already live), Terraform on, internal test | The two steps above · plan in [docs/plans/group-polls.md](docs/plans/group-polls.md) |

## tournaments (new app)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | v1 is a **draft** in production since 2026-10-24 (an app's first production release must be): Play Console → Publishing overview → Send changes for review; then check it on a real phone | You, then Google's review |
| ⏳ | Double elimination and round robin | Feedback after real use |

## league-cards (new game, live with the private deck)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | v2 (1.0) is a **draft** in production since 2026-10-24, with the private deck (every member's OK given): Play Console → Publishing overview → Send changes for review; after Google's review, send the league the Play link | You, then Google's review |
| 🔨 | The store screenshots show the sample deck; retake them with the league's deck if wanted | You |
| ⏳ | More cards and chat lines from the group | Ideas from the league · private deck, never in this repo |

## slipper game (¿Te pilla la chancla?, v1 ready, not on Play)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Play v1 on a phone and say how it feels (jump, speed, how much a hit costs) | You · [docs/plans/slipper.md](docs/plans/slipper.md) |
| 👤 | First release straight to production: upload key, Play app, first bundle by hand | You · checklist in `apps/slipper/OPERATIONS.md` → Release |
| 👤 | Website PR (games tab, privacy page, `/c/` page): merge it before the Play submission | You · jorgelillo7.github.io PR #1 |
| ⏳ | `assetlinks.json` entry for `/c/` with the Play app-signing SHA-256 | The app created in Play Console |

## repo

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | Tablet screenshots are in every app's listing folder (2026-10): publish them with each app's next release (`listing=true`), the first that carries the tablet layouts (readable width, Christmas' 8-column advent) | Each app's next release |
| ⏳ | Move into `lillorepo` (Bazel) | Gradle builds or the Play publishing loop slowing you down · domain modules are pure Kotlin to make it easy |
| 👤 | Publishing from CI is ready (Release workflow): create the Google Cloud service account and give it access in Play Console, then `scripts/set-release-secrets.sh --play` and `scripts/set-release-secrets.sh <app>` per app, in your terminal (docs/publishing-on-google-play.md §6) | You |
| ⏳ | Release workflow without a stored key: Workload Identity Federation (GitHub OIDC → the `play-publisher` service account), then delete its JSON key | The first CI releases working with the key |
| 🔨 | **Next (from 9 to 9.5 as an Android reference):** baseline profiles + Macrobenchmark (startup and the main journeys, a `:baselineprofile` module per app or shared, Play ships the profile) | Planned for 2026-10-23 |
| ⏳ | Large screens, next: window size classes for two-pane layouts (e.g. tournaments' list + bracket, league-cards' card + chat) on tablets in landscape | Tablet users asking · readable width per page done 2026-10 (`LilloPage(readable)`) |
