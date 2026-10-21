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
| 👤 | Upload v5 (4.1: new name "¿Ya es Navidad? Cuenta atrás" / "Is it Christmas yet? Countdown", new advent calendar with music days and sharing) and, with it, the new titles, full description and feature graphics from `store/metadata` | You · bundle built from master, notes in `changelogs/5.txt` |
| ⏳ | Check v4 on a real phone after Play approval: the 3 new carols (transcribed from memory), share text in both modes, widget | Google's review of v4 |
| 👤 | Spanish carols (Los peces en el río, Campana sobre campana, Fum fum fum, Marimorena) | Reliable public-domain scores from you · *El burrito sabanero* is copyrighted (1972), not allowed |
| ⏳ | Grow the advent pool (activities and songs) between January and November, never during an Advent in progress | Ideas from you · `AdventActivity` / `AdventSong` in the domain |
| ⏳ | Raise `targetSdk` to 37 (Android 17 behaviour changes, test on an API 37 emulator) | Google Play announcing API 37 as the requirement (usually each August) |
| 🔨 | Reminders: re-check `canNotify` on resume (granted from system settings, the prompt stays until restart) and plan the next reminder in a `finally` in `ReminderWorker` (a failed run breaks the chain until the app opens) | — |

## decision-wheel

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Upload v2 (1.1: favourites, launcher shortcuts, restore example wheels) to production, with the updated full description | You · bundle built from master, notes in `changelogs/2.txt` |

## whos-lying

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Upload v5 (editable copies of the included packs, landscape / handheld layouts, plus v4: new icon, share packs by link / QR, fixes) to internal testing and test with 4–5 friends: word pairs, flow, readability | You · bundle built from master, notes in `changelogs/5.txt` |
| ⏳ | A tester got "Item not found" on the opt-in link: likely the first review still pending or the wrong Google account in Play | Their next try |
| ⏳ | Open-source license (GPL-3.0 was the candidate) | Your decision · not licensed for now |
| 🔨 | Pack links: publish `/p/` and the impostor entry of `assetlinks.json` (both ready locally in the website repo; add the Play signing SHA-256 from Play Console → App integrity). Until then packs travel by share-to-app or Import → paste | You · the SHA-256, then a website commit |
| 👤 | New icon (a line-up, the liar in a fedora): upload `store/play-icon-512.png` and the feature graphics to the listing | You · Play Console → Main store listing |

## group-polls (new app)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Create the app in Play Console (`com.jorgelillo.grouppolls`, App · Social); the upload key exists. Listing texts and graphics are ready in `store/metadata`; don't upload a bundle until Firebase exists (without it the app runs on sample polls) | You · answers in `apps/group-polls/OPERATIONS.md` → "Play Console answers" |
| 👤 | Create the Firebase project and give the OK | You · lillorepo `packages/group_polls/OPERATIONS.md` → "Create the project" |
| 🔨 | Then: `firebase.properties`, App Check (Play Integrity + debug token), Play signing SHA-256 in `assetlinks.json`, publish `/q/` and `assetlinks.json` on the website (privacy page already live), Terraform on, internal test | The two steps above · plan in [docs/plans/group-polls.md](docs/plans/group-polls.md) |

## tournaments (new app)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Create the app in Play Console (`com.jorgelillo.tournaments`, App · Tools or Board games) and upload the signed bundle (`build/releases/torneos-v1.aab`) to internal testing. Listing texts, screenshots and feature graphics are ready in `store/metadata` | You |
| 🔨 | Then: Play signing SHA-256 in `assetlinks.json` and publish `/t/` (ready locally in the website repo: shows the tournament in the browser) with it, internal test | The step above · plan in [docs/plans/tournaments.md](docs/plans/tournaments.md) |
| ⏳ | Double elimination and round robin | Feedback after real use |

## league-cards (new game, internal test with the league)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Upload key (`scripts/new-upload-key.sh league-cards`), create the app in Play Console (`com.jorgelillo.leaguecards`, Game · Card), internal testing only, add the tester's Gmail; then the bundle built here with the private deck | You · [OPERATIONS.md](apps/league-cards/OPERATIONS.md) |
| 👤 | Each league member's OK to appear in the private deck | You |
| ⏳ | More cards and chat lines from the group | Ideas from the league · private deck, never in this repo |

## next game (idea)

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | An action game: the slipper runner or a tilt-and-roll puzzle like Kula World | Every current app on the store · ideas in [docs/plans/game-ideas.md](docs/plans/game-ideas.md) |

## repo

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | Move into `lillorepo` (Bazel) | Gradle builds or the Play publishing loop slowing you down · domain modules are pure Kotlin to make it easy |
| 🔨 | Publish from the terminal with Gradle Play Publisher (bundle + notes to a track, listing, promote): move every app's `store/metadata` (fastlane layout) to GPP's own `app/src/main/play`, store-asset generators and release skill included | In progress · then 👤 a Google Cloud service account with Play Console access (one-time) |
| 🔨 | Release smoke tests in CI on an emulator (`reactivecircus/android-emulator-runner`): the release build in CI catches R8 build errors, but only running the app catches a stripped class like WorkManager's | `scripts/env.sh` to work on Linux runners |
| 🔨 | Line length under ktlint (`max_line_length` is off: ~100 Kotlin lines over 150 to wrap by hand) and Compose rules (`io.nlopez.compose.rules` for ktlint) | — |
| 🔨 | Dependency update PRs (Renovate or Dependabot); lint already lists 7 newer versions | — |
| 🔨 | Semantic colours in `LilloTheme` (card, muted, line, accent) so core components stop taking colours per call and the per-app wrappers shrink | — |
| 🔨 | `GlassCard` / `GradientBackground` are Christmas-only: back to that app, or a second user | — |
| 🔨 | One UI-state convention for every ViewModel (`StateFlow` + `collectAsStateWithLifecycle`; some still use `mutableStateOf`) | — |
| 🔨 | Accessibility pass: content descriptions (avatars, emoji buttons), contrast (e.g. the Drifter's guess placeholder), touch targets; TalkBack once per app | — |
| 🔨 | Component catalogue: `@Preview`s of every core component (light/dark, short screen) | — |
| ⏳ | `minSdk` 23 → 24 (blocks WorkManager 2.12; check the Play Console share of API 23 first) | Christmas's install stats |
| ⏳ | Baseline profiles for startup | An app where startup feels slow |
| 👤 | Repo licence (only whos-lying was discussed: GPL-3.0 candidate) | Your decision |
| 🔨 | Type-safe navigation (`@Serializable` routes, Navigation 2.10) instead of string routes in decision-wheel, group-polls, tournaments and whos-lying, one app at a time | — |
| 🔨 | `dataExtractionRules` for the 5 apps with `allowBackup="true"` (only group-polls declares them): say what is restored on a new phone | — |
