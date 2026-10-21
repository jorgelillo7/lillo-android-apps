# Adding a new app

Example: a roulette app, `com.jorgelillo.roulette`.

## 1. Modules

```
apps/roulette/
├── app/          Android app (Compose UI, resources)
├── domain/       pure Kotlin logic (no android.*), unit tested
├── store/        raw screenshots and the 512 px icon the listing graphics are made from
│               (the listing itself: app/src/main/play/, Gradle Play Publisher layout)
├── tools/        app-specific scripts (icon, screenshots, smoke test)
├── OPERATIONS.md    commands to run, test and release
├── release-notes.md version history
└── README.md
```

`apps/roulette/domain/build.gradle.kts`
```kotlin
plugins { alias(libs.plugins.jorgelillo.jvm.library) }
dependencies { testImplementation(libs.kotlin.test) }
```

`apps/roulette/app/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.jorgelillo.android.application)
    alias(libs.plugins.jorgelillo.android.compose)
}
android {
    namespace = "com.jorgelillo.roulette"
    defaultConfig {
        applicationId = "com.jorgelillo.roulette"
        versionCode = 1
        versionName = "1.0"
    }
}
dependencies {
    implementation(projects.apps.roulette.domain)
    implementation(projects.core.designsystem)
}
```

Release signing and R8 come from the `jorgelillo.android.application` plugin: it reads
`apps/<app>/keystore.properties` (created by `scripts/new-upload-key.sh`) and falls back to the
debug key without it, so the release build can be smoke tested before the app has its key. Each
app still needs its own `proguard-rules.pro`.

Register both in `settings.gradle.kts`:
```kotlin
include(":apps:roulette:app")
include(":apps:roulette:domain")
```

SDK levels, Java target, desugaring and Compose setup come from `build-logic`; nothing else to configure.

Notes from app #2:
- Apps get `minSdk` 24 (`appMinSdk` in the catalogue): Play's automatic protection rejects bundles
  below it, and navigation-compose needs it. Only an app already live on a lower one keeps it in its
  own `defaultConfig` (Christmas, 23); never raise it for an app with users without checking them.
- Wrap the content in `Modifier.semantics { testTagsAsResourceId = true }` and give buttons a
  `testTag`: some Compose components (e.g. extended FABs) don't expose their text to UI Automator.

## 2. Theme and shared components

Wrap the UI in `LilloTheme(colorScheme = RouletteColorScheme) { … }` from `:core:designsystem`, and
put `TestTagsAsResourceIds` on the root and on every dialog or sheet. Also in core: `LilloPage`
(header with back button, content, bottom actions), `LilloBigButton` and `isShortScreen()` for
landscape phones and handhelds. Apps wrap them with their own colours (see `whos-lying` and
`group-polls` `ui/screens/Common.kt`). If a component is useful to a second app, move it into
`core/designsystem` then, not before.

## 3. Icon and store assets

- Copy `apps/christmas-countdown/tools/generate_icon.py`, change the geometry and palette.
- `tools/generate_store_assets.py`: copy one, keep only the app's `Style` (colours) and
  `LOCALES` (headlines); the drawing is `scripts/store_assets.py`.
- `tools/capture_store_screenshots.sh`: copy one and change the steps.
- `tools/smoke_test.sh`: `source scripts/smoke_lib.sh`, `smoke_start <app> <package>`, the app's
  steps with `tap`/`shot`, then `smoke_finish`.
- `scripts/setup-python.sh` creates the Python environment once.

## 4. Privacy policy

Add `privacy/roulette/index.html` to `jorgelillo7/jorgelillo7.github.io` and list the app on its
home page. Link it from the app's About screen.

## 5. Publish

Follow [publishing-on-google-play.md](publishing-on-google-play.md), including registering the
package name in Android developer verification.
