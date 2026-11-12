# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

The owner's habits that hold in every repo (language, git and PRs, comments, working style) are
personal rules in `~/.claude/rules/`, versioned in lillorepo `system/rules/`; a session that cannot
read `~/.claude/` reads them there. This file holds only what is true of this repo.

## What this repo is

A Gradle multi-project hosting several small Android apps published on Google Play. Each app is a
self-contained folder under `apps/<app>/`; shared Compose code lives in `core/`; shared build
configuration lives in `build-logic/`. Human docs are in `docs/` (index `docs/README.md`, repo runbook `docs/operations.md`); open work in `PENDING.md`;
each app has its own `apps/<app>/OPERATIONS.md` (exact commands), `release-notes.md` (version history) and
`DESIGN.md` (its palette and components, on the shared system in `docs/design.md`; read both before changing colours).

Apps today, live on Play: `christmas-countdown` ("¿Ya es Navidad?", `com.jorgelillo.christmascountdown`),
`decision-wheel` ("¿Qué hacemos?", `com.jorgelillo.decisionwheel`), `whos-lying` ("¿Quién miente?",
`com.jorgelillo.whoslying`), `tournaments` ("¿Solo quedará uno?", `com.jorgelillo.tournaments`),
`league-cards` ("¿Quién llora esta jornada?", `com.jorgelillo.leaguecards`, a card game; its league deck
is private and lives outside this public repo) and `slipper` ("¿Te pilla la chancla?",
`com.jorgelillo.slipper`, a pixel-art runner). Not on Play yet: `stadium-events` ("¿Hay jaleo?
Metropolitano", `com.jorgelillo.stadiumevents`, 1.0 waiting as a production draft; it reads a static
events feed from `jorgelillo7.github.io`) and `group-polls` ("¿Qué votáis?",
`com.jorgelillo.grouppolls`, parked). `group-polls` is the only one with a backend
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
./gradlew spotlessApply                                   # format Kotlin (ktlint + Compose rules, 120 cols); CI runs spotlessCheck
./gradlew recordRoborazziDebug                            # re-record screenshot tests after a deliberate UI change; CI runs verifyRoborazziDebug
./gradlew :apps:<app>:app:installDebug                    # run on emulator/phone
./gradlew :apps:<app>:app:bundleRelease                   # Play bundle (signed if apps/<app>/keystore.properties exists)

