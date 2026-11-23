---
name: slim-engine
description: Make a game in engine/ smaller on the phone by building Godot without the modules it does not use (physics, navigation, XR, networking, video, extra formats): write or edit the game's engine profile, check the game against it, build the slim Android template, export with it and verify it on the emulator and the owner's phone. Use when the owner wants a lighter APK, when a game's profile must bring a module back, or when Godot is upgraded.
---

# Slim the engine of a game

Results so far: Summandos' arm64 APK went from 27 MB to 19 MB (engine 24 → 15 MB compressed),
built in about 5 minutes on an M1. Background: `docs/plans/fake-ad-games-tech.md` (S3b),
`engine/README.md`.

## How it works

- `engine/<game>/engine-profile.py` is a SCons profile: the switches that leave modules out of
  that game's engine. **One assignment per line**: SCons reads the file as variables, so a loop
  or `globals()` is silently ignored (that built a broken engine once).
- `scripts/engine_profile_check.py engine/<game>` greps the game and `lillo_core` for anything a
  switch removes (physics bodies, `RegEx`, `.ogg`, `GLTF*`, `@rpc`…). A removed module only fails
  at run time on the phone, so this runs before every build and every export.
- `scripts/engine-template.sh engine/<game>` compiles Godot from its pinned source tarball
  (SHA-256 checked) with that profile, arm64 only, and drops the library into the **official
  template APK of the same version** (Godot's own Gradle template build wants JDK 17; this Mac has
  25/26, and the Java side is identical anyway). Output: `engine/<game>/export/template-release.apk`,
  rebuilt only when the profile or Godot's version changes. Source, SCons and logs live in
  `~/Library/Caches/lillo-engine/`.
- `scripts/engine-export.sh engine/<game>` then uses that template by itself (arm64 only).

## Writing a profile for a new game

1. Start from Summandos' profile and list what the game really uses (grep its scripts and scenes;
   a 2D physics puzzle needs `disable_physics_2d = "no"` and `module_godot_physics_2d_enabled`).
2. Keep these unless proven unused: `text_server_adv` (the fallback text server broke Luckiest
   Guy's kerning: "CAPTAIN" drew as "AIN CAPT"), `meshoptimizer` (`LilloPoseBaker` simplifies
   crowds at run time), `glslang` (Vulkan compiles shaders on the phone), `svg`, `webp`,
   `freetype`.
3. `python3 scripts/engine_profile_check.py engine/<game>` must pass.
4. `scripts/engine-template.sh engine/<game>`, then `scripts/engine-export.sh engine/<game> out.apk`.

## Verifying

- Emulator (arm64 on Apple silicon): `scripts/emulator.sh start --headless`, `adb install -r`,
  `adb shell am start -n <package>/com.godot.game.GodotAppLauncher`, wait, `adb exec-out screencap`,
  and `adb logcat -d | grep " godot "` for errors. A cold emulator can fail once with "Couldn't
  create Android Surface": launch again before blaming the engine.
- Compare against the stock engine on the same screen when something looks off: install the stock
  APK, then the slim one, same screenshot.
- Look at text in every language, 3D characters, team colours, crowds, sound; then the owner's
  phone for frame rate.

## Bringing a module back

Set its switch to "yes" in the game's profile, rebuild the template (5 minutes), export. Other
games keep their own profiles. A game without a profile exports with the stock engine.

## Upgrading Godot

Change `VERSION` in `scripts/godot.sh` as usual, then `SOURCE_SHA256` (the release's
`godot-<version>-stable.tar.xz.sha256`) and `NDK` (`get_ndk_version()` in the release's
`platform/android/detect.py`) at the top of `scripts/engine-template.sh`; install the new official
export templates; rebuild every game's template and check each on the emulator.

## Pitfalls already met

- `sdkmanager` is deprecated and `yes | sdkmanager …` exits 141 (SIGPIPE) under `pipefail`:
  install with `$ANDROID_HOME/cmdline-tools/latest/bin/android sdk install ndk/<version>`.
- rtk's `ls` hides output: a folder can look empty when it is not; use `rtk proxy ls`.
- Do not hand-edit `export_presets.cfg` around `engine-export.sh`: the script backs it up and
  restores it itself, and a second backup on top loses the original.
