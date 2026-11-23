# engine/

Games built on a game engine (Godot today) instead of Kotlin and Compose. Games written in
Compose, such as `whos-lying`, `league-cards` or `slipper`, stay in `apps/`: this folder is about
the tool, not the genre. Plans: [docs/plans/fake-ad-games.md](../docs/plans/fake-ad-games.md) and
[the technology study](../docs/plans/fake-ad-games-tech.md).

```
engine/
  core/                       Godot project holding the shared addon and its tests
    addons/lillo_core/        what every game reuses: LilloGate (maths gates), LilloFormation
                              (where each member of a crowd stands, scattered into a mob),
                              LilloCrowdView (draws hundreds of copies of a mesh in one call),
                              LilloFigure (cartoon characters, monsters and bullet streaks as
                              one vertex-coloured mesh, toon material with outline),
                              LilloSettings (player preferences kept between sessions),
                              LilloMathPuzzle (a number written as a sum to work out),
                              LilloBackdrop (rays, clouds and hills behind menus), LilloSticker
                              (a waving soldier or swaying monster for corners), LilloFx
                              (pop-ins, squishy buttons, rising numbers, confetti)
    tests/                    GUT tests, res://tests/test_*.gd
  summandos/                  game one, Summandos (com.jorgelillo.summandos), README there
    addons/lillo_core         symlink to ../../core/addons/lillo_core
    tests/
```

## Commands

```bash
scripts/godot.sh --version                 # pinned Godot, downloaded on first use (checksum verified)
scripts/godot.sh --path engine/summandos   # open a project in the editor
scripts/engine-test.sh engine/core         # a project's tests, headless (GUT fetched on first use)
scripts/engine-export.sh engine/summandos  # Android APK from the project's "Android" preset
```

CI's **Engine** workflow runs every project's tests when `engine/` or these scripts change.

Exporting needs, once per machine, Godot's Android export templates for the pinned version in
Godot's data folder (`~/Library/Application Support/Godot/export_templates/<version>.stable/`:
`android_release.apk`, `android_debug.apk`, `version.txt`, from the release's `.tpz`), and the
Android SDK; `scripts/engine-export.sh` points Godot at the JDK from `scripts/env.sh` and signs
with Godot's debug key unless `GODOT_ANDROID_KEYSTORE_RELEASE_*` are set.

## Rules

- **Reuse first**: anything a second game would need lives in `core/addons/lillo_core` from the
  start, with its tests there; a game links it, never copies it.
- **Upright and turned sideways**: projects use the sensor orientation and `canvas_items` /
  `expand` stretching, so a rotated phone gets a real layout, never a stretched one.
- Godot's `.godot/` cache, exports and GUT are git-ignored; nothing downloaded is committed.
- Upgrading Godot or GUT: change the version and checksum at the top of `scripts/godot.sh` or
  `scripts/engine-test.sh`.
