# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

A Gradle multi-project hosting several small Android apps published on Google Play. Each app is a
self-contained folder under `apps/<app>/`; shared Compose code lives in `core/`; shared build
configuration lives in `build-logic/`. Human docs are in `docs/` (index `docs/README.md`, repo runbook `docs/operations.md`); open work in `PENDING.md`;
each app has its own `apps/<app>/OPERATIONS.md` (exact commands) and `release-notes.md` (version history).

Apps today: `christmas-countdown` ("¿Ya es Navidad?", `com.jorgelillo.christmascountdown`) and
`decision-wheel` ("¿Qué hacemos?", `com.jorgelillo.decisionwheel`), both live on Play; `whos-lying` ("Who's lying?",
`com.jorgelillo.whoslying`, internal testing), `group-polls` ("¿Qué votáis?",
`com.jorgelillo.grouppolls`, not released) and `tournaments` ("¿Solo quedará uno?",
`com.jorgelillo.tournaments`, in development) and `league-cards` ("¿Quién llora esta jornada?",
`com.jorgelillo.leaguecards`, a card game; its league deck is private and lives outside this public repo). `group-polls` is the only one with a backend
(Firestore; its rules, tests and infra live in lillorepo `packages/group_polls`). Write code,
docs and scripts so they work for any app.

## Environment

Always `source scripts/env.sh` before running Gradle from a terminal: it points `JAVA_HOME` at
Android Studio's bundled JDK (the shell's default Homebrew JDK is too new for Gradle) and exports
`ADB`/`ANDROID_HOME`.

The machine has 8 GB RAM: emulator + Android Studio + Gradle don't fit together. Stop the emulator
(`scripts/emulator.sh stop`) during long builds and run emulator scripts in the foreground
(background jobs get killed under memory pressure).

## Commands

Module paths follow the folders: `:apps:<app>:app`, `:apps:<app>:domain`, `:core:<name>`.

```bash
./gradlew test                                            # every unit test in the repo
./gradlew :apps:<app>:domain:test --tests '*SomeTest.someMethod*'   # single test
./gradlew :apps:<app>:app:lintDebug                       # lint (kept at 0 warnings)
./gradlew spotlessApply                                   # format Kotlin (ktlint, .editorconfig); CI runs spotlessCheck
./gradlew recordRoborazziDebug                            # re-record screenshot tests after a deliberate UI change; CI runs verifyRoborazziDebug
./gradlew :apps:<app>:app:installDebug                    # run on emulator/phone
./gradlew :apps:<app>:app:bundleRelease                   # Play bundle (signed if apps/<app>/keystore.properties exists)

scripts/emulator.sh start|stop|date MMDDhhmmYYYY|date auto|demo on|off
scripts/screenshot.sh <out.png>                           # dismisses "isn't responding" dialogs first
scripts/tap-text.sh "<label|test tag>"                    # tap by on-screen text or Compose test tag (never coordinates)
scripts/verify-bundle.sh <aab>                            # signature, versionCode, permissions, native libs
apps/<app>/tools/smoke_test.sh                            # release APK on emulator (per app)
```

Asset generators (icons, store graphics, developer profile) are Python + Pillow:
`scripts/setup-python.sh` once, then `.venv/bin/python <script>`. Generated images are committed;
regenerate them instead of editing by hand. Shared pieces: `scripts/store_assets.py` (store
graphics), `scripts/smoke_lib.sh` (smoke tests) and `scripts/ui_lib.py` (drive any app by text or test
tag, keyboard, rotation, screenshots: what `tap-text.sh`, `screenshot.sh` and UI batteries such as
`apps/whos-lying/tools/battery_test.py` run on); each app's `tools/` only holds its own part.

Project skills in `.claude/skills/`: `new-android-app` (competitor study → design → scaffold),
`upgrade-android-deps` (toolchain/dependency upgrades) and `release-android-app` (ship a version).

## Build architecture

- **Version catalog** `gradle/libs.versions.toml` is the single source of truth for every version,
  including `compileSdk`/`targetSdk`/`minSdk`, which the convention plugins read from it.
- **Convention plugins** in `build-logic/convention` (included build): `jorgelillo.android.application`,
  `jorgelillo.android.library`, `jorgelillo.android.compose`, `jorgelillo.jvm.library`. They set
  SDK levels, Java 17 target and core library desugaring, so module build files only declare
  namespace, app id/version, signing and dependencies.
- **AGP 9 has Kotlin built in**: never apply `org.jetbrains.kotlin.android`. The Compose compiler
  plugin is still needed (applied by the compose convention plugin).
- AndroidX versions are chained to compileSdk/AGP (e.g. Compose 1.12+ needs compileSdk 37 and
  AGP 9.1+). If `checkDebugAarMetadata` fails, bump the platform/AGP, not a single library.
