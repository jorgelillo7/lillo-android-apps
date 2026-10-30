---
name: ¿Te pilla la chancla? (slipper)
description: Pixel-art runner in grandma's house — warm light kitchen
theme: light (on purpose: a sunny kitchen, not a night scene)
colors:            # Kitchen (ui/theme/Kitchen.kt)
  Cream: "#FFECC8"        # background
  Raised: "#FFF6E6"       # panels, HUD
  Ink: "#3C2828"          # text
  Muted: "#6B4A3A"        # secondary text
  Shout: "#B0263A"        # titles, challenge, caught
  Green: "#1F7A5A"        # Play
  GreenText: "#155C44"
  Slipper: "#E5465A"      # the slipper
  Tile: "#468CC8"         # kitchen tiles; Floor "#B0623E", Croqueta "#D68C32"
components:
  scene: "pixel art drawn in code (no image assets), cutaway house, rooms by palette"
  hud: "cream panels with 92% alpha over the scene"
---

## Overview

A sunny kitchen in pixel art: the only light, warm theme of the family, because the game happens at
home in the daytime. Not part of the dark recipe; same components, contrast rules and motion rules.

## Rules

- The scene's colours live in `Kitchen` too; pixel sizes are integers (no blurry scaling).
- HUD text always sits on a cream panel (the tiles reach the top in landscape).
