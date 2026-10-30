---
name: ¿Quién llora esta jornada? (league-cards)
description: A fantasy-league card game told as a group chat — pitch at night, chat green
seed: "#00A884"
colors:            # Chat (ui/theme/Chat.kt)
  Ink: "#00110E"          # background (bottom)
  InkTop: "#00251F"       # background (top)
  Raised: "#001E19"       # cards, the swipe card
  Bubble: "#002C25"       # chat bubbles, trophy tiles
  System: "#001713"       # system messages in the chat
  Mine: "#005C4B"         # the player's own row/bubble
  Text: "#E5E6E3"         # text, near-white (Neutral scheme: green tinted it too much)
  Muted: "#AAABA9"        # secondary text
  Green: "#00A884"        # brand fill: Continue, positive points fill
  GreenText: "#5FD8BC"    # green as text: positive points
  Gold: "#FFC83D"         # trophies, joker, unlocked edges
  GoldText: "#F5CF6E"
  Tear: "#53BDEB"         # the tears meter
  Red: "#F15C6D"          # danger, meters near either end
  Hero: ["#00695C", "#1E5BB8"]   # summary hero, white text
meters: { money: Gold, squad: Green, patience: "#A791F5", tears: Tear }
components:
  swipe-card: "Raised, 24dp corners, character colour border; compact under 420dp; text ≥ 14sp then scrolls"
  chat-bubble: "Bubble, 4dp top-start corner; smaller text on low screens"
  trophy-tile: "Bubble, accentEdge(Gold) when unlocked"
---

## Overview

The league's group chat at night: dark pitch-green surfaces, near-white text, **chat green** for
the main actions and gains, gold for trophies and the joker, each character in their own colour.
Built on the shared recipe (seed `#00A884`, Vibrant; text from the Neutral scheme).

## Rules

- Each character keeps their colour (from the deck) for name, avatar ring and card border.
- Meters: one colour each, red near either end; the number always under the tile.
- The private deck's cards are long (≈140 characters): every layout is tested with that length on a
  360×700dp phone at font scale 1.3.