- Type-safe project accessors are on: `implementation(projects.apps.<appCamel>.domain)`,
  `implementation(projects.core.designsystem)`. New modules must be included in `settings.gradle.kts`.

## App architecture (conventions for every app)

- `:apps:<app>:domain` is **pure Kotlin, no `android.*`**: all business rules live here, unit
  tested with `kotlin.test`. This keeps them portable (a later move into the `lillorepo` Bazel
  monorepo starts with these modules).
- `:apps:<app>:app`: Compose + Material 3, MVVM (`ViewModel` + `StateFlow`, collected with
  `collectAsStateWithLifecycle`). No DI framework: app-wide singletons live on the app's
  `Application` subclass and reach ViewModels through `viewModelFactory` companions.
- `:core:designsystem` holds `LilloTheme` (each app passes its own `ColorScheme`) and what two or
  more apps share: `LilloPage` (with `sideActionsWhenShort`) / `LilloBigButton` (apps wrap them with
  their colours in `ui/…/Common.kt`), `LilloAboutSheet` (language, rate, privacy),
  `LilloLanguageDialog`, `isShortScreen()` for landscape and handhelds, `TestTagsAsResourceIds`,
  `ConfettiBurst` / `ConfettiRain`, `LilloAvatar`, `LilloSettingRow`, `LilloSectionTitle`, `LilloQrCode`. `:core:platform` has
  `JsonStore`, sharing and links; `:core:sharelink` (pure Kotlin) puts any serializable value in a
  `jorgelillo7.github.io/<path>/#…` link (tournaments `/t/`, impostor packs `/p/`).
- **Reuse first; the second copy goes to core.** Every app should get what the others already
  proved. Before writing a composable, helper, Gradle setting or script, look for it in `core/`,
  `build-logic/`, `scripts/` and the other apps (`git grep`). If it is in core, use it. If another
  app has it, move it to core (or `build-logic` / `scripts`) **in the same change** and switch both
  apps over — never copy it. A one-off stays in the app; a second one never does.
- Strings ship in English (`values/`) and Spanish (`values-es/`); keep both in sync, including
  plurals (Spanish needs `one`/`many`/`other`).
- **Screenshot tests** (Roborazzi on Robolectric, no emulator): a module applies
  `jorgelillo.android.screenshots` and writes tests with `captureScreenshot` from
  `:core:screenshot-testing` (frozen clock, shared tolerance), upright and `@Config(qualifiers = "+land")`.
  Images live in `src/test/screenshots/` and are reviewed like code. Unit tests that need Android
  classes use `jorgelillo.android.robolectric`.
- Snapshot tests pin anything users already see that is derived algorithmically (e.g. Christmas
  Countdown's yearly advent door order and `AdventPlan` contents); don't change those algorithms. Add
  advent activities or songs only outside December: later years are recomputed from the pool.

## Releasing (any app)

- **Always test the release build** (`apps/<app>/tools/smoke_test.sh`), not just debug: R8 strict
  full mode (AGP 9 default) once stripped WorkManager's `WorkDatabase_Impl` constructor and the app
  crashed on start. Any app using WorkManager/Glance/Room needs the `RoomDatabase` keep rule
  (see `apps/christmas-countdown/app/proguard-rules.pro`).
- Signing reads `apps/<app>/keystore.properties` (git-ignored, values trimmed). Never commit it or
  any `.jks`; check `git status` for filename variants (a leading-space copy once slipped in).
- Each release bumps `versionCode` in `apps/<app>/app/build.gradle.kts`, rewrites
  `apps/<app>/app/src/main/play/release-notes/{en-US,es-ES}/default.txt` and adds a row to
  `apps/<app>/release-notes.md` (the history).
- Store listing texts/images live in `apps/<app>/app/src/main/play/listings/<locale>/` (Gradle Play
  Publisher, applied by the convention plugin). Publish from the terminal: `:apps:<app>:app:publishBundle`
  (internal testing), `promoteArtifact --from-track internal --promote-track production`,
  `publishListing` (docs/publishing-on-google-play.md §6; needs the service account key). Publish
  the listing together with the release that adds what it describes.
- `targetSdk` must meet Google Play's current requirement (API 36 since Aug 2026).
- Share links and App Links use `jorgelillo7.github.io` (`/q/`, `/t/`, `/p/` and
  `/.well-known/assetlinks.json` live in that repo): free static GitHub Pages, data in the link
  fragment, no server. The domain is tied to the GitHub username, which the user keeps for good,
  so no custom domain is planned; changing it would break every link already shared.
- Privacy policies are served from the separate repo `jorgelillo7/jorgelillo7.github.io`
  (`/privacy/<app>/`), never from this repo, so renaming this repo can't break them.
