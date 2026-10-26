# Decisions

The choices that shape this repo, one line each: what we decided and why. The reasoning in full
lives in the PR. Add a line when a decision is taken; change it when it is reversed, don't delete it.

## Code and architecture

| Decision | Why | Since |
|---|---|---|
| Business rules in pure Kotlin `domain` modules (no `android.*`) | Fast JVM tests; portable to the lillorepo Bazel monorepo | 2026-10 |
| **Reuse first: the second copy goes to `core`** in the same change | "Move it once a second app needs it" never triggered: two clean-ups found ~10 duplicated pieces | #49 |
| App-module composables are `internal`; `core` is the public API | Tighter visibility; the Compose rules' "every public composable takes a modifier" then applies to the reusable library | #62 |
| Immutable UI state in `StateFlow`, collected with `collectAsStateWithLifecycle` | A mutable domain object Compose couldn't observe left the impostor game stuck | #48 |
| Type-safe navigation (`@Serializable` routes), never route strings | Compile-checked arguments; no magic ids like `"new"` / `"copy-of-…"` | #61 |
| Colours come from the app's Material 3 `ColorScheme` roles | Set once per app, used by core components and by Material dialogs/fields alike | #59 |
| Text contrast meets WCAG AA (4.5:1), enforced by the tests | Live apps had white on light gradients (down to 1.8:1) and an invisible hint | #60 |
| Short screens (< 480 dp tall) get compact layouts; `LilloPage(sideActionsWhenShort)` puts actions beside the content | Landscape phones and handhelds (AYN Thor) lost results, Spin and the card below the fold | #47, #55 |
| No DI framework: app singletons on the `Application`, `viewModelFactory` | Six small apps; Hilt would cost more than it saves | 2026-10 |
| No crash reporting SDK | Privacy: Play Console vitals cover crashes and ANRs | 2026-10 |

## Testing

| Decision | Why | Since |
|---|---|---|
| Screenshot tests on the JVM (Roborazzi + Robolectric), upright and landscape, images committed | Catch layout breaks in seconds; they found four real landscape bugs in live apps | #54, #55 |
| Every screenshot test also runs Google's Accessibility Test Framework | Labels, touch targets and contrast checked on every screen, every PR | #58 |
| Disabled controls and emoji are exempt from the contrast check | WCAG exempts inactive controls; ATF measures emoji as if they were letters | #60 |
| A frozen clock and fixed dates in screenshot tests | Confetti, countdowns and "2 h ago" must look the same every run | #54 |
| Core's `@Preview` catalogue is its screenshot test (ComposablePreviewScanner) | One place to show and pin each component; it found unreadable colours in the previews themselves | #73 |
| ViewModel tests with Turbine; one whole-flow test per app through the real `MainActivity` on the JVM | The smoke test's journey in seconds on every PR; the first one found routes kotlinx.serialization couldn't reach | #67, #69 |
| Release smoke tests on an emulator, locally and in CI (one job per app, ATD image, required check) | R8 once stripped a class and crashed an app on start; only running the app catches that. ATD: the Google image crashed in CI | #63, #68 |
| One UI run on the emulator at a time | Two runs tap over each other and void the results | #56 |
| UI scripts tap by text or test tag (`scripts/ui_lib.py`), never coordinates | They survive layout, language and orientation changes | #48 |

## Tooling

| Decision | Why | Since |
|---|---|---|
| Community-standard tools, adopted natively (refactors welcome) | Custom glue is what rots; the repo should be a good example | 2026-10 |
| Convention plugins in `build-logic` + one version catalog | Each module only declares what is its own | 2026-10 |
| Gradle build cache, saved from master's CI runs; screenshot images are test inputs | Untouched modules come from the cache instead of rebuilding every PR; a changed image alone still re-runs its tests | #74 |
| Smoke runs only the apps a PR touches (all when shared code changes) | Six emulators for a one-app change was most of the CI time | #74 |
| Releases from a manual Release workflow (Gradle Play Publisher): upload to internal or production, promote, listing; production needs an approval | No Play Console visits after an app's first release, and nothing goes live without a click; keys stay in GitHub environments | #75 |
| Spotless + ktlint (Android Studio style) + Compose rules, 120 columns | One formatter, checked in CI; 120 keeps Compose call chains readable | #53, #62 |
| Renovate for dependency PRs, grouped (Kotlin, AGP, AndroidX), WorkManager held below 2.12 | Weekly, reviewable updates; WorkManager 2.12 needs minSdk 24 | #53 |
| Gradle Play Publisher, listing in its own `app/src/main/play` layout | Publish from the terminal; a Gradle plugin, no Ruby | #51 |
| Installers only from official channels (Gradle plugin portal, Maven Central, PyPI) | Wrapper installers (`npx`, `curl \| bash`) are a supply-chain risk | 2026-10 |
| Backups copy only `files/datastore/` | Users' data travels to a new phone; WorkManager's database never does | #56 |
| MIT licence for the code (names, icons, store graphics: all rights reserved) | Same as lillorepo, where this code may move; free to reuse, but no clones with the apps' own branding | #66 |
| Share links and App Links on `jorgelillo7.github.io`, data in the fragment | Free static hosting, no server, nothing stored | 2026-10 |
| Every app on minSdk 24 (Christmas Countdown left 23) | WorkManager 2.12, navigation-compose and Play's automatic protection need it; phones on API 23 keep the version they have | 2026-10 |
| `LilloPage` pads for system bars and the display cutout; predictive back on from `:core:designsystem`'s manifest | One place for every app: landscape notches never cover content, the back gesture previews the screen behind on Android 13+ | 2026-10 |
