# 🩴 ¿Te pilla la chancla? · Will the slipper get you?

A one-finger runner: you run away from the grandma, dodging the slippers she throws and stealing
her croquetas. Tap to jump, hold to jump higher; one slipper and she catches you. The end screen
challenges a friend by link to the very same run. Pixel art, no ads, no account, no server.

Status: **prototype** (2026-10-22). Plan, decisions and to-do: [docs/plans/slipper.md](../../docs/plans/slipper.md).

## 🏗️ Architecture

- `domain/` (pure Kotlin): `Engine.step` moves the whole run by one fixed step (1/120 s) from a
  `World` and the finger's `Input`; deterministic per seed (one random source per throw and per
  croqueta), which is what a challenge link replays. All the tuning numbers are in `GameConfig`.
  `Challenges` puts `{seed, score}` in a `jorgelillo7.github.io/c/#…` link (core `ShareLink`).
  `Sprites` parses the pixel art.
- `app/`: `GameScreen` runs the engine at its fixed rate on every frame (`withFrameNanos`) and
  `GameScene` draws a `World` on a `Canvas`: the kitchen (tiles and floor scrolling at two speeds)
  and the sprites. Home, run and result are type-safe routes; the best score lives in a `JsonStore`.
- **Pixel art** lives in [`app/src/main/assets/sprites.txt`](app/src/main/assets/sprites.txt): one
  character per pixel and a palette. The app draws it, and `tools/` reads the same file for the
  icon and the store graphics. Change a drawing there, nowhere else.

## 🧪 Tests

`domain` (jump, buffering, hold, collisions, croquetas, determinism, difficulty, challenge link,
sprites file), `SlipperViewModelTest`, `SlipperFlowTest` (a whole run through `MainActivity`) and
screenshot tests with the accessibility checks. Release smoke test: `tools/smoke_test.sh`.
