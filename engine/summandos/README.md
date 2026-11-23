# Summandos

**Sumandos** in Spanish: a soldier and his platoon run up a track shooting the enemies coming the
other way; maths gates grow or shrink the platoon, weapon crates make it deadlier but cost a gate.
Package `com.jorgelillo.summandos`. Plan and scope: [docs/plans/fake-ad-games.md](../../docs/plans/fake-ad-games.md).

Status: **prototype** (spike S2 of the [technology study](../../docs/plans/fake-ad-games-tech.md)):
three modes (easy: + − ×2 ÷2; normal: percentages and small sums; grown-up: powers, roots and
equations for x, with a 10-second thinking pause before a few gates), 30 generated levels each
with 1–3 stars and the next level opening on a win, and a 1,000-unit performance test. Look: toon-shaded figures with an
outline, a striped track whose halves take their gate's colour, a monster boss; two palettes,
**sunset** (the game's look) and **night** (the menu's night mode, remembered).

```
game/level.gd        level data (rows of choices, enemy waves) and the level generator: the same
                     level for the same mode and number, waves sized so the best choices win
game/progress.gd     stars per mode and level, saved; which levels are open
game/palette.gd      the colour themes (sunset, night): every colour the scene uses
game/run.gd          one run without drawing: steering, gates, shooting, clashes, win or loss
main.gd              the scene, built in code: track, squad, enemies, bullets, HUD, menu
tests/               GUT tests of the run (choices, weapons, waves, the first level is winnable)
tools/balance.gd     prints each level's difficulty (random choices' win rate)
tools/generate_icon.py   icons (a soldier and his squad under a gold plus), on scripts/icon_lib.py
```

## Commands

```bash
scripts/engine-test.sh engine/summandos                   # tests
scripts/godot.sh --path engine/summandos                  # open in the editor
scripts/godot.sh --path engine/summandos -- --mode=stress # run the performance test on the Mac
scripts/godot.sh --headless --path engine/summandos --script tools/balance.gd  # difficulty curve
scripts/engine-export.sh engine/summandos                 # APK in engine/summandos/export/ (debug-signed)
.venv/bin/python engine/summandos/tools/balance.gd     prints each level's difficulty (random choices' win rate)
tools/generate_icon.py  # regenerate the icons
```

`--mode=play|stress` starts a run at once, `--autosteer` takes the better gate by itself,
`--difficulty=easy|normal|adult --level=<n>` pick the level, `--palette=sunset|night` overrides the saved choice, `--shot=<png> --after=<s>` saves a screenshot
and quits. With the screen locked, Godot does not
draw a window; `--write-movie <png> --fixed-fps 10 --quit-after <frames>` renders frames anyway.

## What the prototype already does right

- Upright and landscape: the camera has a rig for each, the HUD keeps clear of cutouts and bars.
- Back returns to the menu, and from the menu leaves the game.
- English and Spanish, from the phone's language.
- The view mirrors nothing: the run's left is the screen's left (the camera looks down +z).
