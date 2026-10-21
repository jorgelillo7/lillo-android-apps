---
name: new-android-app
description: Create a new Android app in this repo from idea to first Play-ready build: competitor study on Google Play, differential value, design direction (palette, name), module scaffolding with the convention plugins, icon, privacy policy page, store listing skeleton and repo bookkeeping. Use when the user wants to start, create or build a new app.
---

# New Android app

Process used for the apps after Christmas Countdown. Keep the user in the loop at the three decision
points marked **🛑 ask**; everything else you can do on your own. Write in English; talk to the
user in their language.

## 1. Understand the idea

Restate the idea in two lines: what the user decides/does with it, for whom, offline or not.
Default constraints in this repo: **local only, no backend, no ads, no tracking**, English +
Spanish, simple scope.

## 2. Competitor study (Google Play)

```bash
for q in "<keyword 1>" "<keyword 2>" "<spanish keyword>"; do
  curl -s -A 'Mozilla/5.0' "https://play.google.com/store/search?q=${q// /%20}&c=apps&hl=en" \
    | rtk proxy grep -oE 'details\?id=[A-Za-z0-9_.]+'
done | sort | uniq -c | sort -rn | head -25
```

For the top ~12 ids, fetch `https://play.google.com/store/apps/details?id=<id>&hl=en` and extract
title, downloads (`>[0-9.,]+[KMB]?\+<`), rating (`Rated ([0-9.]+) stars`), "Contains ads",
"In-app purchases" and the description (`data-g-id="description"`). One WebSearch for review
complaints ("<category> app reviews too many ads / I wish") gives the pain points.

Summarise for the user: a table (app, downloads, rating, ads, standout feature), what users
complain about, and **honestly** whether there is room for something different. "Nothing new,
but done well and without ads" is a valid outcome.

## 3. 🛑 ask: scope, design, name

Propose 3–5 differential ideas ranked by value/effort and let the user pick (AskUserQuestion,
multi-select). Keep v1 small.

Design: render 2 palette proposals of the app's key screen with Pillow and `open` the PNG on the
user's Mac (they can't see your Read images). Rules that worked: one background (light or dark),
5–6 accent colours with similar saturation (cohesive, not a rainbow), never two equal colours
adjacent, dark text on light accents.

Name: check it on Play (`search?q=<name>`); avoid generic names that already have dozens of
apps. Pick a package id that never changes: `com.jorgelillo.<name>`.

## 4. Scaffold

Follow [docs/adding-a-new-app.md](../../../docs/adding-a-new-app.md). Checklist:

- `apps/<app>/domain` (pure Kotlin: all rules, unit tested) and `apps/<app>/app` (Compose UI).
- Register both in `settings.gradle.kts`.
- Before adding a library, read its AAR `minCompileSdk`/`minAndroidGradlePluginVersion`
  (see the upgrade-android-deps skill) and add it to `gradle/libs.versions.toml`.
- Persistence: DataStore (+ kotlinx.serialization JSON for small object graphs). Room only when
  queries/relations justify it.
- Signing and R8 come from the `jorgelillo.android.application` plugin: the app only needs a
  `proguard-rules.pro` (RoomDatabase keep rule if WorkManager/Glance/Room is pulled in).
- **Start from what is already proven, never from a copy** (CLAUDE.md, "Reuse first"). List
  `core/designsystem`, `core/platform`, `core/sharelink` and `scripts/` first and build on them:
  `installLilloWindow` + `setLilloContent` in `MainActivity`, `LilloTheme`, `LilloPage` /
  `LilloBigButton`, `LilloAboutSheet` (language, rate, privacy), `LilloLanguageDialog`,
  `isShortScreen` / `sideActionsWhenShort`, `ConfettiBurst`, `LilloQrCode`, `JsonStore`,
  `LilloLinks`, share links. The language list (`localeConfig`) comes from the convention plugin:
  add `res/resources.properties` (`unqualifiedResLocale=en-US`).
- Need something another app has but core doesn't (an avatar, a settings row, a dialog)? Move it to
  core in this change and point the other app at it too, instead of copying it.
