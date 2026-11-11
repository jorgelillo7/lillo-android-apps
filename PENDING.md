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
| ⏳ | Grow the advent pool (activities and songs) between January and November, never during an Advent in progress | Ideas from you · `AdventActivity` / `AdventSong` in the domain |
| ⏳ | Spanish carols (villancicos) for the advent songs | Public-domain scores to be found |

## group-polls (new app, parked until 2026-10-23)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Create the app in Play Console (`com.jorgelillo.grouppolls`, App · Social); the upload key exists. Listing texts and graphics are ready in `app/src/main/play/listings`; don't upload a bundle until Firebase exists (without it the app runs on sample polls) | You · answers in `apps/group-polls/OPERATIONS.md` → "Play Console answers" |
| 👤 | Create the Firebase project and give the OK | You · lillorepo `packages/group_polls/OPERATIONS.md` → "Create the project" |
| 🔨 | Then: `firebase.properties`, App Check (Play Integrity + debug token), Play signing SHA-256 in `assetlinks.json`, publish `/q/` and `assetlinks.json` on the website (privacy page already live), Terraform on, internal test | The two steps above · plan in [docs/plans/group-polls.md](docs/plans/group-polls.md) |

## stadium-events (new app, v1 built 2026-11; feed live at `jorgelillo7.github.io/stadium/events.json`)

| | What is missing | Waiting on |
|---|---|---|
| 👤 | Play Console: 1.0 is a **production draft** with its listing (2026-11-11). Fill in App content (answers in `apps/stadium-events/OPERATIONS.md`), then *Publishing overview → Send changes for review*; after the first upload, register the package in Android developer verification with the app signing key | You |
| ⏳ | Try it with the family for a couple of weeks: does the evening reminder arrive, is the widget clear? | Real use |

## tournaments (new app)

| | What is missing | Waiting on |
|---|---|---|
| ⏳ | Double elimination and round robin | Feedback after real use |

## league-cards (new game, live with the private deck)

| | What is missing | Waiting on |
|---|---|---|
| 🔨 | The store screenshots show the sample deck; retake them with the league's deck if wanted | You |
| ⏳ | More cards and chat lines from the group | Ideas from the league · private deck, never in this repo |

## repo

| | What is missing | Waiting on |
|---|---|---|
| 🔨 | Write each app's behaviour specs in `openspec/specs/{app}/` (and `core/`), each scenario naming the Kotlin test that proves it; the scaffold and map are in `openspec/project.md` | Pick one app at a time |
| ⏳ | Play's new app-quality thresholds (memory and code optimisation) are enforced from February 2027: in January, run `scripts/play_vitals.py` and compare with the thresholds once published (2026-11-07: no app has enough users for Google to report vitals; R8 is on everywhere and the apps are 1–2 MB) | January 2027 |
| 🔨 | The six live apps' Play builds predate targetSdk 37 (2026-11) and the AGP 9.5 toolchain: add something worthwhile to each one and release it, so users get them. Candidates: Play's In-App Review once in `core`, shown after a good moment (first check with `verify-bundle.sh` that it adds no INTERNET permission to the apps that promise none); review that each app's first store screenshot says why to install it. No rush: Play is expected to require 37 from Aug 2027 | Your ideas for each app · `release-android-app` skill |
| ⏳ | Release workflow without a stored key: Workload Identity Federation (GitHub OIDC → the `play-publisher` service account), then delete its JSON key | The first CI releases working with the key |
| 👤 | Baseline profiles are in every app (but group-polls), 2026-10: measure the cold start on a real phone (USB debugging on, `./gradlew :apps:slipper:baselineprofile:connectedBenchmarkReleaseAndroidTest`, then christmas-countdown). **It reinstalls the app with the upload key and removes it afterwards: the phone's Play copy and its data go** (use a phone without them, or accept losing that app's data). The emulator shows no difference on this Mac; Google's figure is ~20–30 % on real phones. Also more journeys (tournaments with data, whos-lying past the welcome) | You, with a phone at hand |
