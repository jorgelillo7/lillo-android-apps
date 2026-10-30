---
name: ¿Quién miente? (whos-lying)
description: The impostor party game — violet neon at night
seed: "#7756FA"
colors:            # Neon (ui/theme/Theme.kt)
  Night: "#14052B"        # background
  Card: "#210F3D"         # cards, tiles
  CardHigh: "#2F1A50"     # raised cards, selected rows
  Ink: "#EEE0FF"          # text
  Muted: "#B6A3D1"        # secondary text
  Violet: "#7756FA"       # brand fill: Play, selection (white text on it). Not for text (4.1:1)
  VioletLight: "#8B6EFF"
  Turquoise: "#00E0B8"    # the secret word, positive
  TurquoiseText: "#6AE7D0"
  Pink: "#FF4D94"         # impostors, danger, your own packs (edge)
  PinkText: "#FFA3C8"
  Amber: "#FFC94D"        # hints, warnings
  AmberText: "#FFE29A"
  Go: "#00D26A"           # go actions: hide the card, vote, next round
  GoText: "#6AE7A9"
  Hero: ["#5B2EE6", "#C2185B"]   # summary hero, white text
components:
  pack-row: "Card, accentEdge(Pink) for the group's own packs"
  pack-tile: "Card; selected: Violet 25% fill + 2dp Violet border"
  go-button: "LilloBigButton with Go fill, Night text"
---

## Overview

A party game at night: violet surfaces, **turquoise for the secret word**, pink for the impostor,
green to move the game on. Built on the shared recipe (seed `#7756FA`, Vibrant).

## Rules

- Roles have fixed colours: civilians turquoise, impostor pink, drifter amber. Never swap them.
- "Go" (green) is the only colour for the action that moves the round forward.
- Violet is a fill colour; as text use `VioletLight` or the text tones.
