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
| 👤 | Play Console: 1.0 is a **production draft** with its listing (2026-11-11). Fill in App content (answers in `apps/stadium-events/OPERATIONS.md`), then *Publishing overview → Send changes for review*; after the first upload, register the package in Android developer verification with the app signing key | You |
| ⏳ | Try it with the family for a couple of weeks: does the evening reminder arrive, is the widget clear? | Real use |
| 👤 | Release 1.1 with the widget offered inside the app (a tester on MIUI could not find it) and publish store screenshot 05, which shows the widget | You · `release-android-app` skill, `-f listing=true` |

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
| 🔨 | Close the GAPs the specs record (`grep -rn "GAP — unverified" openspec/specs`): the tournaments, league-cards, decision-wheel and design-system ones are plain unit or screenshot-backed behaviour; reboot survival and battery delays need a device | Pick one app at a time |
| ⏳ | Play's new app-quality thresholds (memory and code optimisation) are enforced from February 2027: in January, run `scripts/play_vitals.py` and compare with the thresholds once published (2026-11-07: no app has enough users for Google to report vitals; R8 is on everywhere and the apps are 1–2 MB) | January 2027 |
| ⏳ | Play is expected to require targetSdk 37 from August 2027; the apps target it on `master` since 2026-11 and reach users with the releases above. In-App Review in `core`, shown after a good moment, is still an idea: first check with `scripts/verify-bundle.sh` that it adds no INTERNET permission to the apps that promise none | The releases above |
| 🔨 | **Release the seven live apps** — nothing from November is on Play yet. Per app, on a branch `release/<app>-<version>`: bump `versionCode`/`versionName`, write `play/release-notes/{en-US,es-ES}/default.txt` and a row in `release-notes.md`, regenerate the store screenshots from the new Roborazzi images (they still show the old look), run the release smoke test; then the Release workflow `upload → internal` and `promote → production`, which waits for your approval. **christmas-countdown first: Advent starts 1 December.** On `master` and waiting to ship: light and dark looks for decision-wheel, christmas-countdown, whos-lying and tournaments; a spin and a slipper run that survive rotation; a whos-lying round that survives Android stopping the app; tournaments' round-by-round bracket and its question before a newer copy replaces yours; stadium-events' widget offered in the app (1.1, with store screenshot 05); stored data set aside instead of overwritten when unreadable; share links checked before anything is stored | A session per app or one for all · `release-android-app` skill · baseline profiles need the emulator and now name removed classes in the redesigned apps |
| 🔨 | **Finish the new looks**: league-cards (and its rules collapsed for a returning player), slipper (it has no dark look), stadium-events (move it onto `LilloStyle`/`LilloPalette` and the look choice) and group-polls; then remove the thin `Page`/`BigButton` wrappers left in whos-lying's and tournaments' `ui/Common.kt`. The four done apps are the model; each gets a contact sheet for you (`python3 scripts/contact_sheet.py <app> <screen>… --old-ref 1db987f --out <png>`) | Pick up, one branch per app · you validate each sheet before its release |
| 👤 | Look at the new looks on a real phone before releasing: whos-lying's round stages stay night-coloured in the light look, so the status-bar icons may be dark over violet or red; tournaments' exported bracket image and christmas-countdown's widget keep their night colours in both looks; the light palettes were chosen by hand and checked only by the contrast tests | You, with a debug build · `./gradlew :apps:<app>:app:installDebug` |
| ⏳ | Release workflow without a stored key: Workload Identity Federation (GitHub OIDC → the `play-publisher` service account), then delete its JSON key | The first CI releases working with the key |
| 👤 | Baseline profiles are in every app (but group-polls), 2026-10: measure the cold start on a real phone (USB debugging on, `./gradlew :apps:slipper:baselineprofile:connectedBenchmarkReleaseAndroidTest`, then christmas-countdown). **It reinstalls the app with the upload key and removes it afterwards: the phone's Play copy and its data go** (use a phone without them, or accept losing that app's data). The emulator shows no difference on this Mac; Google's figure is ~20–30 % on real phones. Also more journeys (tournaments with data, whos-lying past the welcome) | You, with a phone at hand |
