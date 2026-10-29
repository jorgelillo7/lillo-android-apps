<div align="center">

# 📱 Lillo Android Apps

**A home for small, polished Android apps — built with Kotlin, Jetpack Compose and shared building blocks.**

![Kotlin](https://img.shields.io/badge/Kotlin-2.4-7F52FF?logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)
![AGP](https://img.shields.io/badge/AGP-9.4-02303A?logo=gradle&logoColor=white)
![Target SDK](https://img.shields.io/badge/targetSdk-36-3DDC84?logo=android&logoColor=white)

</div>

---

## 🧩 Apps

| App | Status | Folder |
|---|---|---|
| 🎄 [Is it Christmas yet? · Countdown](apps/christmas-countdown) | [On Google Play](https://play.google.com/store/apps/details?id=com.jorgelillo.christmascountdown) | `apps/christmas-countdown` |
| 🎡 [What next? · Decision Wheel](apps/decision-wheel) | [On Google Play](https://play.google.com/store/apps/details?id=com.jorgelillo.decisionwheel) | `apps/decision-wheel` |
| 🕵️ [Who's lying? · Impostor game](apps/whos-lying) | [On Google Play](https://play.google.com/store/apps/details?id=com.jorgelillo.whoslying) | `apps/whos-lying` |
| 👑 [Last one standing? · Tournaments](apps/tournaments) | [On Google Play](https://play.google.com/store/apps/details?id=com.jorgelillo.tournaments) | `apps/tournaments` |
| 😭 [¿Quién llora esta jornada? · Fantasy-league card game](apps/league-cards) | [On Google Play](https://play.google.com/store/apps/details?id=com.jorgelillo.leaguecards) (its league deck is private, outside this repo) | `apps/league-cards` |
| 🩴 [¿Te pilla la chancla? · Slipper runner](apps/slipper) | Ready, first release pending | `apps/slipper` |
| 🔴🔵 [What do you vote? · Group polls](apps/group-polls) | Parked until its Firebase project exists | `apps/group-polls` |

## 🗂️ Repository layout

```
PENDING.md                         Open work, one line per item
docs/                              Runbook, Google Play guide, decisions, lessons learned (index: docs/README.md)
scripts/                           Shared scripts: env, emulator, UI driver (ui_lib), smoke / capture libs, store graphics
build-logic/                       Gradle convention plugins: Android, Compose, screenshots, Robolectric, baseline profiles, publishing
gradle/libs.versions.toml          Single source of truth for every dependency version
core/
├── designsystem/                  Theme and Compose components every app uses (page, buttons, rows, avatar, about…)
├── platform/                      Android helpers without UI: JsonStore, sharing, links, backup rules
├── sharelink/                     Pure Kotlin: any value inside a share link
├── screenshot-testing/            What every screenshot test shares (capture, accessibility checks)
├── testing/                       What every unit test shares (main dispatcher rule, Turbine helpers)
└── baselineprofile/               Baseline profile generator and startup Macrobenchmark every app builds on
apps/                              One folder per app (table above), all with the same layout:
└── <app>/
    ├── app/                       :apps:<app>:app — Android app (UI, resources)
    │   ├── src/main/play/         Google Play listing and release notes (Gradle Play Publisher)
    │   └── src/test/screenshots/  Screenshot test images, reviewed like code
    ├── domain/                    :apps:<app>:domain — pure Kotlin business logic, unit tested
    ├── baselineprofile/           :apps:<app>:baselineprofile — the app's startup and main journeys, for its baseline profile
    ├── store/                     Raw screenshots and the 512 px icon the listing graphics come from
    ├── tools/                     App-specific scripts (icon, store graphics, captures, smoke test)
    ├── README.md                  What the app does and how it is built
    ├── OPERATIONS.md              Every command to run, test and release the app
    └── release-notes.md           Version history
```

### Principles

- **Each app is a self-contained folder** under `apps/`, with its own `app` and `domain` modules.
- **Domain modules are pure Kotlin** (no `android.*`): fast to test and easy to move to another
  build system later.
- **Reuse first.** Before writing something, look in `core/`, `build-logic/`, `scripts/` and the
  other apps; the second copy of anything moves to `core` in the same change.
- **Configuration is written once** in `build-logic` convention plugins and the version catalog.
- **Community-standard tools**, adopted as they are meant to be used. The choices and their reasons:
  [docs/decisions.md](docs/decisions.md).

## 🛠️ How we work

Every change goes the same way, small enough to review in one sitting:

1. **A branch per change**, never straight to `master`.
2. **Locally, before the PR:**
   ```bash
   ./gradlew spotlessApply                 # format (ktlint + Compose rules)
   ./gradlew test verifyRoborazziDebug lintDebug
   ```
   Every app has screenshot tests, a ViewModel test and a whole-flow test through its real
   `MainActivity`, all on the JVM (seconds, no emulator).
   A deliberate UI change re-records its screenshots (`./gradlew recordRoborazziDebug`); the new
   images go in the PR and are reviewed like code.
3. **A pull request**, checked by CI (all four required): **Lint** (format + Android lint),
   **Test** (unit, flow and screenshot tests with their accessibility checks), **Release** (R8
   build of every app, Play listing check) and **Smoke** (the release build of each app the PR
   touches, on an emulator; every app when shared code changes). Gradle's build cache, saved from
   `master`, means untouched modules aren't rebuilt.
4. **Squash merge** when green. Dependency updates arrive the same way, as weekly Renovate PRs.
5. **Releases**: a PR with the version bump and the notes in every language (the
   `release-android-app` skill), then the **Release** workflow publishes it from `master`: to
   internal testing, promoted to production, or straight to production (which waits for an
   approval), store listing included. No Play Console visit after an app's first release
   ([docs/publishing-on-google-play.md](docs/publishing-on-google-play.md#6-release)).

What every app gets from the shared pieces: the same page and components, per-app language,
compact layouts for landscape and handhelds, readable width and two panes on tablets, edge-to-edge
and predictive back, screenshot, accessibility, ViewModel and flow tests, release smoke tests,
baseline profiles, phone and tablet store graphics, publishing from CI.

## 🚀 Building

Requirements: Android Studio 2026.2+ and Android SDK 37. See [docs/setup/toolchain.md](docs/setup/toolchain.md).

```bash
source scripts/env.sh                                      # Android Studio's JDK, adb, SDK paths

./gradlew test                                             # unit tests for every module
./gradlew :apps:christmas-countdown:app:installDebug       # run on a device or emulator
./gradlew :apps:christmas-countdown:app:bundleRelease      # Play Store bundle
```

## ➕ Adding a new app

Three Gradle files of a few lines each (app, domain, baseline profile), plus registering them in
`settings.gradle.kts`.
Full recipe (modules, icon, privacy policy, store assets): [docs/adding-a-new-app.md](docs/adding-a-new-app.md).

## 📚 Docs

Start at [docs/README.md](docs/README.md): publishing checklist, decisions, toolchain notes, lessons learned.

## 🔐 Signing

Each app reads its release signing values from a git-ignored
`apps/<app>/keystore.properties`:

```properties
storeFile=/absolute/path/to/upload-key.jks
storePassword=...
keyAlias=...
keyPassword=...
```

## 🧭 Roadmap

- **Now:** one repo, Gradle, more apps added as folders under `apps/`.
- **Later:** possible move into a larger monorepo. The pure Kotlin domain modules are the parts
  designed to migrate first.

## ⚖️ License

The code is [MIT](LICENSE), like [lillorepo](https://github.com/jorgelillo7/lillorepo): reuse it freely.
The apps' names, icons, store graphics and screenshots are not covered: all rights reserved.
