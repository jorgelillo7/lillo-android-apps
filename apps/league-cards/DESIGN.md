---
name: ¿Quién llora esta jornada? (league-cards)
description: A fantasy-league card game told as a group chat — the chat by day, the pitch at night
character: "The league's group chat: light paper and white bubbles by day, pitch green at night; chat green for what you act on"
light:             # Chat.Day (ui/theme/Chat.kt), ChatLight
  Background: "#F4F1EA"   # top (to #ECE7DC at the bottom)
  Bubble: "#FFFFFF"       # chat bubbles, cards, the swipe card, trophy tiles
  Mine: "#D7F3E4"         # the player's own row/bubble
  System: "#FFF1C7"       # system messages; text #5C4300
  Text: "#0E1F1B"         # text
  Muted: "#54645F"        # secondary text
  Green: "#007A63"        # brand fill: Continue, white text on it (chat green is ~3:1)
  GreenText: "#00695C"    # green as text: positive points
  GoldText: "#7F5300"     # gold as text: trophies, the joker
  RedText: "#B3261E"      # red as text: lost points, a meter in danger
dark:              # Chat.Night, ChatDark
  Background: "#00251F"   # top (to #00110E at the bottom)
  Raised: "#001E19"       # cards, the swipe card
  Bubble: "#002C25"       # chat bubbles, trophy tiles
  Mine: "#005C4B"         # the player's own row/bubble
  System: "#001713"       # system messages; text #FFD279
  Text: "#E5E6E3"         # text, near-white (Neutral scheme: green tinted it too much)
  Muted: "#AAABA9"        # secondary text
  Green: "#00A884"        # brand fill: Continue, positive points fill
  GreenText: "#5FD8BC"    # green as text: positive points
  GoldText: "#F5CF6E"
  RedText: "#F15C6D"
both:              # the same in both looks
  Gold: "#FFC83D"         # trophies, joker, unlocked edges, the money meter
  Red: "#F15C6D"          # a meter in danger
  Tear: "#53BDEB"         # the tears meter
  Patience: "#A791F5"     # the patience meter
  Hero: ["#00695C", "#1E5BB8"]   # summary hero, white text
components:
  swipe-card: "Raised, 24dp corners, character colour border; compact under 420dp; text ≥ 14sp then scrolls"
  chat-bubble: "Bubble, 4dp top-start corner; smaller text on low screens"
  trophy-tile: "Bubble, accentEdge(Gold) when unlocked"
  rules: "open for a new player; folded behind \"¿Cómo se juega?\" once a season is under way or played"
---

## Overview

The league's group chat, in two looks that follow the phone or the one picked in About →
Appearance: **by day** light paper and white bubbles with dark text, **at night** dark pitch-green
surfaces with near-white text. In both, **chat green** for the main actions and gains, gold for
trophies and the joker, each character in their own colour.

## Rules

- Each character keeps their colour (from the deck) for avatar ring and card border. As a name
  it is shown through `readableOn`, moved towards black by day until it reads at 4.5:1 on the
  surface under it: the deck's colours were picked for the dark chat (the league's eight are all
  under 3:1 on white).
- Meters: one colour each, red near either end; the number always under the tile.
- The private deck's cards are long (≈140 characters): every layout is tested with that length on a
  360×700dp phone at font scale 1.3.
- Screenshots: every screen at night under its name, the main ones by day with `_light`.
