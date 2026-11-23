# engine/

Games built on a game engine (Godot today) instead of Kotlin and Compose. Games written in
Compose, such as `whos-lying`, `league-cards` or `slipper`, stay in `apps/`: this folder is about
the tool, not the genre. Plans: [docs/plans/fake-ad-games.md](../docs/plans/fake-ad-games.md) and
[the technology study](../docs/plans/fake-ad-games-tech.md). Working on a game: the
[`engine-game`](../.claude/skills/engine-game/SKILL.md) skill.

```
engine/
  core/                       Godot project holding the shared addon and its tests
    addons/lillo_core/
      crowd/                  LilloFormation (where each member of a crowd stands, or a mob),
                              LilloCrowdView (hundreds of copies of a mesh in one draw call),
                              LilloFigure (cartoon characters, monsters, streaks; toon material),
                              LilloPoseBaker (an animated model as a few still, simplified
                              poses) and LilloAnimatedCrowd (a crowd stepping through them)
      maths/                  LilloGate (a maths gate), LilloMathPuzzle (a number as a sum to
                              work out, three tiers)
      render/                 LilloMaterials (flat, banded ground, camera-facing badge, a
                              textured model repainted in a team colour) and their shaders
      ui/                     LilloTheme (font, outlined text, buttons with a lip, cards),
                              LilloLevelMap (paged level map with stars and padlocks),
                              LilloStars, LilloBackdrop (rays, clouds, hills), LilloSticker
                              (a waving soldier or a swaying monster for corners), LilloFx
                              (pop-ins, squishy buttons, rising numbers, confetti), LilloSafeArea,
                              LilloAbout (name, version, credit blocks with links), LilloSwatchPicker,
                              LilloModelStage (an animated 3D character standing in a 2D screen),
                              LilloColours (ΔE under simulated colour blindness, for palette tests)
      save/                   LilloSettings (player preferences and progress, kept on the phone)
      dev/                    LilloCapture (--shot screenshots), keep_animations.gd (import
                              script that drops a model's unused animations)
    tests/                    GUT tests, res://tests/test_*.gd
  <game>/                     one Godot project per game, laid out as below
```

## A game's layout

The same shape as an app in `apps/` wherever the two overlap (README, DESIGN, OPERATIONS,
`store/`, `tools/`), plus what a Godot project needs:

```
engine/<game>/
  README.md                   what the game is, its status and structure
  DESIGN.md                   palette, type, characters, screens
  OPERATIONS.md               commands to run, test, screenshot, export and release
  project.godot, export_presets.cfg, main.tscn
  main.gd                     entry point: wires game/ and view/, input, back button, options
  game/                       rules without drawing (levels, a run, progress, palette, texts),
                              unit tested
  view/                       drawing: the 3D world and the screens; no rules
  assets/                     what the game loads: icons/ (launcher), fonts/, models/ (third-party
                              models listed in models.txt, fetched, not committed; their .import
                              files are), later sounds/
  store/                      Play listing graphics: play-icon-512.png, raw/<locale>/ screenshots
  tools/                      game-specific scripts (icon generator, balance report, samples/)
  tests/                      GUT tests of game/ (test_core_link.gd checks the addon link)
  addons/lillo_core           symlink to ../../core/addons/lillo_core
```

## Commands

```bash
scripts/godot.sh --version                                   # pinned Godot, downloaded on first use
scripts/godot.sh --path engine/<game>                        # open a project in the editor
scripts/engine-test.sh engine/core                           # a project's tests, headless
scripts/engine-assets.sh engine/<game>                       # fetch its models (pinned, checksummed)
scripts/engine-shot.sh engine/<game> out.png 3 -- --levels   # screenshot (--landscape, --progress f.cfg)
.venv/bin/python scripts/engine_icon_preview.py engine/<game> out.png   # icon at every size
scripts/engine-export.sh [--arm64] engine/<game>             # Android APK, debug-signed by default
```

CI's **Engine** workflow runs every project's tests when `engine/` or these scripts change.

Exporting needs, once per machine, Godot's Android export templates for the pinned version in
Godot's data folder (`~/Library/Application Support/Godot/export_templates/<version>.stable/`:
`android_release.apk`, `android_debug.apk`, `version.txt`, from the release's `.tpz`), and the
Android SDK; `scripts/engine-export.sh` points Godot at the JDK from `scripts/env.sh` and signs
with Godot's debug key unless `GODOT_ANDROID_KEYSTORE_RELEASE_*` are set.

## Rules

- **Reuse first**: anything a second game would need lives in `core/addons/lillo_core` from the
  start, with its tests there; a game links it, never copies it. Core classes take colours and
  callbacks as arguments and never read a game's palette or progress themselves.
- **Rules apart from drawing**: `game/` never touches a node; `view/` never decides an outcome.
- **Upright and turned sideways**: projects use the sensor orientation and `canvas_items` /
  `expand` stretching, so a rotated phone gets a real layout, never a stretched one.
- **New `class_name` scripts need an import** (`scripts/godot.sh --headless --path <project>
  --import`) before other scripts can name them; `engine-test.sh` and `engine-export.sh` do it.
- Godot's `.godot/` cache, exports, GUT and third-party models are git-ignored; nothing
  downloaded is committed. Models are pinned by URL and SHA-256 in `assets/models/models.txt`;
  a game still runs without them, drawing `LilloFigure` characters instead.
- **Characters from models**: hundreds of skinned characters are too much for a phone, so crowds
  use `LilloPoseBaker` (four poses, about 30 % of the triangles) and only the few hero characters
  (leader, boss) play the full animation.
- Upgrading Godot or GUT: change the version and checksum at the top of `scripts/godot.sh` or
  `scripts/engine-test.sh`.
