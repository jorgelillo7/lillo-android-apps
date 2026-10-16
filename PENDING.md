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

## decision-wheel

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Upload v2 (1.1: favourites, launcher shortcuts, restore example wheels) to production, with the updated full description | You · bundle built from master, notes in `changelogs/2.txt` |

## whos-lying

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Upload v5 (editable copies of the included packs, plus v4: new icon, share packs by link / QR, fixes) to internal testing and test with 4–5 friends: word pairs, flow, readability | You · bundle built from master, notes in `changelogs/4.txt` |
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

## repo

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | Move into `lillorepo` (Bazel) | Gradle builds or the Play publishing loop slowing you down · domain modules are pure Kotlin to make it easy |
