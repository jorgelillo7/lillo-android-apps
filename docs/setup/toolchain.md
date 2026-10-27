# Toolchain

## Versions (October 2026)

| Tool | Version | Where it is set |
|---|---|---|
| Android Studio | 2026.2 "Rabbit" (bundled JBR = JDK 25) | installed app |
| Gradle | 9.8.2 | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 9.4.1 | `gradle/libs.versions.toml` |
| Kotlin | 2.4.21 | `gradle/libs.versions.toml` |
| compileSdk / targetSdk / minSdk | 37 / 36 / 24 | `gradle/libs.versions.toml` |
| Compose BOM | 2026.09.00 | `gradle/libs.versions.toml` |
| Baseline profile plugin / Macrobenchmark | 1.5.0 | `gradle/libs.versions.toml` |
| Java bytecode target | 17 | `build-logic/.../KotlinAndroid.kt` |

All dependency versions live in **one file**, `gradle/libs.versions.toml`. Shared Android/Kotlin
settings live in the convention plugins under `build-logic/`.

## Terminal setup

```bash
source scripts/env.sh   # JAVA_HOME = Android Studio's JDK, ADB, ANDROID_HOME
```

The shell's default `java` (Homebrew) is newer than Gradle supports; always use Android Studio's JDK.

## Upgrading

1. Check latest versions:
   - AGP: https://developer.android.com/build/releases/gradle-plugin
   - Gradle: https://gradle.org/releases/
   - Kotlin: https://kotlinlang.org/docs/releases.html
   - AndroidX: https://developer.android.com/jetpack/androidx/versions
2. Bump versions in `libs.versions.toml`. For Gradle, edit `distributionUrl` in
   `gradle/wrapper/gradle-wrapper.properties`, then run `./gradlew wrapper --gradle-version X` to
   refresh the scripts.
3. If `checkDebugAarMetadata` fails with *"requires compileSdk N / AGP X"*, a library needs a newer
   platform: bump `compileSdk` (and install it: `sdkmanager "platforms;android-N.0"`) or AGP.
4. Run `./gradlew test lint bundleRelease`, then the app's `tools/smoke_test.sh`
   (**release build**, not debug: R8 rules only bite there).

## Gotchas we hit

- **AGP 9 has Kotlin built in.** Don't apply `org.jetbrains.kotlin.android`. `CommonExtension` is no
  longer generic. The Compose compiler plugin is still needed.
- **R8 strict full mode (AGP 9 default)** stops keeping implicit constructors for `-keep class X`
  rules. Room's rule lost `WorkDatabase_Impl()` and the release crashed on start. Fix in the app's
  `proguard-rules.pro`:
  ```
  -keep class * extends androidx.room.RoomDatabase { <init>(); }
  ```
- **AndroidX version chains:** Compose 1.12+, core 1.19+ and lifecycle 2.11+ require compileSdk 37
  and AGP 9.1+. You can't bump one without the others.
- **JDK 25 `keytool` crashes in Spanish locale** (`MissingFormatArgumentException`). Run it with
  `-J-Duser.language=en` (`scripts/verify-bundle.sh` does).
- **Old Gradle (8.x) can't run on JDK 25.** To rebuild an old tag, download a JDK 21 (Adoptium)
  and point `JAVA_HOME` to it just for that build.
- **The Mac has 8 GB RAM.** Emulator + Android Studio + Gradle don't fit together: close Chrome,
  and stop the emulator (`scripts/emulator.sh stop`) during long builds.
