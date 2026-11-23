# engine/

Games built on a game engine (Godot today) instead of Kotlin and Compose. Games written in
Compose, such as `whos-lying`, `league-cards` or `slipper`, stay in `apps/`: this folder is about
the tool, not the genre. Plans: [docs/plans/fake-ad-games.md](../docs/plans/fake-ad-games.md) and
[the technology study](../docs/plans/fake-ad-games-tech.md).

```
engine/
  core/                       Godot project holding the shared addon and its tests
    addons/lillo_core/        what every game reuses (LilloGate today: maths gates)
    tests/                    GUT tests, res://tests/test_*.gd
  summandos/                  game one, Summandos (com.jorgelillo.summandos)
    addons/lillo_core         symlink to ../../core/addons/lillo_core
    tests/
```

## Commands

```bash
scripts/godot.sh --version                 # pinned Godot, downloaded on first use (checksum verified)
scripts/godot.sh --path engine/summandos   # open a project in the editor
scripts/engine-test.sh engine/core         # a project's tests, headless (GUT fetched on first use)
```

CI's **Engine** workflow runs every project's tests when `engine/` or these scripts change.

## Rules

- **Reuse first**: anything a second game would need lives in `core/addons/lillo_core` from the
  start, with its tests there; a game links it, never copies it.
- **Upright and turned sideways**: projects use the sensor orientation and `canvas_items` /
  `expand` stretching, so a rotated phone gets a real layout, never a stretched one.
- Godot's `.godot/` cache, exports and GUT are git-ignored; nothing downloaded is committed.
- Upgrading Godot or GUT: change the version and checksum at the top of `scripts/godot.sh` or
  `scripts/engine-test.sh`.
