# Fake-ad games: technology study

Status: **recommendation, pending the spikes below**. Product plan: [fake-ad-games.md](fake-ad-games.md).

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

Game one also has three modes (classic, maths, and a think mode that stops time for 10 seconds at each gate; [plan](fake-ad-games.md#game-one-modes)): pausing is the engine's time scale, and the gate expressions come from a generator with tests. Both need levels loaded from data, a difficulty curve and progression **without** coins, ads or
purchases (the plan's rule).

## Options

| | Compose Canvas (as `slipper`) | **Godot 4** | Unity 6 |
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

**Godot 4 (pinned to the current stable, 4.6.x) for both games.** It covers real 3D crowds and 2D
physics in one engine, it is free with no strings, and its text formats keep the work reviewable
and writable from a session like the rest of the repo. Unity stays the fallback only if spike S2
cannot hold 60 fps with 1,000 units on a mid-range phone. Compose stays for the apps; it is the
wrong tool for a 3D crowd.

**One app per game**, not a collection: people search Play for the mechanic they saw in an ad,
each listing can show that mechanic, and it matches how this repo already ships one app per idea.

## How it lives in the repo

```
games/
  core/                 Godot addon shared by every game: crowd (MultiMesh), gates and number
                        pop-ups, level loader, haptics, save, settings and About screen
  <game>/               one Godot project per game; addons/lillo_core points at ../core
    levels/             level data
    play/               thin Gradle module: Gradle Play Publisher + listing in src/main/play
```

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
| S1 | Pinned Godot install script, an empty `games/core` addon and one test running headless, locally and in CI | The test runs in CI with no manual step |
| S2 | Crowd runner prototype: 1,000 units on a track through two gates; a debug APK for a real phone | 60 fps on the owner's phone with 1,000 units; record the APK size |
| S3 | Pull-the-pin prototype: two pins, 300 bodies, lava meeting water | Pours read clearly and stay smooth on the phone |
| S4 | Headless AAB export plus Gradle Play Publisher `artifactDir` dry run to the internal track | The bundle reaches internal testing through the Release workflow |

S2 and S3 need the owner with a phone; the rest runs here. The engine decision goes into
[decisions.md](../decisions.md) after S2.

## Legal line

The mechanics (maths gates, a cannon firing a crowd, pins holding back lava) are genre and free
to use. Names, characters, art, logos, sounds and the specific look of Count Masters, Mob Control,
Hero Rescue or any other game are not: everything visible is our own.
