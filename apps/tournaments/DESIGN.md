---
name: ¿Solo quedará uno? (tournaments)
description: Tournaments among friends — night arena, gold for winners
seed: "#2B3FD8"
colors:            # Arena (ui/theme/Arena.kt)
  Ink: "#0C072E"          # background (bottom)
  InkTop: "#1E184A"       # background (top of the gradient)
  Raised: "#181241"       # cards
  High: "#241D54"         # raised cards, selected rows
  Cream: "#E7E2FF"        # text (15.4:1 on Ink)
  Muted: "#ABA6D5"        # secondary text
  Faint: "#33E7E2FF"      # dividers, bracket lines not won
  Gold: "#FFC83D"         # brand fill: primary button, champion banner, finished edge
  GoldText: "#F5CF6E"     # gold as text: round names, winners' scores
  Accent: "#9DA7FF"       # harmonised lilac: in progress, selected tab, tap-to-score
  AccentText: "#9DA7FF"
  Coral: "#FF6B6B"        # danger fill
  CoralText: "#EA8080"    # danger as text (delete)
  Hero: ["#2B3FD8", "#7B3FE4"]   # summary hero, white text
components:
  tournament-card: "Raised, accentEdge(Accent in progress / Gold finished)"
  champion-banner: "Gold fill, Ink text"
  bracket: "Raised boxes, GoldText for winners, Faint lines (Gold for the winners' path)"
---

## Overview

A night arena: deep indigo backgrounds, cream text, **gold only for what is won** (the champion,
winners' scores, the finished tournaments) and a calm lilac for what is still going on. Built on the
shared recipe in `docs/design.md` (seed `#2B3FD8`, Vibrant).

## Rules

- Gold is the reward colour: champion banner, winners' scores and path, finished tournaments.
  Never for neutral actions besides the primary button.
- Lilac (`Accent`) marks *in progress* and selection; it replaces the old teal.
- The bracket uses every bit of width (`readable = false`); lines in `Faint`, the winners' path in
  `Gold` (also in the exported image).
