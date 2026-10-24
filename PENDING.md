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
| ⏳ | 1.1 in production since 2026-10-22 (and internal testing): first public release; gather feedback from the friends who tested it | Google's review |
| ⏳ | A tester got "Item not found" on the opt-in link: likely the first review still pending or the wrong Google account in Play | Their next try |
| ⏳ | Tester feedback of 2026-10-23 fixed (names on the result, 24-player cap, colour wheel, no hint in Blind): ship it in the next release and ask the tester to check | Next release |

## group-polls (new app, parked until 2026-10-23)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Create the app in Play Console (`com.jorgelillo.grouppolls`, App · Social); the upload key exists. Listing texts and graphics are ready in `app/src/main/play/listings`; don't upload a bundle until Firebase exists (without it the app runs on sample polls) | You · answers in `apps/group-polls/OPERATIONS.md` → "Play Console answers" |
| 👤 | Create the Firebase project and give the OK | You · lillorepo `packages/group_polls/OPERATIONS.md` → "Create the project" |
| 🔨 | Then: `firebase.properties`, App Check (Play Integrity + debug token), Play signing SHA-256 in `assetlinks.json`, publish `/q/` and `assetlinks.json` on the website (privacy page already live), Terraform on, internal test | The two steps above · plan in [docs/plans/group-polls.md](docs/plans/group-polls.md) |

## tournaments (new app)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | v1 in internal testing (2026-10-22, listing published): add testers and send them the opt-in link; production needs its countries and, after testing, `action=promote` | You |
| ⏳ | Double elimination and round robin | Feedback after real use |

## league-cards (new game, internal test with the league)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | v1 in internal testing (2026-10-22, listing with the sample deck published): add the league's Gmail addresses as testers and send the opt-in link | You |
| 👤 | Each league member's OK to appear in the private deck | You |
| ⏳ | More cards and chat lines from the group | Ideas from the league · private deck, never in this repo |
| ⏳ | Tester feedback of 2026-10-22 fixed (clipped buttons and cards on handhelds, readable meters, short season, no repeated chat, release-clause story): next release, then re-upload the deck secret | You · `scripts/set-release-secrets.sh league-cards` |

## slipper game (¿Te pilla la chancla?, in development)

| | What is missing | Waiting on |
|---|---|---|
| 🔨 | **Next:** finish v1 from the prepared prototype: play feel (jump, hit pause, shake, haptics, sounds), pixel-art polish, then upload key and Play app | Planned for 2026-10-23 · [docs/plans/slipper.md](docs/plans/slipper.md) |
| 👤 | Confirm the package id `com.jorgelillo.slipper` (permanent once uploaded), then upload key and Play app | You |
| 🔨 | Website: `/c/` challenge page, privacy page (draft in the website repo) and the card in "Juegos" | The first internal test |

## repo

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | Move into `lillorepo` (Bazel) | Gradle builds or the Play publishing loop slowing you down · domain modules are pure Kotlin to make it easy |
| 👤 | Publishing from CI is ready (Release workflow): create the Google Cloud service account and give it access in Play Console, then `scripts/set-release-secrets.sh --play` and `scripts/set-release-secrets.sh <app>` per app, in your terminal (docs/publishing-on-google-play.md §6) | You |
| ⏳ | Release workflow without a stored key: Workload Identity Federation (GitHub OIDC → the `play-publisher` service account), then delete its JSON key | The first CI releases working with the key |
| ⏳ | Christmas Countdown's `minSdk` 23 → 24, the only app still on 23 (blocks WorkManager 2.12 repo-wide; check the Play Console share of API 23 first) | Christmas's install stats |
| 🔨 | **Next (from 9 to 9.5 as an Android reference):** baseline profiles + Macrobenchmark (startup and the main journeys, a `:baselineprofile` module per app or shared, Play ships the profile) | Planned for 2026-10-23 |
| 🔨 | **Next:** audit edge-to-edge (mandatory with targetSdk 35+: insets on every screen, landscape, gesture bar), predictive back (`enableOnBackInvokedCallback`, back animations in navigation) and large screens (tablets, foldables: window size classes beyond `isShortScreen`) | Planned for 2026-10-23 |
