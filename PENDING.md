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
| 🔨 | Then: `firebase.properties`, App Check (Play Integrity + debug token), the Firebase API key restricted to this package and both signing SHA-1s and to the APIs the app calls (lillorepo `PENDING.md`), Play signing SHA-256 in `assetlinks.json`, publish `/q/` and `assetlinks.json` on the website (privacy page already live), Terraform on, internal test | The two steps above · plan in [docs/plans/group-polls.md](docs/plans/group-polls.md) |

## stadium-events (new app, v1 built 2026-11; feed live at `jorgelillo7.github.io/stadium/events.json`)

| | What is missing | Waiting on |
|---|---|---|
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
| ⏳ | The specs' two remaining GAPs can't be tested here: reminders surviving a reboot and battery delays (WorkManager on a device) and league-cards' private deck (never in this repo) | A phone · the private deck |
| ⏳ | Play's new app-quality thresholds (memory and code optimisation) are enforced from February 2027: in January, run `scripts/play_vitals.py` and compare with the thresholds once published (2026-11-07: no app has enough users for Google to report vitals; R8 is on everywhere and the apps are 1–2 MB) | January 2027 |
| ⏳ | Play is expected to require targetSdk 37 from August 2027; the apps target it on `master` since 2026-11 and reach users with the releases above. In-App Review in `core`, shown after a good moment, is still an idea: first check with `scripts/verify-bundle.sh` that it adds no INTERNET permission to the apps that promise none | The releases above |
| ⏳ | The seven releases of 2026-11-13 are in Google's review (production 9, 6, 11, 5, 2, 7, 3): once live, install christmas-countdown from Play on a phone. What merged after them (the share-size guard, confetti and Remove animations, group-polls' terms) ships with each app's next release, which regenerates its baseline profile | Google's review |
| 👤 | Look at the new looks on a real phone before releasing (now every app has two: slipper's night scene, league-cards' day chat with the deck's names darkened): whos-lying's round stages stay night-coloured in the light look, so the status-bar icons may be dark over violet or red; tournaments' exported bracket image and christmas-countdown's widget keep their night colours in both looks; the light palettes were chosen by hand and checked only by the contrast tests | You, with a debug build · `./gradlew :apps:<app>:app:installDebug` |
| 🔨 | Release slipper, league-cards and stadium-events with their two looks (and whos-lying and tournaments with the share-size guard): version, notes, store screenshots in the light look with one night shot (`look dark` in the capture scripts), baseline profile | After the phone check · `release-android-app` skill |
| ⏳ | Release workflow without a stored key: Workload Identity Federation (GitHub OIDC → the `play-publisher` service account), then delete its JSON key | The first CI releases working with the key |
| 👤 | Baseline profiles are in every app (but group-polls), 2026-10: measure the cold start on a real phone (USB debugging on, `./gradlew :apps:slipper:baselineprofile:connectedBenchmarkReleaseAndroidTest`, then christmas-countdown). **It reinstalls the app with the upload key and removes it afterwards: the phone's Play copy and its data go** (use a phone without them, or accept losing that app's data). The emulator shows no difference on this Mac; Google's figure is ~20–30 % on real phones. Also more journeys (tournaments with data, whos-lying past the welcome) | You, with a phone at hand |
