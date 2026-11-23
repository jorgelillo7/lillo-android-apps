---
name: engine-game
description: Build, test, look at and ship a game in engine/ (Godot): start a new game from the shared core, run its tests, take screenshots upright and sideways, check its icon, export an APK for the owner's phone, and tune its difficulty. Use when working on Summandos or any other game under engine/, or when starting a new one.
---

# Work on an engine game

Background: `engine/README.md` (layout, core classes, rules). Game specifics:
`engine/<game>/OPERATIONS.md` and `DESIGN.md`. Plans: `docs/plans/fake-ad-games*.md`.

## Before writing code

- **Reuse the core first.** Read the class list in `engine/README.md`. Anything a second game
  would want (a crowd, a gate, a menu piece, an effect) goes in
  `engine/core/addons/lillo_core/<area>/` with a test in `engine/core/tests/`, taking colours and
  callbacks as arguments. Never copy core code into a game.
- **Rules in `game/`, drawing in `view/`.** `game/` is plain `RefCounted` scripts, unit tested;
  `view/` builds nodes; `main.gd` only wires them.
- Colours only in `game/palette.gd`, words only in `game/texts.gd` (English and Spanish).

## Loop

```bash
scripts/engine-assets.sh engine/<game>                       # third-party models, once
scripts/godot.sh --headless --path engine/<game> --import   # after adding a class_name script
scripts/engine-test.sh engine/core
scripts/engine-test.sh engine/<game>
scripts/engine-shot.sh engine/<game> /tmp/a.png 2            # upright 540x1170
scripts/engine-shot.sh --landscape engine/<game> /tmp/b.png 2 # sideways
```

- **Look at every screen you touched, both ways round**, before saying it works: Read the PNGs.
  Put several on one board with Pillow when comparing.
- Screens that need progress (stars, unlocked levels): `--progress
  engine/<game>/tools/samples/<file>.cfg`. The script restores the player's own settings after.
- A run at a given moment: `-- --mode=play --autosteer` and pick the seconds; options per game in
  its `OPERATIONS.md`.
- **Screen locked** (owner away): Godot draws grey or nothing on the Mac. Use the emulator
  (`scripts/emulator.sh start --headless`, install the APK, `adb exec-out screencap`).
- Difficulty: `scripts/godot.sh --headless --path engine/<game> --script tools/balance.gd` where
  the game has one; early levels should be won by random choices about half the time or more.

## Third-party models

- Only CC0 (or similarly free) packs from the author's official source; pin the file by commit
  URL and SHA-256 in `assets/models/models.txt`, with the animations the game uses in the fourth
  column, then `scripts/engine-assets.sh engine/<game>`.
- After the first import, set `import_script/path="res://addons/lillo_core/dev/keep_animations.gd"`
  in the model's `.glb.import` and commit that file: it drops unused animations (a KayKit model
  shrinks from ~4 MB of animations to one).
- Crowds: `LilloPoseBaker.bake(...)` into a `LilloAnimatedCrowd`; one hero model at most per role
  plays the real animation. Wash textures with the palette so the teams still read.
- Credit the pack on the game's About card (`game/texts.gd`, `credits`).

## Sound

Effects come from `engine/<game>/tools/generate_sounds.py` on the shared synthesiser
`engine/core/tools/lillo_sfx.py`; play them through `LilloAudio` (switches, throttling, button
clicks). Third-party music is CC0 only, credited on the About card like models.

## Icons

```bash
.venv/bin/python engine/<game>/tools/generate_icon.py
.venv/bin/python scripts/engine_icon_preview.py engine/<game> /tmp/icon.png
```

Read the preview: 48 px must still say what the game is; nothing important outside the round
mask; the monochrome layer must read (cut details out rather than lose them). Send it to the owner
before committing a new icon.

## Store listing graphics

The apps' pipeline, fed by the game: `tools/capture_store_screenshots.sh` takes 1080x2424 raws
with `scripts/engine-shot.sh --store` (drawn off-screen, so taller than the Mac's screen; the main
scene calls `LilloCapture.rehost(self)` first), in every locale (`--lang=`), then
`tools/generate_store_assets.py` frames them with headlines on `scripts/store_assets.py` into
`play/src/main/play/listings/`. Pick moments by reading the level data (gate z, think rows):
the run moves 9 m/s. Keep the frame rate and test-only labels off the pictures.

## A build for the owner's phone

```bash
scripts/engine-export.sh --arm64 engine/<game> /tmp/<game>.apk
```

About 25 MB with arm64 only (the full APK with x86_64 is twice that and over the 30 MB file
limit); send it with SendUserFile. It is signed with Godot's debug key: the owner may need to
uninstall a build signed otherwise.

## Starting a new game

1. Plan in `docs/plans/`, competitors studied as for an app (the `new-android-app` skill's study
   step), package `com.jorgelillo.<englishname>`.
2. Copy the layout in `engine/README.md`: `project.godot` (sensor orientation, 720x720 base,
   `canvas_items` + `expand`, mobile renderer, ETC2/ASTC on), `export_presets.cfg` from Summandos
   with the package changed, the `addons/lillo_core` symlink, `tests/test_core_link.gd`.
3. README, DESIGN, OPERATIONS from the start; a row in the root README's engine table.
4. CI picks the project up through `scripts/engine-test.sh`; check the Engine workflow runs it.

## Pitfalls already met

- `Preload file ... has no resource loaders`: a new font or image was never imported; run the
  import line above.
- `Identifier "LilloX" not declared`: the class cache is stale; same fix.
- Containers set children's position and size, not scale: `LilloFx.pop_in` and `squishy` are
  safe on grid tiles and buttons.
- A model still shows a T-pose or lacks an animation after editing `models.txt`: Godot did not
  reimport it. Delete `engine/<game>/.godot/imported/<Model>.glb-*` and import again.
- A Label3D left in world space falls behind a moving squad: parent rising text to something
  that moves with it (`LilloFx.rising_text(count_label, …)`).
- The camera looks down +z, so screen-right is world −x: go through `world_x()`.
