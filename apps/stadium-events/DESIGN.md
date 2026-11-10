---
name: ¿Hay jaleo? Metropolitano (stadium-events)
description: Busy days around the stadium, for neighbours aged 50–70 — light, big text, one answer
colors:            # ui/theme/JaleoTheme.kt (LightPalette, DarkPalette)
  Background: "#FBF7F1"   # cream (dark: #141B2D)
  Surface: "#FFFFFF"      # list rows (dark: #1F2940)
  Text: "#1E1B18"         # ink, also on every answer card in both themes
  SecondaryText: "#3F3A35"  # darker than usual on purpose: older eyes (dark: #D3D8E2)
  Accent: "#2F5D8A"       # primary (dark: #8FB8F2)
  Busy: "#F2B544"         # amber: a match tomorrow
  Max: "#E8705F"          # coral: concerts and finals
  Calm: "#8CC7A4"         # green: a quiet day
components:
  answer-card: "Busy / Max / Calm fill, Ink text, 28 dp corners, 40 sp answer"
  upcoming-row: "Surface, 10 dp edge in Busy or Max, 21 sp day + 18 sp what"
  widget: "Same colours as the answer card (drawable/widget_*.xml), Ink text"
  icon: "Stadium from above with two floodlights and an amber bell, on the house's dark-blue gradient"
---

## Overview

The opposite of the other apps' dark night designs: **light by default**, because dark text on a
light background reads best for the people it is for, outdoors and with tired eyes. The dark variant
is only used when the phone is in dark mode (some people choose it on purpose).

One screen, one question. The answer card's colour is the answer: amber for a match, coral for the
biggest days (concerts, finals), green for a quiet day. Text never sits in a light grey: secondary
text is a dark brown in light mode. Sizes are large on purpose (32 sp title, 40 sp answer, nothing
under 15 sp).

The shared system (`docs/design.md`) still applies: `LilloPage`, the About sheet, the screenshot
tests with the accessibility checks.
