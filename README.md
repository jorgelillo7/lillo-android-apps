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
| 🕵️ [Who's lying? · Impostor game](apps/whos-lying) | In development | `apps/whos-lying` |
| 🔴🔵 [What do you vote? · Group polls](apps/group-polls) | In development (needs its Firebase project) | `apps/group-polls` |
| 👑 [Last one standing? · Tournaments](apps/tournaments) | In development | `apps/tournaments` |
| 😭 [¿Quién llora esta jornada? · Fantasy-league card game](apps/league-cards) | In development | `apps/league-cards` |

## 🗂️ Repository layout

```
PENDING.md                         Open work, one line per item
docs/                              Runbook, Google Play guide, decisions, lessons learned (index: docs/README.md)
scripts/                           Shared scripts: env, emulator, UI driver (ui_lib), smoke / capture libs, store graphics
build-logic/                       Gradle convention plugins: Android, Compose, screenshots, Robolectric, publishing
gradle/libs.versions.toml          Single source of truth for every dependency version
core/
├── designsystem/                  Theme and Compose components every app uses (page, buttons, rows, avatar, about…)
├── platform/                      Android helpers without UI: JsonStore, sharing, links, backup rules
├── sharelink/                     Pure Kotlin: any value inside a share link
└── screenshot-testing/            What every screenshot test shares (capture, accessibility checks)
apps/                              One folder per app (table above), all with the same layout:
└── <app>/
    ├── app/                       :apps:<app>:app — Android app (UI, resources)
    │   ├── src/main/play/         Google Play listing and release notes (Gradle Play Publisher)
    │   └── src/test/screenshots/  Screenshot test images, reviewed like code
    ├── domain/                    :apps:<app>:domain — pure Kotlin business logic, unit tested
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
   apps/<app>/tools/smoke_test.sh          # release build on the emulator, for each app touched
   ```
   A deliberate UI change re-records its screenshots (`./gradlew recordRoborazziDebug`); the new
   images go in the PR and are reviewed like code.
3. **A pull request**, checked by CI: **Lint** (format + Android lint), **Test** (unit tests,
   screenshot tests with their accessibility checks), **Release** (R8 build of every app, Play
   listing check) and **Smoke** (every app's release build on an emulator).
4. **Squash merge** when green. Dependency updates arrive the same way, as weekly Renovate PRs.
5. **Releases** follow the `release-android-app` skill: version bump, notes in every language,
   `./gradlew :apps:<app>:app:publishBundle` to internal testing, then production
   ([docs/publishing-on-google-play.md](docs/publishing-on-google-play.md)).

What every app gets from the shared pieces: the same page and components, per-app language,
compact layouts for landscape and handhelds, screenshot and accessibility tests, release smoke
tests, store graphics, publishing from the terminal.

## 🚀 Building

Requirements: Android Studio 2026.2+ and Android SDK 37. See [docs/setup/toolchain.md](docs/setup/toolchain.md).

```bash
source scripts/env.sh                                      # Android Studio's JDK, adb, SDK paths

./gradlew test                                             # unit tests for every module
./gradlew :apps:christmas-countdown:app:installDebug       # run on a device or emulator
./gradlew :apps:christmas-countdown:app:bundleRelease      # Play Store bundle
```

## ➕ Adding a new app

Two Gradle files of a few lines each, plus registering them in `settings.gradle.kts`.
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
