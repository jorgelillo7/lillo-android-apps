# ¿Te pilla la chancla? (slipper runner)

Status: **prototype prepared** (2026-10-22), not on Play yet. Idea: [game-ideas.md](game-ideas.md) §1.

You run away from the grandma, who throws slippers at you. One finger: tap to jump (hold to jump
higher). Collect croquetas on the way; the more you eat, the angrier she gets and the faster she
throws. One slipper hit and she catches you. Runs are short (around 30 seconds at first), restart
is instant, and the end screen challenges a friend by link to the very same run.

## Decisions

| Decision | Choice | Why |
|---|---|---|
| Name | **¿Te pilla la chancla?** / *Will the slipper get you?* | Question + what it is, like the rest of the family; free on Play |
| Package / folder | `com.jorgelillo.slipper` / `apps/slipper` | Permanent once uploaded: **confirm before the first bundle** |
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

## To do (2026-10-23 on)

1. **Play the chase version on a phone** (2026-10-22 night: camera following the kid, rooms,
   furniture, the grandma chasing with a meter, the new icon). Then: fill the upper wall more
   (upper cabinets, more frames) or bring the camera closer; tune speed, gap losses, croqueta
   density (`GameConfig`, `House`).
2. Play it on a phone: tune jump, gravity, speeds and spawn gaps (`GameConfig`).
3. Feel: hit pause, shake, haptics, sounds.
4. Sprites polish (run cycle, grandma throw, slipper spin) and the icon (`tools/generate_icon.py`
   from the same sprites).
5. Website `/c/` page, privacy page publish, home card.
6. Upload key (`scripts/new-upload-key.sh slipper`), Play app, first bundle by hand.
7. Later: shouts, achievements, unlockable slippers (paid with croquetas, never money), bosses
   (the homing slipper, "¡a comer!").
