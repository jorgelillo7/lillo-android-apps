---
name: ¿Te pilla la chancla? (slipper)
description: Pixel-art runner in grandma's house — a sunny kitchen by day, the lamps on by night
character: "Grandma's house: the same rooms in daylight or lamplight; the characters never change"
light:             # Kitchen.Day (ui/theme/Kitchen.kt), KitchenLight
  Cream: "#FFECC8"        # background (to #FFD6AA), kitchen wall
  Raised: "#FFF6E6"       # panels, HUD
  Ink: "#3C2828"          # text
  Muted: "#6B4A3A"        # secondary text
  Green: "#1F7A5A"        # Play fill, white text on it
  GreenText: "#155C44"    # green as text
  Shout: "#B0263A"        # titles, challenge, caught; red as text
  Tile: "#468CC8"         # kitchen tiles (white #EBF0F5, grout #D9C9B0)
  Floor: "#B0623E"        # terracotta (edge #8C4628, plank #9E5434)
  Sky: "#9FD3F0"          # through the windows
dark:              # Kitchen.Night, KitchenDark
  Cream: "#2A1C17"        # background (to #1A110E)
  Wall: "#4B3A3F"         # kitchen wall
  Raised: "#33231E"       # panels, HUD, the bubble behind popping points
  Ink: "#FFECC8"          # text
  Muted: "#D9BFA3"        # secondary text
  Green: "#6BD6A8"        # Play fill, #10261C text on it
  GreenText: "#7FDDB5"    # green as text
  Shout: "#FF8A8F"        # red as text
  Tile: "#2B5480"         # kitchen tiles (white #7C8CA6, grout #4A4352)
  Floor: "#6B3A28"        # (edge #4A2518, plank #5A2E1E)
  Sky: "#1B2748"          # night through the windows; the cream cloud reads as the moon
  LampGlow: "#FFD27A"     # 11% under each ceiling lamp
both:              # the same by day and by night
  Slipper: "#E5465A"      # grandma's slipper (and its skins), the danger edge
  Croqueta: "#D68C32"
  sprites: "assets/sprites.txt: grandma, the kid, furniture, power-ups, the cat"
components:
  scene: "pixel art drawn in code from sprites.txt; the backdrop (walls, tiles, floors, window sky) from the look, everything that moves or can be picked up the same in both"
  hud: "Raised panels at 92% alpha over the scene"
  pops: "points and words over the scene on a popBubble at 85%, in the look's green or red text tone"
---

## Overview

Grandma's house in pixel art, in two looks that follow the phone, or the one picked in About →
Appearance: **a sunny kitchen by day** (cream walls, blue and white tiles, terracotta floor) and
**the same rooms at night** (walls about half as bright and a little cooler, a night sky through
the windows, a warm pool of light under each lamp). Grandma, the kid, the slipper, the croquetas
and everything on the floor keep their colours, so a run reads the same whatever the hour.

## Rules

- The scene's colours live in `KitchenLook`; the draw functions take the look resolved in
  composable scope (`Kitchen.look`). Pixel sizes are integers (no blurry scaling).
- At night the backdrop gets darker, never the sprites: if something on the floor stops standing
  out, lighten the wall or the tiles.
- HUD text always sits on a Raised panel (the tiles reach the top in landscape); points that pop
  over the scene sit on their own bubble.
- Screenshots: every screen in both looks, upright and landscape (`<screen>` night,
  `<screen>_light` day).
