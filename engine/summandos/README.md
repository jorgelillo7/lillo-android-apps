# Summandos

**Sumandos** in Spanish: a soldier and his platoon run up a track shooting the enemies coming the
other way; maths gates grow or shrink the platoon, weapon crates make it deadlier but cost a gate.
Package `com.jorgelillo.summandos`. Plan and scope: [docs/plans/fake-ad-games.md](../../docs/plans/fake-ad-games.md).
Look: [DESIGN.md](DESIGN.md). Commands: [OPERATIONS.md](OPERATIONS.md).

Status: **in development**, not on Play yet.

- Three modes: **easy** (+ − ×2 ÷2), **normal** (percentages and small sums), **grown-up**
  (powers, roots and equations for x, with a 10-second pause to think before a few gates).
- 30 generated levels per mode, the same every time, each winnable by the best choices; 1–3 stars
  by soldiers left, the next level opening on a win; a boss every fifth level.
- A paged level map, menus over an animated backdrop, night mode, English and Spanish, upright
  and sideways; a 1,000-unit performance test.

```
main.gd              entry point: wires game/ and view/, input, back button, command-line options
game/level.gd        level data (rows of choices, enemy waves) and the level generator
game/run.gd          one run without drawing: steering, gates, shooting, clashes, thinking pauses
game/progress.gd     stars per mode and level, saved; which levels are open
game/palette.gd      the colour themes (sunset, night): every colour the game uses
game/texts.gd        every word on screen, English and Spanish
view/world.gd        the 3D world: track, gates, squad, enemies, boss, bullets, chase camera
view/ui.gd           the screens: menu, level map, HUD, result card
assets/icons/        launcher icons, written by tools/generate_icon.py
assets/models/       KayKit crossbowmen and skeletons (CC0), listed in models.txt, fetched by
                     scripts/engine-assets.sh; without them the game draws its own figures
assets/fonts/        Luckiest Guy (Astigmatic, Apache 2.0, from google/fonts) and its licence
store/               play-icon-512.png
tools/               generate_icon.py, balance.gd (difficulty report), samples/ (progress files)
tests/               GUT tests: runs, levels, progress, the addon link
```
