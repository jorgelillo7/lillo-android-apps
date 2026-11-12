---
name: ¿Ya es Navidad? (christmas-countdown)
description: Christmas countdown — a snowy morning by day, the wine-to-midnight sky by night
character: "Christmas lights: berry red (light) or gold (dark) for what you act on; pine for opened doors; snow falls in both"
light:             # ChristmasLight (ui/theme/ChristmasTheme.kt)
  Snow: "#FFF7F0"         # background top (to #FBF1EE, then a cold #EAF1F6 at the bottom)
  Card: "#FFFFFF"         # sheets, dialogs; glass cards are white at 78%
  Ink: "#2B1418"          # text
  Muted: "#6B4F55"        # secondary text
  Berry: "#B5162F"        # brand fill: selected mode, tab, reminder button, music on; white text on it
  BerryText: "#A3122B"    # berry as text: "Days", screen titles, Christmas Day
  Pine: "#1E7A4A"         # opened doors (edge, 18% fill)
  Locked: "#6E5A61"       # locked door numbers
  Flakes: "#8FA6BF"       # snow, blue-grey so it shows on white
dark:              # ChristmasDark
  Wine: "#4A0E1C"         # background top (through #2A1020 into Midnight)
  Midnight: "#0B1426"     # background bottom: the night sky
  Snow: "#EEE3E2"         # text (Neutral scheme off-white)
  Muted: "#D9C8C8"        # secondary text
  Gold: "#FFC857"         # brand fill, dark text on it
  GoldText: "#F5CF6E"     # gold as text
  Pine: "#2A9D5C"         # opened doors
both:              # ChristmasColors, the same in both looks
  Berry: "#E63946"        # openable doors (into #9B1B30), white numbers, gold edge
hero: ["#E63946", "#9B1B30"]   # palette hero; no screen uses the summary hero
components:
  countdown: "the big number is the screen's hero: Figtree Black digits, 'Days' in accentText, glass unit boxes; no summary card"
  advent: "LilloScreenHeader (title in accentText, one line, 🎄), then the doors: the grid says the state, so no card above it"
  glass-card: "ChristmasLook.glass over the gradient, with a hairline border (white in dark, ink in light)"
  advent-door: "locked: glass with a lock; openable: Berry gradient, gold edge; opened: Pine edge with 🎁; 4/6/8 columns by width"
---

## Overview

Two looks that follow the phone, or the one picked in About › Appearance. **By day, a snowy
morning**: warm snow at the top cooling to a pale blue, blue-grey flakes, berry red for what you act
on. **By night, the wine-to-midnight sky** with gold lights, the look the app has always had. Glass
cards, the doors and the snow take their colours from `ChristmasLook`, picked by the theme's
background, so every piece follows the look.

## Rules

- Brand (berry by day, gold by night) for what is today and the main action; Berry doors you can
  open now; Pine for opened. Gold never carries text in the light look (it is a door's edge only).
- Neither screen uses `LilloSummaryHero`: the countdown's number is the hero, and the advent grid
  shows its own state under a light `LilloScreenHeader`.
- Snapshot tests pin the advent order and plan: colours may change, the algorithms may not.
- Screenshots: countdown, sleeps and advent in both looks, countdown and advent in landscape too;
  the countdown at twice the font size, upright and landscape (light).
- The home-screen widget keeps its own dark card in both looks.
