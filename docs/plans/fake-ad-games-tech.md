# Fake-ad games: technology study

Status: **Godot decided** after S2; S3 and S4 pending. Product plan: [fake-ad-games.md](fake-ad-games.md).

The question: what to build the first two games with, and how they live in this repo next to the
Compose apps, reusing what already works (Play publishing, signing, listings, privacy pages, CI).

## What the two games need

| | Game one: maths-gates crowd runner | Game two: pull the pin |
|---|---|---|
| View | Real 3D, a chase camera over a track | 2D, a fixed vertical cut-away |
| Many things at once | 1,000+ units on screen: the platoon plus the enemies coming at it, and their bullets | 100–400 small bodies (gold, lava, water drops) |
| Physics | Little: lanes, gates, collisions with the enemy | Everything: falling bodies, pins as removable walls, liquids that pour and mix (lava + water → stone) |
| Feel | Numbers popping on gates, particles, haptics, a satisfying count-up | Pins that slide out under a finger, a pour that reads clearly |
| Content | Levels as data: gate sequence, enemy size, boss | Levels as data: walls, pins, what sits behind each |

Game one also has two modes, classic and maths, where the hardest pairs of gates stop time for 10 seconds ([plan](fake-ad-games.md#game-one-modes)): pausing is the engine's time scale, and the gate expressions come from a generator with tests. Both need levels loaded from data, a difficulty curve and progression **without** coins, ads or
purchases (the plan's rule).

## Options

| | Compose Canvas (as `slipper`) | **Godot 4** (4.7.2 stable) | Unity 6 |
|---|---|---|---|
| 3D and 1,000+ units | No real 3D; fake perspective only | Yes: `MultiMeshInstance3D` draws thousands of copies in one call | Yes, the strongest (GPU instancing, DOTS) |
| 2D physics and liquids | Needs a library (JBox2D) and hand-made fluids | Built-in 2D physics; particles with collision for liquids | Built-in 2D physics; fluids via asset store |
| Cost and licence | Free | Free, MIT | Free under US$200K a year; runtime fee cancelled, splash screen optional |
| Install | Already here | One binary (~100 MB) plus export templates, pinned by version and checksum from the official GitHub release | Editor of several GB, a Unity account activated on every CI runner |
| **Reviewable in a PR** | Kotlin | **Scenes (`.tscn`) and scripts (GDScript) are plain text** | Scenes and prefabs are serialized YAML, hard to read or merge |
| Headless build | Gradle | `godot --headless --export-release` to an AAB through Godot's Gradle build | Batch mode, more setup |
| App size | 1–2 MB | Tens of MB (**to measure**; custom export templates without unused modules shrink it) | Similar or larger |
| Reuse from this repo | `core` designsystem, everything | Publishing, signing, listings, privacy, CI conventions; not the Compose UI | Same as Godot, with more glue |

## Recommendation

**Godot 4 (pinned to the current stable, 4.7.2) for both games.** It covers real 3D crowds and 2D
physics in one engine, it is free with no strings, and its text formats keep the work reviewable
and writable from a session like the rest of the repo. Unity stays the fallback only if spike S2
cannot hold 60 fps with 1,000 units on a mid-range phone. Compose stays for the apps; it is the
wrong tool for a 3D crowd.

**One app per game**, not a collection: people search Play for the mechanic they saw in an ad,
each listing can show that mechanic, and it matches how this repo already ships one app per idea.

## Requirements every game keeps

- **Upright and landscape**: the phone turned sideways (and handhelds, tablets) gets a layout made
  for it, never a stretched or cropped one; the 3D camera widens its view instead of zooming. Every
  screen is checked both ways, as the apps' `+land` screenshots are.
- **What the apps already learned**: display cutouts and system bars never cover the HUD; back
  goes back (predictive back on Android 13+); "Remove animations" calms decorative motion;
  English and Spanish; contrast 4.5:1 for text; test the release build, not only debug.
- **Icons with care**: drawn by the repo's Pillow generators like the apps' (adaptive icon,
  monochrome layer, store graphics), reviewed at launcher size on a light and a dark wallpaper
  before any release.
- **Reuse first**: anything a second game would need goes to `engine/core` from the start; this is
  the first of several games.

## How it lives in the repo

```
engine/
  core/addons/lillo_core/   shared by every game: crowd/, maths/, render/, ui/, save/, dev/
  <game>/                   one Godot project per game: game/ (rules), view/ (drawing), assets/,
                            store/, tools/, tests/; README, DESIGN, OPERATIONS like an app
    play/                   (S4) thin Gradle module: Gradle Play Publisher + listing in src/main/play
```

The full layout and the class list: [engine/README.md](../../engine/README.md).

- **Publishing reuses the Release workflow.** Godot exports the AAB; the game's `play/` module
  applies Gradle Play Publisher with `artifactDir` pointing at that AAB, so upload, promote,
  listing, production approval and `scripts/play_status.py` work as for the apps. To verify in S4:
  that `artifactDir` uploads a bundle Gradle did not build.
- **Signing**: one upload key per game with `scripts/new-upload-key.sh`, in its own GitHub
  environment, as today.
- **CI**: a job installs the pinned Godot and templates (official release, SHA-256 checked, no
  wrapper installer), runs the GDScript tests and a headless debug export of the games a PR touches.
  The test framework (GUT or gdUnit4) is chosen in S1.
- **Privacy pages, App Links, listings**: same places and patterns as the apps.

## Spikes before deciding

| | Spike | Passes when |
|---|---|---|
| S1 ✅ | Pinned Godot install script, an `engine/core` addon and tests running headless, locally and in CI | Done: `scripts/godot.sh`, `scripts/engine-test.sh` (GUT 9.6.1), the Engine workflow; `LilloGate` and its tests in core, linked from `engine/summandos` |
| S2 ✅ | Crowd runner prototype: 1,000 units on a track through two gates; a debug APK for a real phone | Passed on the owner's Android handheld: the performance test holds **60 fps** in landscape with about 350 soldiers and 500 enemies on screen plus bullets; the first level is won. Prototype in `engine/summandos`. APK 50 MB with arm64 + x86_64, about 25 MB arm64 only |
| S3 | Pull-the-pin prototype: two pins, 300 bodies, lava meeting water | Pours read clearly and stay smooth on the phone |
| S4 | Headless AAB export plus Gradle Play Publisher `artifactDir` dry run to the internal track | The bundle reaches internal testing through the Release workflow |
| S3b | Smaller engine: Godot's Android export templates built without the modules the games do not use (advanced physics, networking, XR, extra importers), pinned and checksummed like the stock ones | The arm64 download drops from ~26 MB (the engine is ~24 MB of it) to 10–15 MB and every game still passes its tests and runs on the phone |

S3 needs the owner with a phone; the rest runs here. The engine decision is in
[decisions.md](../decisions.md).

## Legal line

The mechanics (maths gates, a cannon firing a crowd, pins holding back lava) are genre and free
to use. Names, characters, art, logos, sounds and the specific look of Count Masters, Mob Control,
Hero Rescue or any other game are not: everything visible is our own.
