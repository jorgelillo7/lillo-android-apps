# Summandos: operations

Shared engine commands and setup: [engine/README.md](../README.md). Run from the repo root.

```bash
scripts/engine-assets.sh engine/summandos                   # fetch the 3D models (once; before export)
scripts/engine-test.sh engine/summandos                     # tests
scripts/godot.sh --path engine/summandos                    # open in the editor
scripts/godot.sh --path engine/summandos -- --mode=stress   # performance test (not in the menu)
scripts/godot.sh --headless --path engine/summandos --script tools/balance.gd   # difficulty curve
.venv/bin/python engine/summandos/tools/generate_icon.py    # regenerate the icons
.venv/bin/python engine/summandos/tools/generate_sounds.py  # regenerate effects and music
.venv/bin/python scripts/engine_icon_preview.py engine/summandos /tmp/icon.png  # check them
scripts/engine-template.sh engine/summandos                 # slim engine (5 min, once per profile change)
scripts/engine-export.sh --arm64 engine/summandos           # APK for a phone (export/, debug key)
```

Store listing graphics (7 phone and 7 tablet screenshots and the feature graphic, Spanish and
English):

```bash
scripts/engine-assets.sh engine/summandos                   # models, if not fetched yet
engine/summandos/tools/capture_store_screenshots.sh         # store/raw[-tablet]/<locale>/
.venv/bin/python engine/summandos/tools/generate_store_assets.py   # play/src/main/play/listings/
```

Screenshots (`scripts/engine-shot.sh`, add `--landscape` for sideways, `--store` for 1080x2424):

```bash
scripts/engine-shot.sh engine/summandos menu.png 2
scripts/engine-shot.sh --progress engine/summandos/tools/samples/progress-easy.cfg \
  engine/summandos levels.png 2 -- --levels
scripts/engine-shot.sh engine/summandos run.png 2.5 -- --mode=play --autosteer
scripts/engine-shot.sh engine/summandos adult.png 2.5 -- --mode=play --difficulty=adult --level=12
```

Game options after `--`: `--mode=play|stress` starts a run at once, `--difficulty=easy|normal|adult`
and `--level=<n>` pick it, `--autosteer` takes the better gate, `--levels` opens the level map,
`--palette=sunset|night` overrides the saved choice, `--shot=<png> --after=<s>` saves a
screenshot and quits.

Releasing (S4 of the [technology study](../../docs/plans/fake-ad-games-tech.md)) is not set up
yet; `release-notes.md` and the Play listing arrive with it.
