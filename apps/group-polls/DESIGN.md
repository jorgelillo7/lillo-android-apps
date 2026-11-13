---
name: ¿Qué votáis? (group-polls)
description: Two-answer polls for groups — a voting paper by day, graphite at night, red vs blue
character: "A voting paper: white cards on light paper by day, graphite at night; red against blue for the two answers"
light:             # Polls.Day (ui/theme/Theme.kt), PollsLight
  Paper: "#F3F6FB"        # background (header from #DDEBFF)
  Card: "#FFFFFF"
  Ink: "#1B2433"          # text; also the selected pill and the New poll button, paper text on it
  Muted: "#5A6577"
  Line: "#E2E8F1"
  Red: "#C81E3A"          # answer A as text, border, dot; RedSoft "#FFE3E6"
  Blue: "#1E50E0"         # answer B and the main button; BlueSoft "#E1EAFF"
dark:              # Polls.Night, PollsDark
  Paper: "#14161A"        # background (header from #1B2233): neutral graphite, not navy
  Card: "#1E2127"         # raised #272B33
  Ink: "#ECEFF4"          # text; also the selected pill and the New poll button, paper text on it
  Muted: "#A9B1BF"
  Line: "#2E333D"
  Red: "#FF8A93"          # answer A as text, border, dot; RedSoft "#3A1A20"
  Blue: "#8FB0FF"         # answer B and the main button (#0E1830 text on it); BlueSoft "#1A2444"
both:              # the same in both looks
  RedFill: "#C81E3A"      # the answers' big fills and the split bar, white text (from #D93348)
  BlueFill: "#1E50E0"     # (from #3366F0)
---

## Overview

A voting paper: white cards on light paper by day, graphite cards at night, **red against blue**
for the two answers. The look follows the phone or the choice in About → Appearance. Parked until
its Firebase project exists.

## Rules

- Red and blue have a text tone per look (`Polls.Red`, `Polls.Blue`, `Polls.color(side)`) and one
  fill for both (`RedFill`, `BlueFill`, white text): at night the text tones are lighter, the fills
  are not.
- The night is neutral graphite on purpose: a blue-tinted background would hide the blue answer.
- `Page` and `BigButton` in `ui/screens/Common.kt` stay: they carry this app's own style (black
  titles, a 56 dp round main button), not just core's defaults.
- Screenshots: every screen at night under its name, by day with `_light`.
