#!/usr/bin/env bash
# Shared environment for the scripts and for running ./gradlew from a terminal.
# Usage: source scripts/env.sh
# On the Mac, Android Studio's JDK (the shell's Homebrew JDK is too new for Gradle); elsewhere (CI)
# keep the JAVA_HOME the environment already set.
STUDIO_JBR="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
if [ -d "$STUDIO_JBR" ]; then export JAVA_HOME="$STUDIO_JBR"; fi
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export ADB="$ANDROID_HOME/platform-tools/adb"
export EMULATOR="$ANDROID_HOME/emulator/emulator"
