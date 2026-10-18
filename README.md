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
docs/                              Runbook (operations.md), Google Play guide, setup/toolchain, adding an app
scripts/                           Shared scripts: env, emulator, screenshots, bundle verification
branding/                          Developer profile images (Play Console) and their generator
build-logic/                       Gradle convention plugins: shared Android/Kotlin/Compose config
gradle/libs.versions.toml          Single source of truth for every dependency version
core/
└── designsystem/                  :core:designsystem — theme and Compose components shared by all apps
apps/                              One folder per app (table above), all with the same layout:
└── <app>/
    ├── app/                       :apps:<app>:app — Android app (UI, resources)
    ├── domain/                    :apps:<app>:domain — pure Kotlin business logic
    ├── store/                     Play Store listing: texts and images per language
    ├── tools/                     App-specific scripts (icon, store assets, smoke test)
    ├── README.md                  What the app does and how it is built
    ├── OPERATIONS.md              Every command to run, test and release the app
    └── release-notes.md           Version history
```

### Principles

- **Each app is a self-contained folder** under `apps/`, with its own `app` and `domain` modules.
- **Domain modules are pure Kotlin** (no `android.*`): fast to test and easy to move to another
  build system later.
- **Shared code lives in `core/`, but only once a second app needs it.** No empty modules "just
  in case".
- **Configuration is written once** in `build-logic` convention plugins and the version catalog.

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

Start at [docs/README.md](docs/README.md): publishing checklist, toolchain notes, lessons learned.

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
