---
name: ¿Quién miente? (whos-lying)
description: The impostor party game — lavender menus by day, violet neon at night, vivid stages for the round
character: "Neon: violet to act, turquoise for the word, pink for the impostor, amber for hints, green to move on"
light:             # NeonLight (ui/theme/Theme.kt)
  Lavender: "#F6F2FF"     # background (from #E4DAFF at the top)
  Card: "#FFFFFF"         # cards, tiles, sheets
  CardHigh: "#ECE4FF"     # raised cards, selected rows
  Ink: "#1D1035"          # text
  Muted: "#5D4E78"        # secondary text
  Violet: "#5B3BE0"       # brand fill: Play, selection; white text on it
  VioletText: "#4B2BC8"   # violet as text
  TurquoiseText: "#006B5A"
  PinkText: "#B0124F"
  AmberText: "#7F4C00"
dark:              # NeonDark
  Night: "#14052B"        # background (from #2B1B6B at the top)
  Card: "#210F3D"         # cards, tiles
  CardHigh: "#2F1A50"     # raised cards, selected rows
  Ink: "#EEE0FF"          # text
  Muted: "#B6A3D1"        # secondary text
  Violet: "#7756FA"       # brand fill (white text on it). Not for text (4.1:1)
  VioletText: "#8B6EFF"
  TurquoiseText: "#6AE7D0"
  PinkText: "#FFA3C8"
  AmberText: "#FFE29A"
fills:             # Neon, the same in both looks
  Turquoise: "#00E0B8"    # the secret word, civilians
  Pink: "#FF4D94"         # impostors, danger, your own packs (edge)
  Amber: "#FFC94D"        # hints, warnings
  Go: "#00D26A"           # go actions: hide the card, vote, next round
  OnFill: "#14052B"       # text and icons on those fills and on avatars
hero: ["#5B2EE6", "#C2185B"]   # summary hero, white text
components:
  home: "the title, then the summary hero only once a group exists (leader, game N of M, mode): real state; a first launch has none"
  stages: "reveal, countdown, debate, time up, voting, expose, result, ranking and podium play on vivid backdrops (RevealBackdrop and the outcome gradients) inside NightStage: the same in both looks"
  pack-row: "Card, accentEdge(Pink) for the group's own packs"
  pack-tile: "Card; selected: Violet 25% fill + 2dp Violet border"
  go-button: "LilloBigButton with Go fill, OnFill text"
---

## Overview

A party game. The menus (home, players, setup, packs, drawing, sheets) follow the phone, or the
look picked in About: **lavender by day, violet night by night**. The round itself plays on vivid
stages (violet for the reveal and the vote, blue, orange or red for who won) that look the same in
both, so the moment of the game never changes with the phone's setting.

## Rules

- Roles have fixed colours: civilians turquoise, impostor pink, drifter amber. Never swap them.
- "Go" (green) is the only colour for the action that moves the round forward.
- Fills (violet, turquoise, pink, amber, go) carry `OnFill` or white; as text each accent has its
  own tone per look (`Neon.TurquoiseText`, `PinkText`, `AmberText`, `VioletLight`).
- A new round screen on a vivid backdrop goes inside `NightStage`.
- Screenshots: the menus in both looks, upright and landscape; the stages in the dark look (voting
  also in the light one, to show it is the same); the home with a group at twice the font size.
