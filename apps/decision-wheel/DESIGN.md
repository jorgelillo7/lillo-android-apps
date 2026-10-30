---
name: ¿Qué hacemos? (decision-wheel)
description: Spin and decide — violet night, a sunset wheel, sun-yellow actions
seed: "#6A3DE8"
colors:            # WheelColors (ui/theme/WheelTheme.kt)
  Night: "#15052B"        # background
  NightRaised: "#220E3C"  # cards
  NightHigh: "#30194F"    # raised cards, menus
  Cream: "#EFDFFF"        # text
  Muted: "#B7A2D1"        # secondary text
  Sun: "#FFD93D"          # brand fill: New wheel, Spin, favourite star and edge
  SunText: "#F3D56A"      # sun as text: option counts, result title
  Coral: "#FF6B6B"        # danger fill
  CoralText: "#EA8080"    # danger as text (delete)
  Segments: ["#FF6B6B", "#FFB347", "#FFD93D", "#4ECDC4", "#6C8EF5", "#C792EA"]  # the wheel; neighbours contrast
  Hero: ["#C2185B", "#4527A0"]   # summary hero, white text
components:
  wheel-card: "NightRaised, mini wheel, accentEdge(Sun) for favourites only"
  wheel: "Segments in order; label in Night or Cream, whichever contrasts (onSegment)"
---

## Overview

A violet night with a sunset wheel on it: the six segment colours are the only rainbow in the app,
everything else is calm violet with **sun yellow for actions and favourites**. Built on the shared
recipe (seed `#6A3DE8`, Vibrant).

## Rules

- The segment palette is fixed and ordered so neighbours contrast; never reuse it for UI.
- Sun yellow = act (New wheel, Spin) or favourite (star, card edge).
- On short screens "New wheel" sits in the title row (a floating button covered the last star).