- Strings in `values/` and `values-es/` from the start.
- Tests from the first screen (CLAUDE.md, "Working on a change"):
  - screenshot tests: apply `jorgelillo.android.screenshots` and capture the key screens upright and
    in landscape (like `apps/whos-lying/app/src/test/.../RoundScreensScreenshotTest.kt`); the
    accessibility checks run on every capture;
  - a ViewModel test on a real `JsonStore` with Turbine and `:core:testing` (`MainDispatcherRule`
    with `cancelAtEnd`, `awaitMatching`), like `apps/decision-wheel/.../WheelsViewModelTest.kt`;
  - a whole-flow test through the real `MainActivity` (`*FlowTest`, taps by test tag, one per
    class), like `apps/tournaments/.../TournamentFlowTest.kt`. If the `Application` starts
    WorkManager, a test subclass starts its test instance first (Christmas's
    `TestChristmasCountdownApplication`).
- A new core component gets a `@Preview` in `core/designsystem/.../Catalogue.kt`: that preview is
  its screenshot test.
- `./gradlew spotlessApply :apps:<app>:domain:test :apps:<app>:app:testDebugUnitTest :apps:<app>:app:lintDebug :apps:<app>:app:assembleDebug`

## 5. Assets

- Icon: copy the most similar app's `tools/generate_icon.py` and change geometry/palette; it
  emits adaptive + monochrome + legacy PNGs + `store/play-icon-512.png`. House style: flat shapes
  on a dark gradient, checked at 48/72 px next to the other apps; study the competitors' icons
  first.
- `tools/smoke_test.sh` on `scripts/smoke_lib.sh` (`smoke_start` … `smoke_finish`), tapping test
  tags with `tap` (never coordinates) and closing the keyboard with `hide_keyboard` (never a bare
  `back`: CI's emulator has no on-screen keyboard). CI's Smoke workflow finds it by itself
  (`apps/*/tools/smoke_test.sh`) and runs it on every PR that touches the app; `tools/capture_store_screenshots.sh` on `scripts/capture_lib.sh`
  (`capture_start` … `capture_open` per locale … `shot`); longer UI runs (a battery of scenarios) build on
  `scripts/ui_lib.py` like `apps/whos-lying/tools/battery_test.py`; and
  `tools/generate_store_assets.py` (only `STYLE` + `LOCALES`, the drawing is `scripts/store_assets.py`).
- `app/src/main/play/listings/{en-US,es-ES}/` with `title.txt` (≤30), `short-description.txt` (≤80),
  `full-description.txt`, and `app/src/main/play/release-notes/{en-US,es-ES}/default.txt`
  (Gradle Play Publisher layout; `generateReleasePlayResources` validates it). The first bundle
  goes to Play Console by hand; later ones with `publishBundle`.

## 6. Repo bookkeeping

- Privacy policy: `privacy/<app>/index.html` in `~/Projects/jorgelillo7.github.io` (copy the
  closest one: no-data apps → `tournaments`, backend → `group-polls`) and an app card in the pinned "Apps" block of `index.html` and
  `en/index.html`. Publish only after the user previews it.
- `apps/<app>/README.md`, `OPERATIONS.md` (uppercase), `release-notes.md`; add the app to the
  root README table, `docs/README.md` per-app table and `PENDING.md` (its own section).
- Add the app to the `app` choices of `.github/workflows/release.yml` (CI and Smoke need nothing).
- Branch + PR; merged once CI is green.

## 7. First release

Hand over to the `release-android-app` skill. For a brand-new app the Play Console also needs, by
hand and once (the Play API can't do them): create the app, register the package name in Android
developer verification, the App content declarations (data safety, content rating, target
audience, ads, privacy policy…) and the **first bundle**. A personal developer account created
after November 2023 also needs a closed test (12 testers, 14 days) before production.

The upload key: the user runs `scripts/new-upload-key.sh <app>` in their own terminal (never ask
for the password in chat), backs it up off the Mac ([docs/signing.md](../../../docs/signing.md)),
then `scripts/set-release-secrets.sh <app>` so the Release workflow can sign. From the second
release on, everything (bundles, notes, listing, promotion) goes from the Release workflow.
