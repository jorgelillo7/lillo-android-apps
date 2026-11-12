---
name: ¿Solo quedará uno? (tournaments)
description: Tournaments among friends — a trophy room by day, a floodlit stadium by night, gold for winners
character: "Arena: gold only for what is won, blue for what is going on, navy ink"
light:             # ArenaLight (ui/theme/Arena.kt)
  Ivory: "#FBF8F1"        # background (from #F3EEDF at the top)
  Card: "#FFFFFF"         # cards
  High: "#F1ECDD"         # raised cards, selected rows
  Ink: "#141A33"          # text
  Muted: "#545B78"        # secondary text
  Gold: "#F2B705"         # brand fill: primary button, champion banner, finished edge; ink text on it
  Bronze: "#7F5300"       # gold as text: round names, winners; Material's primary (text buttons)
  Blue: "#2B3FD8"         # in progress, selected tab, tap-to-score (fill and text)
  CoralText: "#B3261E"    # danger as text (delete)
dark:              # ArenaDark
  Navy: "#0A1024"         # background (from #18213F at the top)
  Card: "#141C38"         # cards
  High: "#1F2A4D"         # raised cards, selected rows
  Cream: "#E4E8FF"        # text
  Muted: "#A6AECF"        # secondary text
  Gold: "#FFC83D"         # brand fill, navy text on it
  GoldText: "#F5CF6E"     # gold as text
  Lilac: "#9DB0FF"        # in progress, selected tab, tap-to-score
  CoralText: "#EA8080"    # danger as text (delete)
hero: ["#2B3FD8", "#7B3FE4"]   # summary hero, white text, both looks
components:
  home: "summary hero (in progress, matches to play, last champion: real state) over the tournament cards"
  tournament-card: "Card, accentEdge(Blue/Lilac in progress, Gold finished)"
  champion-banner: "Gold fill, ink text"
  bracket-rounds: "a phone upright: a tab per round (opening on the one being played) over full-width cards, 16sp names, winner in the gold text tone"
  bracket-fit: "landscape and tablets: the two-sided bracket fitted, zoom -/Fit/+; Faint lines, Gold for the winners' path"
  export: "the shared bracket image always uses the night colours (ArenaNight)"
---

## Overview

Two looks that follow the phone, or the one picked in About: **a trophy room by day** (ivory,
navy ink) and **a floodlit stadium by night** (deep navy). In both, **gold only for what is won**
(the champion, winners' scores and path, finished tournaments) and blue for what is still going
on.

## Rules

- Gold is the reward colour: champion banner, winners' scores and path, finished tournaments.
  Never for neutral actions besides the primary button. As text it is the bronze or soft gold
  tone of the look.
- Blue (lilac by night) marks *in progress* and selection.
- On a phone held upright the bracket reads one round at a time; landscape and tablets show the
  whole bracket fitted (`readable = false`), lines in `Faint`, the winners' path in `Gold`.
- Screenshots: home, tournament and stats in both looks; the bracket round by round in both
  looks and fitted in landscape; tablets in the dark look.