scripts/emulator.sh start|stop|date MMDDhhmmYYYY|date auto|demo on|off
scripts/screenshot.sh <out.png>                           # dismisses "isn't responding" dialogs first
scripts/tap-text.sh "<label|test tag>"                    # tap by on-screen text or Compose test tag (never coordinates)
scripts/verify-bundle.sh <aab>                            # signature, versionCode, permissions, native libs
scripts/generate-baseline-profiles.sh <app|all>           # regenerate baseline profiles on the emulator (before a release)
./gradlew :apps:<app>:baselineprofile:connectedBenchmarkReleaseAndroidTest   # cold start without/with the profile
apps/<app>/tools/smoke_test.sh                            # release APK on emulator (per app; CI's Smoke workflow runs the apps a PR touches, each in its own job on the ATD image)
```

Asset generators (icons, store graphics, developer profile) are Python + Pillow:
`scripts/setup-python.sh` once, then `.venv/bin/python <script>`. Generated images are committed;
regenerate them instead of editing by hand. Shared pieces: `scripts/store_assets.py` (store
graphics), `scripts/smoke_lib.sh` (smoke tests), `scripts/capture_lib.sh` (store screenshot captures:
demo mode, locales, clean install, restore on exit; `CAPTURE_DEVICE=tablet` for the tablet ones), both on `scripts/ui_lib.sh` (tap, type_in,
hide_keyboard, swipe_up/down, back, on_screen, wait_for, bounds/in_box/swipe_in for gestures inside one element), and `scripts/ui_lib.py` (drive any app by text or test tag,
keyboard, rotation, screenshots: what the shell helpers, `tap-text.sh`, `screenshot.sh` and UI batteries
such as `apps/whos-lying/tools/battery_test.py` run on); each app's `tools/` only holds its own part.

Project skills are indexed in [`.claude/CLAUDE.md`](.claude/CLAUDE.md).

## Working on a change

- **Decisions**: `docs/decisions.md` lists the choices that shape the repo, with their reason.
  Read it before changing a convention; add a line when a new decision is taken.
- **Behaviour**: `openspec/` states what each app must keep doing (`openspec/project.md` is the
  map). A new feature is agreed in `openspec/changes/{name}/` before it is written; a change to
  behaviour updates its spec in the same PR, and each scenario names the test that proves it.
  `python3 scripts/check_specs.py` fails when a scenario names a test that does not exist (CI's
  Lint job runs it).
- **Before the PR**: `./gradlew spotlessApply`, then `test verifyRoborazziDebug lintDebug`, and
  the release smoke test of every app touched. A new or changed screen gets a screenshot test,
  upright and `+land`; re-record on purpose and review the images.
- **One UI run on the emulator at a time** (smoke, battery, captures). A run moved to the
  background by a timeout is still running: wait for its notification before starting another.
- Each PR goes through CI (Lint, Test, Release, Smoke) and is squash-merged.

## Build architecture

- **Version catalog** `gradle/libs.versions.toml` is the single source of truth for every version,
  including `compileSdk`/`targetSdk`/`minSdk` (the libraries' floor) and `appMinSdk` (24, every
  app's), which the convention plugins read from it.
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
  tested with `kotlin.test`, fast on the JVM.
- `:apps:<app>:app`: Compose + Material 3 (its composables are `internal`; public API lives in core), MVVM (`ViewModel` + `StateFlow`, collected with
  `collectAsStateWithLifecycle`; domain state immutable, replaced on change — never a mutable object
  Compose can't observe). Navigation uses type-safe `@Serializable` routes (`composable<Route>`,
  `toRoute()`), never route strings. No DI framework: app-wide singletons live on the app's
  `Application` subclass and reach ViewModels through `viewModelFactory` companions.
- `:core:designsystem` holds `LilloTheme` (an app passes a light and a dark `LilloStyle`, each a
  `ColorScheme` plus a `LilloPalette` read through `LilloColors.current`: background, hero, brand,
  accent text; the look follows the phone unless the user picks one, `LilloThemeMode`; apps not yet
  moved pass one `ColorScheme`), the display face (`LilloDisplayFamily`, Figtree, in the display,
  headline and `titleLarge` styles) and what two or
  more apps share: `LilloPage` (with `sideActionsWhenShort`) / `LilloBigButton` / `LilloSecondaryButton` (they take the
  app's background and brand from `LilloColors`), `LilloAboutSheet` (appearance, language, rate, privacy),
  `LilloLanguageDialog`, `isShortScreen()` for landscape and handhelds, `isWideScreen()` for two panes (tablets in landscape), `TestTagsAsResourceIds`,
  `ConfettiBurst` / `ConfettiRain`, `LilloAvatar`, `LilloSummaryHero` (opt-in home header: gradient, state at a glance, chips), `LilloScreenHeader` (the light header), `Modifier.accentEdge` (a card's state or kind as a coloured edge), `Modifier.entrance` / `animationsEnabled()` (motion that honours "Remove animations"), `LilloSettingRow`, `LilloSectionTitle`, `LilloQrCode`, `LilloWidgetCard` (an app's home-screen widget: a button when the launcher accepts the request, the way by hand always). Each core component has a
  `@Preview` in `core/designsystem/.../Catalogue.kt`; `CatalogueScreenshotTest` captures every preview there
  in both looks (`_light` added to the light one; with the accessibility checks), so a new component needs a preview, not a hand-written screenshot test. `:core:platform` has
  `JsonStore` (an unreadable stored document is set aside, never overwritten; each app's `StoredStateTest` pins its released format), `AppWidgetPins` (is the app's widget placed, can the launcher be asked to place it), sharing and links; `:core:reminders` has notifications (`LilloNotifications`: channels,
  permission check, tap to open) and reminder chains (`LilloSchedule.at`: one WorkManager job at a time); `:core:sharelink` (pure Kotlin) puts any serializable value in a
  `jorgelillo7.github.io/<path>/#…` link (tournaments `/t/`, impostor packs `/p/`).
- **Reuse first; the second copy goes to core.** Every app should get what the others already
  proved. Before writing a composable, helper, Gradle setting or script, look for it in `core/`,
  `build-logic/`, `scripts/` and the other apps (`git grep`). If it is in core, use it. If another
  app has it, move it to core (or `build-logic` / `scripts`) **in the same change** and switch both
  apps over — never copy it. A one-off stays in the app; a second one never does.
