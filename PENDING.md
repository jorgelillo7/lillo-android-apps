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
| 👤 | Version 14 (Original, Mysterious and Chaos modes, journalists, setup in cards, continue a match from the home) uploaded to production with the listing by the Release workflow: approve it in GitHub, then Google's review; once live, play every mode on a real phone | You |

## group-polls (new app, parked until 2026-10-23)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Create the app in Play Console (`com.jorgelillo.grouppolls`, App · Social); the upload key exists. Listing texts and graphics are ready in `app/src/main/play/listings`; don't upload a bundle until Firebase exists (without it the app runs on sample polls) | You · answers in `apps/group-polls/OPERATIONS.md` → "Play Console answers" |
| 👤 | Create the Firebase project and give the OK | You · lillorepo `packages/group_polls/OPERATIONS.md` → "Create the project" |
| 🔨 | Then: `firebase.properties`, App Check (Play Integrity + debug token), the Firebase API key restricted to this package and both signing SHA-1s and to the APIs the app calls (lillorepo `PENDING.md`), Play signing SHA-256 in `assetlinks.json`, publish `/q/` and `assetlinks.json` on the website (privacy page already live), Terraform on, internal test | The two steps above · plan in [docs/plans/group-polls.md](docs/plans/group-polls.md) |

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
| 🔨 | Fake-ad games: **next big project once this file holds only long-term items**. First a technology study (engine, crowds of 1,000+, physics for pin-pull and fluids), then game one: the maths-gates crowd runner (a cannon fires a crowd through + / × gates at an enemy base), faithful in mechanics and feel, own art and names. Pin-pull (lava, treasure, a trapped character) is the candidate for game two | Ready · [docs/plans/fake-ad-games.md](docs/plans/fake-ad-games.md) |

## repo

| | What is missing | Waiting on |
|---|---|---|
| 🔨 | The other bottom sheets (decision-wheel's and league-cards' own; whos-lying's and tournaments' already expanded) still open half-way: move them onto core `LilloSheet` | — |
| ⏳ | Play's new app-quality thresholds (memory and code optimisation) are enforced from February 2027: in January, run `scripts/play_vitals.py` and compare with the thresholds once published (2026-11-07: no app has enough users for Google to report vitals; R8 is on everywhere and the apps are 1–2 MB) | January 2027 |
| ⏳ | Release round of 2026-11-15 approved and with Google (production 11, 8, 13, 7, 9, 5, 4 for christmas-countdown, decision-wheel, whos-lying, tournaments, league-cards, slipper, stadium-events; tracks match the repo per `scripts/play_status.py`). Once live: check About in landscape on the handheld, and that a reminder arrives with the app closed (christmas-countdown, stadium-events) | Google's review, then you · same phone session: the new light and dark looks (whos-lying's night stages under light status-bar icons, tournaments' bracket image and christmas-countdown's widget kept night-coloured, hand-picked light palettes) with a debug build, `./gradlew :apps:<app>:app:installDebug` |
| ⏳ | Release workflow without a stored key: Workload Identity Federation (GitHub OIDC → the `play-publisher` service account), then delete its JSON key | The first CI releases working with the key |
