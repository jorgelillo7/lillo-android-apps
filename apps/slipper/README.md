# 🩴 ¿Te pilla la chancla? · Will the slipper get you?

A one-finger runner in the spirit of Super Mario Run, with Temple Run's chaser: the kid runs through
the house (kitchen, hall, living room) jumping furniture and stealing croquetas, and the grandma runs
behind throwing slippers. Bumping into furniture or a slipper lets her close in; she catches you when
the gap closes. Tap to jump, hold to jump higher. The end screen
challenges a friend by link to the very same run. Pixel art, no ads, no account, no server.

[On Google Play](https://play.google.com/store/apps/details?id=com.jorgelillo.slipper). Plan and decisions: [docs/plans/slipper.md](../../docs/plans/slipper.md).

## ✨ Features

- 🏃 One finger: tap to jump, hold to jump higher; the house is built ahead in chunks, so every run differs
- 🩴 Bumping into furniture or a slipper lets the grandma close in; she catches you when the gap closes
- 🔗 Challenge a friend by link to the very same run (`jorgelillo7.github.io/c/#…`)
- 🌗 Two looks, grandma's house by day and by night: Auto follows the phone, or pick Light or Dark in About (ⓘ)
- 🌍 Spanish and English, switchable in About (Android 13+) · 🚫 no ads, no account, no server

## 🏗️ Architecture

- `domain/` (pure Kotlin): `Engine.step` moves the whole run by one fixed step (1/120 s) from a
  `World` and the finger's `Input`: the kid's run and jump (landing on furniture), the chase gap,
  the slippers. `House` builds the house ahead in chunks (furniture and croquetas from the seed and
  the chunk index), so a challenge link replays the same house and the same slippers. All the tuning numbers are in `GameConfig`.
  `Challenges` puts `{seed, score}` in a `jorgelillo7.github.io/c/#…` link (core `ShareLink`).
  `Sprites` parses the pixel art.
- `app/`: `GameScreen` runs the engine at its fixed rate on every frame (`withFrameNanos`) and
  `GameScene` draws a `World` on a `Canvas` with a camera that follows the kid: each room (wall,
  decoration, floor, ceiling), furniture, croquetas, the grandma and the slippers. `ChaseMeter`
  shows how close she is. Home, run and result are type-safe routes; the best score lives in a `JsonStore`.
- **Pixel art** lives in [`app/src/main/assets/sprites.txt`](app/src/main/assets/sprites.txt): one
  character per pixel and a palette. The app draws it, and `tools/` reads the same file for the
  icon and the store graphics. Change a drawing there, nowhere else.

## 🧪 Tests

`domain` (jump, buffering, hold, collisions, croquetas, determinism, difficulty, challenge link,
sprites file), `SlipperViewModelTest`, `SlipperFlowTest` (a whole run through `MainActivity`) and
screenshot tests with the accessibility checks. Release smoke test: `tools/smoke_test.sh`.
