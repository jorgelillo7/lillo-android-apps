# ¿Te pilla la chancla? (slipper runner)

Status: **v1 ready for its first release** (2026-10-25: gameplay and look reworked, see below), not on Play yet. Idea: [game-ideas.md](game-ideas.md) §1.

You run away from the grandma, who throws slippers at you. One finger: tap to jump (hold to jump
higher). Collect croquetas on the way; the more you eat, the angrier she gets and the faster she
throws. One slipper hit and she catches you. Runs are short (around 30 seconds at first), restart
is instant, and the end screen challenges a friend by link to the very same run.

## Decisions

| Decision | Choice | Why |
|---|---|---|
| Name | **¿Te pilla la chancla?** / *Will the slipper get you?* | Question + what it is, like the rest of the family; free on Play |
| Package / folder | `com.jorgelillo.slipper` / `apps/slipper` | Permanent once uploaded; confirmed 2026-10-25 |
| Control | Side view, portrait, tap to jump, hold for a higher jump | Simplest to make feel good and to understand in a second |
| Look | Pixel art, "grandma's kitchen" palette (blue and white tiles, terracotta floor, red slipper) | Light and warm, unlike the other (dark) apps; sprites drawn as character grids, no AI |
| v1 scope | The run, best score, **challenge by link** | Small v1; shouts, achievements and unlockables later |
| Camera and chase (2026-10-22, after playing the first prototype) | **Mario Run style**: the camera follows the kid through the house (kitchen, hall, living room) and the furniture changes all the time; **the grandma chases in sight, Temple Run style**: she closes in when you stumble or a slipper hits you, falls back when you run clean, and catches you when the gap closes | The first prototype (fixed camera, scrolling tiles) read as static; the user pictured Mario Run, Sonic Dash, Temple Run. Lanes and a 3D view (Temple Run, Sonic Dash) would need another engine |
| Icon | Recognisable at a glance: the grandma with her slipper raised | The first one (a slipper on blue) was too abstract |
| Store category | Game · Arcade (Casual) | |
| Website | "Juegos / Games" block, with Who's lying? and the league game | |

Market check (2026-10-22): Angry Gran Run (50M+, the grandma is *you*), Temple Run, Crossy Road,
all with ads and in-app purchases; "Chancla Survivors" (10+ downloads) is a survivors-like, another
genre. Nobody does "run from grandma's slipper": the joke is instantly recognisable in Spain and
Latin America. Our difference: the joke, no ads, short runs, the challenge link.

## Rework of 2026-10-25: what makes a run fun

What the genre's hits do (Wikipedia and store pages, read 2026-10-25):

| Game | Control | Chaser | Keeps a run lively | Brings you back |
|---|---|---|---|---|
| Jetpack Joyride | one touch (hold to rise) | — | missile **warnings at the edge**, near misses, vehicles | 3 missions at a time, levels, outfits |
| Subway Surfers | swipes | inspector and dog **in sight** | power-ups (sneakers, jetpack, magnet), x2 multiplier | daily challenge, world tour seasons, characters |
| Super Mario Run | one touch, auto-run, hold for higher | — | coins, vaulting | ghost races (Toad Rally), Remix 10 |
| Chrome Dino | one key / tap | — | speed rises, day/night | best score |
| Angry Gran Run | swipes | — | shield, magnet, turbo | missions, daily goals, costumes |

What we took, without ads or purchases:

| Problem in the prototype | Change |
|---|---|
| Upright, the grandma was off screen (camera 50 units wide, she started 24 behind) and most of the screen was bare wall | Camera framed to show more behind (she runs in from the edge when close), upright shows the house in cut-away (the floor upstairs, cabinets, lamps); wide screens put the HUD in one row |
| Slippers from off screen came with no warning | She **winds up** (0.45 s, a "!" over her head) and, when off screen, a **warning at the left edge** at the height the slipper will fly (Jetpack's missiles) |
| One hit + stumble cost ~10 units of a 16 gap: two hits and out | Softer hits and quicker recovery; balanced with bots (below) |
| Nothing but jumping | **Near misses** (+5, she loses ground), **croqueta combos** (×1…×5), **sandwich** (blocks one hit), **skates** (fast, crash through furniture), the **cat** (walks at you), **toy bricks**, **pairs of throws** after 20 s |
| No feel | Hit pause (70 ms), screen shake, squash on landing, dust, crumbs, pop-up words, haptics, chiptune sounds (generated, `tools/generate_sounds.py`), mute |
| No reason to come back | **Run of the day** (same seed for everyone each day, today's best), **slipper skins** unlocked by croquetas eaten over all runs (150, 400, 800) |

Balance (`BalanceTest`, 200 seeds): a decent bot lasts ~26 s (median), a random tapper ~13 s; every
run ends. Tune by feel on a phone in `GameConfig`, then check the report still holds. Challenge links
carry `GameConfig.VERSION` (2).

## Design

- **World** in logical units, independent of the screen: the kid stays at a fixed x, the world
  scrolls left. The grandma runs behind (left) and throws slippers that fly right, faster than the
  world: **low** ones along the floor (jump them) and **high** ones at head height (stay on the
  ground). Croquetas come from the right, some in the air: jumping for them is the risk.
- **Deterministic** with a fixed time step and a seeded random source per event stream (throw *n*
  and croqueta *n* depend only on the seed and *n*), so a challenge link replays the same pattern.
  Eating croquetas makes the grandma angrier: her slippers fly faster, never at different times.
- **Score**: distance (time survived) plus 10 per croqueta. Best score saved on the phone.
- **Challenge link**: `jorgelillo7.github.io/c/#<code>` with `{seed, score}` (core `ShareLink`), like
  `/t/` and `/p/`. The website page `/c/` (to do) shows "beat 1,240!" and the Play button.
- **Feel** (the most important part, tune on a phone): jump buffering and coyote time, a 50 ms
  hit pause, screen shake, haptics, short sounds, gradual difficulty.

## Prepared on 2026-10-22

- `:apps:slipper:domain`: the engine (`Engine.step`), the house in chunks (`House`), the chase,
  sprites parser, challenge link, unit tests (21).
- `:apps:slipper:app`: Compose `Canvas` loop with fixed steps, pixel-art sprites from
  `assets/sprites.txt`, home / game / game over, challenge links in and out, best score in a
  `JsonStore`; ViewModel, flow and screenshot tests.
- Listing texts, release notes, privacy policy draft (website repo, not published), docs.

## To do

1. Play it on a phone and tune by feel (`GameConfig`): jump, speeds, how much a hit costs. Keep
   `BalanceTest` passing; bump `GameConfig.VERSION` when runs change.
2. First release: see `apps/slipper/OPERATIONS.md` → Release (upload key, Play app, website `/c/`
   and privacy pages, first bundle by hand).
3. Later: missions (three at a time, like Jetpack Joyride), shouts, bosses (the homing slipper,
   "¡a comer!"), more rooms.