- Strings ship in English (`values/`) and Spanish (`values-es/`); keep both in sync, including
  plurals (Spanish needs `one`/`many`/`other`).
- Around suspend calls, catch with `runCatchingCancellable` (`:core:platform`), never `runCatching`:
  a cancelled coroutine must stop, not read as a failure.
- **Screenshot tests** (Roborazzi on Robolectric, no emulator): a module applies
  `jorgelillo.android.screenshots` and writes tests with `captureScreenshot` from
  `:core:screenshot-testing` (frozen clock, shared tolerance), upright and `@Config(qualifiers = "+land")`.
  Images live in `src/test/screenshots/` and are reviewed like code. Every capture also runs Google's
  Accessibility Test Framework: labels, touch targets and text contrast (WCAG AA, 4.5:1) fail the test.
  Core components take their colours from the app's Material 3 colour roles (`surfaceContainer`,
  `onSurfaceVariant`, `primary`…): set them in the app's `ColorScheme`, not per call. Unit tests that need Android
  classes use `jorgelillo.android.robolectric`, which also brings `:core:testing` (`MainDispatcherRule`,
  Turbine's `awaitMatching`) for ViewModel tests. Every app has a ViewModel test and a whole-flow test
  through the real `MainActivity` (`*FlowTest`); a new journey or ViewModel action gets one too.
- Snapshot tests pin anything users already see that is derived algorithmically (e.g. Christmas
  Countdown's yearly advent door order and `AdventPlan` contents); don't change those algorithms. Add
  advent activities or songs only outside December: later years are recomputed from the pool.

## Releasing (any app)

- **Always test the release build** (`apps/<app>/tools/smoke_test.sh`), not just debug: R8 strict
  full mode (AGP 9 default) once stripped WorkManager's `WorkDatabase_Impl` constructor and the app
  crashed on start. The `RoomDatabase` keep rule WorkManager/Glance need comes from `:core:reminders`'
  consumer rules: an app with reminders or widgets depends on it, never on WorkManager alone.
- Signing reads `apps/<app>/keystore.properties` (git-ignored, values trimmed). Never commit it or
  any `.jks`; check `git status` for filename variants (a leading-space copy once slipped in).
- Each release bumps `versionCode` in `apps/<app>/app/build.gradle.kts`, rewrites
  `apps/<app>/app/src/main/play/release-notes/{en-US,es-ES}/default.txt` and adds a row to
  `apps/<app>/release-notes.md` (the history).
- Store listing texts/images live in `apps/<app>/app/src/main/play/listings/<locale>/` (Gradle Play
  Publisher, applied by the convention plugin). Publish with the **Release** workflow
  (`gh workflow run release.yml -f app=<app> -f action=upload|promote -f track=internal|production`,
  optional `-f listing=true`, `-f dry_run=true`); production waits for the user's approval in GitHub.
  The same tasks run from the terminal (`publishBundle`, `promoteArtifact`, `publishListing`).
  Secrets (upload keys per app environment, Play service account, league-cards' private deck) go
  in with `scripts/set-release-secrets.sh`, run by the user. Details: docs/publishing-on-google-play.md §6.
  Publish the listing together with the release that adds what it describes. A new app goes into
  the workflow's `app` choices.
- **Baseline profiles**: each app (but group-polls) has `apps/<app>/baselineprofile` (`jorgelillo.android.baselineprofile`,
  on `:core:baselineprofile`: startup plus the app's main journeys by test tag). The generated
  `app/src/release/generated/baselineProfiles/` is committed and ships in the bundle; regenerate it
  with `scripts/generate-baseline-profiles.sh <app>` before a release whose code changed.
- `targetSdk` must meet Google Play's current requirement (API 36 since Aug 2026; the apps target 37 since 2026-11).
- Share links and App Links use `jorgelillo7.github.io` (`/q/`, `/t/`, `/p/` and
  `/.well-known/assetlinks.json` live in that repo): free static GitHub Pages, data in the link
  fragment, no server. The domain is tied to the GitHub username, which the user keeps for good,
  so no custom domain is planned; changing it would break every link already shared.
- Privacy policies are served from the separate repo `jorgelillo7/jorgelillo7.github.io`
  (`/privacy/<app>/`), never from this repo, so renaming this repo can't break them.
