---
name: ¿Ya es Navidad? (christmas-countdown)
description: Christmas countdown — wine to midnight sky, gold lights
seed: "#C8102E"
colors:            # ChristmasColors (ui/theme/ChristmasTheme.kt)
  Wine: "#481222"         # background top
  Mid: "#2A1020"          # background middle
  Midnight: "#0B1426"     # background bottom: the night sky
  Snow: "#EEE3E2"         # text (Neutral scheme off-white)
  Gold: "#FFC857"         # brand: lights, today's door, primary button
  GoldText: "#F5CF6E"     # gold as text: "Days", Christmas Day title
  Pine: "#2A9D5C"         # opened doors
  Berry: "#E63946"        # openable doors; as text use a lighter tone (4.4:1)
components:
  glass-card: "white 8–12% over the gradient, 1dp white 15% border"
  advent-door: "locked: glass; openable: Berry; opened: Pine border with 🎁; 4/6/8 columns by width"
  countdown: "Black display digits, glass unit boxes"
---

## Overview

Wine red fading into a midnight sky, gold lights, glass cards: festive but quiet enough to open
every day of Advent. The recipe's tint stays on the doors and cards; the **night-sky gradient is the
app's identity** and is kept on purpose.

## Rules

- Gold for what is today and the main action; Berry for doors you can open now; Pine for opened.
- Snapshot tests pin the advent order and plan: colours may change, the algorithms may not.
