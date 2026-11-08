---
name: upgrade-android-deps
description: Upgrade the Android toolchain and dependencies of this repo (Android Gradle Plugin, Gradle wrapper, Kotlin, compileSdk, AndroidX/Compose libraries) to the latest compatible versions, then verify every app's release build. Use when the user asks to update dependencies, libraries, Gradle, AGP, Kotlin or "everything" to the latest versions, or after Android Studio is updated.
---

# Upgrade Android toolchain and dependencies

All versions live in `gradle/libs.versions.toml` (plus `distributionUrl` in
`gradle/wrapper/gradle-wrapper.properties`). Work on a branch (`chore/deps-<yyyy-mm>`), one PR.

## 1. Baseline

Run the `check-android-env` skill first and fix what is red. Then bring the local tools up to date:

```bash
source scripts/env.sh
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --update   # SDK packages
scripts/update_android_studio.sh                               # needs Android Studio closed
```

Then:

```bash
source scripts/env.sh
git switch -c chore/deps-$(date +%Y-%m)
/usr/libexec/PlistBuddy -c 'Print CFBundleShortVersionString' "/Applications/Android Studio.app/Contents/Info.plist"
"$JAVA_HOME/bin/java" -version
./gradlew test   # must be green before changing anything
```

Android Studio goes first because new AGP majors may need it. If it is open, ask the user to close it.

## 2. Find latest versions

Query the repositories directly (more reliable than release-notes pages). Use `rtk proxy curl`
so output isn't truncated:

```bash
v(){ rtk proxy curl -s "$1" | grep -oE '<version>[0-9]+\.[0-9]+(\.[0-9]+)?</version>' | sed -E 's#</?version>##g' | tail -${2:-3}; }
G=https://dl.google.com/dl/android/maven2
v $G/com/android/tools/build/gradle/maven-metadata.xml                      # AGP
v https://repo1.maven.org/maven2/org/jetbrains/kotlin/kotlin-gradle-plugin/maven-metadata.xml
rtk proxy curl -s https://services.gradle.org/versions/current | grep '"version"'   # Gradle
v $G/androidx/compose/compose-bom/maven-metadata.xml
# …one line per [versions] entry of libs.versions.toml (group path = group with dots -> slashes)
rtk proxy curl -sL https://jb.gg/android-studio-releases-list.json | head -c 2000   # Studio releases
```

Stable versions only (skip `-alpha`, `-beta`, `-rc`, `-dev`). Show the user a table
(current → latest) before editing.

## 3. Check compatibility chains before bumping

AndroidX libraries declare `minCompileSdk` and `minAndroidGradlePluginVersion`. Read them from
the AAR instead of guessing:

```bash
aar(){ curl -s -o /tmp/x.aar "$G/$1/$2/$3/$2-$3.aar"; unzip -p /tmp/x.aar META-INF/com/android/build/gradle/aar-metadata.properties; }
aar androidx/compose/foundation foundation-android 1.12.1
```

- Compose BOM → foundation version: read `compose-bom-<v>.pom`.
- If the latest library needs a compileSdk/AGP you can't adopt, pin to the last compatible
  version and leave a comment above `[versions]` explaining the chain.
- AGP major versions have minimum Gradle versions (AGP 9 → Gradle 9.1+). Read the AGP release
  notes for breaking changes: https://developer.android.com/build/releases/gradle-plugin

## 4. Apply

1. Edit `gradle/libs.versions.toml`.
2. Install a new platform if `compileSdk` changes:
   `$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platforms;android-<N>.0" "build-tools;<N>.0.0"`
   (check disk space first: `df -h ~`).
3. Gradle wrapper: edit `distributionUrl` **by hand first** (the `wrapper` task fails if the old
   Gradle can't load the new AGP), then run `./gradlew wrapper --gradle-version <X>` to refresh
   `gradlew`, `gradlew.bat` and the jar.
4. Fix `build-logic/` for API changes (e.g. AGP 9: no `org.jetbrains.kotlin.android`,
   non-generic `CommonExtension`).
5. Bump each app's `versionCode`/`versionName` only if the user wants to ship this as a release.

## 5. Verify (all of it, every app)

```bash
./gradlew help --warning-mode all | grep -i deprecat   # fix ours; plugin-internal ones are fine
./gradlew test
for app in apps/*/; do a=$(basename $app); ./gradlew :apps:$a:app:lintDebug :apps:$a:app:bundleRelease; done
scripts/emulator.sh start
for app in apps/*/; do ${app}tools/smoke_test.sh; done   # RELEASE build on the emulator
scripts/emulator.sh stop
scripts/verify-bundle.sh apps/<app>/app/build/outputs/bundle/release/app-release.aab
```

The smoke test is not optional: R8 changes only show up in release builds. Known case: AGP 9's
strict full mode stripped `WorkDatabase_Impl()` (Room via WorkManager/Glance) and the app crashed
on launch. Read crashes with `$ADB logcat -d -b crash` and check R8 output in
`apps/<app>/app/build/outputs/mapping/release/mapping.txt`.

## 6. Finish

- Update the versions table and any new gotcha in `docs/setup/toolchain.md`; add a line to
  `docs/lessons-learned.md` if something broke.
- Commit (message lists old → new versions and any fix), push, open a PR with the version table
  and the test plan (tests, lint, smoke test per app).
