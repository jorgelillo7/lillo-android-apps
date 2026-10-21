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
| 👤 | Upload v5 (4.1: new name "¿Ya es Navidad? Cuenta atrás" / "Is it Christmas yet? Countdown", new advent calendar with music days and sharing) and, with it, the new titles, full description and feature graphics from `app/src/main/play/listings` | You · Release workflow (`listing=true` for the new name and graphics) |
| ⏳ | Check v4 on a real phone after Play approval: the 3 new carols (transcribed from memory), share text in both modes, widget | Google's review of v4 |
| 👤 | Spanish carols (Los peces en el río, Campana sobre campana, Fum fum fum, Marimorena) | Reliable public-domain scores from you · *El burrito sabanero* is copyrighted (1972), not allowed |
| ⏳ | Grow the advent pool (activities and songs) between January and November, never during an Advent in progress | Ideas from you · `AdventActivity` / `AdventSong` in the domain |
| ⏳ | Raise `targetSdk` to 37 (Android 17 behaviour changes, test on an API 37 emulator) | Google Play announcing API 37 as the requirement (usually each August) |

## decision-wheel

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Upload v2 (1.1: favourites, launcher shortcuts, restore example wheels) to production, with the updated full description | You · Release workflow (`listing=true`) |

## whos-lying

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Upload v5 / 1.1 (everything since v3: new icon, pack sharing, editable copies, readability, landscape) to internal testing with the Release workflow, with `listing=true` for the new icon and graphics; test with 4–5 friends | You · the service account and `scripts/set-release-secrets.sh --play` / `whos-lying` first |
| ⏳ | A tester got "Item not found" on the opt-in link: likely the first review still pending or the wrong Google account in Play | Their next try |
| 🔨 | Pack links: publish `/p/` and the impostor entry of `assetlinks.json` (both ready locally in the website repo; add the Play signing SHA-256 from Play Console → App integrity). Until then packs travel by share-to-app or Import → paste | You · the SHA-256, then a website commit |
| 👤 | New icon (a line-up, the liar in a fedora): upload `store/play-icon-512.png` and the feature graphics to the listing | You · Play Console → Main store listing |

## group-polls (new app)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Create the app in Play Console (`com.jorgelillo.grouppolls`, App · Social); the upload key exists. Listing texts and graphics are ready in `app/src/main/play/listings`; don't upload a bundle until Firebase exists (without it the app runs on sample polls) | You · answers in `apps/group-polls/OPERATIONS.md` → "Play Console answers" |
| 👤 | Create the Firebase project and give the OK | You · lillorepo `packages/group_polls/OPERATIONS.md` → "Create the project" |
| 🔨 | Then: `firebase.properties`, App Check (Play Integrity + debug token), Play signing SHA-256 in `assetlinks.json`, publish `/q/` and `assetlinks.json` on the website (privacy page already live), Terraform on, internal test | The two steps above · plan in [docs/plans/group-polls.md](docs/plans/group-polls.md) |

## tournaments (new app)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | App created in Play Console: App content forms, then upload the first signed bundle to internal testing by hand. Listing texts, screenshots and feature graphics are ready in `app/src/main/play/listings` | You |
| 🔨 | Then: Play signing SHA-256 in `assetlinks.json` and publish `/t/` (ready locally in the website repo: shows the tournament in the browser) with it, internal test | The step above · plan in [docs/plans/tournaments.md](docs/plans/tournaments.md) |
| ⏳ | Double elimination and round robin | Feedback after real use |

## league-cards (new game, internal test with the league)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | App created, key and release secrets in place: App content forms, upload the first bundle (with the private deck) to internal testing by hand, add the league's Gmail addresses as testers | You · [OPERATIONS.md](apps/league-cards/OPERATIONS.md) |
| 👤 | Each league member's OK to appear in the private deck | You |
| 🔨 | Store feature graphic and phone screenshots (Play shows only the icon and texts now): `tools/generate_store_assets.py` + captures from a build with the **sample** deck, never the private one | Before the listing goes beyond the league |
| ⏳ | More cards and chat lines from the group | Ideas from the league · private deck, never in this repo |

## next game (idea)

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | An action game: the slipper runner or a tilt-and-roll puzzle like Kula World | Every current app on the store · ideas in [docs/plans/game-ideas.md](docs/plans/game-ideas.md) |

## repo

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | Move into `lillorepo` (Bazel) | Gradle builds or the Play publishing loop slowing you down · domain modules are pure Kotlin to make it easy |
| 👤 | Publishing from CI is ready (Release workflow): create the Google Cloud service account and give it access in Play Console, then `scripts/set-release-secrets.sh --play` and `scripts/set-release-secrets.sh <app>` per app, in your terminal (docs/publishing-on-google-play.md §6) | You |
| ⏳ | Release workflow without a stored key: Workload Identity Federation (GitHub OIDC → the `play-publisher` service account), then delete its JSON key | The first CI releases working with the key |
| ⏳ | Christmas Countdown's `minSdk` 23 → 24, the only app still on 23 (blocks WorkManager 2.12 repo-wide; check the Play Console share of API 23 first) | Christmas's install stats |
| ⏳ | Baseline profiles for startup | An app where startup feels slow |
