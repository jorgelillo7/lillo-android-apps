---
name: ¿Qué hacemos? (decision-wheel)
description: Spin and decide — a sunset wheel on warm paper by day and plum wine by night
character: "Sunset: the wheel is the only rainbow; raspberry (light) or sun yellow (dark) for what you act on"
light:             # WheelLight (ui/theme/WheelTheme.kt)
  Paper: "#FFF6EC"        # background (to #FFEBDC at the bottom)
  Card: "#FFFFFF"         # cards, sheets
  CardHigh: "#FCEBDD"     # raised cards, menus
  Ink: "#2A1424"          # text
  Muted: "#6E5466"        # secondary text
  Raspberry: "#C2185B"    # brand fill: New wheel, Spin, favourite star and edge; white text on it
  RaspberryText: "#A3124B" # raspberry as text: title, option counts, result
  Teal: "#00796B"         # Save
  Danger: "#B3261E"       # delete
dark:              # WheelDark
  Wine: "#1E0A1B"         # background (from #2A0F25 at the top)
  Card: "#2E1429"         # cards, sheets
  CardHigh: "#3D1D37"     # raised cards, menus
  Cream: "#FBEAF2"        # text
  Muted: "#D4B6C8"        # secondary text
  Sun: "#FFD93D"          # brand fill, wine text on it
  SunText: "#F3D56A"      # sun as text
  Teal: "#4ECDC4"         # Save
  Danger: "#EA8080"       # delete
wheel:             # WheelColors, the same in both looks
  Segments: ["#FF6B6B", "#FFB347", "#FFD93D", "#4ECDC4", "#6C8EF5", "#C792EA"]  # neighbours contrast
  Ink: "#1E0A1B"          # hub, pointer outline, labels on light segments
  Cream: "#FBEAF2"        # pointer, labels on dark segments
hero: ["#C2185B", "#7B1FA2"]   # palette hero (raspberry into grape, white text); the home doesn't use it
components:
  home: "the title in the display face and accent colour, then a light line (most picked) and the wheels; no summary card: the wheels are the home"
  last-result: "a card under the wheels: the last decision in the display face and accent colour, its wheel and day, Spin again (outlined); with no decision yet, an invitation to spin the favourite. Only when the list leaves 240dp free; never on short screens or once the list scrolls; then a compact \"Last:\" line under the title instead"
  wheel-card: "Card, mini wheel, accentEdge(brand) for favourites only"
  wheel: "Segments in order; label in Ink or Cream, whichever contrasts (onSegment)"
  wheel-title: "the wheel's name shrinks to fit (16–22sp), then ends in an ellipsis"
---

## Overview

A sunset wheel, and around it a calm room: **warm paper by day, plum wine by night**, following
the phone unless the user picks a look in About. The six segment colours are the only rainbow in
the app. What you act on (New wheel, Spin, the favourite star) is **raspberry by day and sun yellow
by night**, so the app no longer shares the violet night of whos-lying and tournaments.

## Rules

- The segment palette is fixed and ordered so neighbours contrast; never reuse it for UI.
- Brand fill = act (New wheel, Spin) or favourite (star, card edge); as text it is the softer
  accent tone.
- The home doesn't restate its list: no "2 wheels · 4 decisions" card, only what the list can't
  say: the most picked option under the title, and the last decision, large, in a card under
  the wheels when there is room for it, else a compact "Last:" line under the title (said once,
  never both).
- On short screens "New wheel" sits in the title row (a floating button covered the last star).
- Screenshots: home and wheel in both looks, upright and landscape; the home before any decision
  in both looks; the wheel at twice the font
  size, upright and landscape.
