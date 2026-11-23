# Summandos: design

Cartoon, warm and loud, in the genre's language (big numbers, a crowd you can count, a boss)
with our own art. Colours live in `game/palette.gd`; change them there, nowhere else.

## Palettes

| | Sunset (default) | Night (menu toggle) |
|---|---|---|
| Sky | `#FF8F6B` → `#FFD3A8` | `#070A1A` → `#26305E` |
| Squad | teal `#13A89E`, helmets `#0B5E58` | cyan `#22D3EE`, `#0E7490` |
| Enemies | purple `#7B3FD1` | neon pink `#FF3D8B` |
| Boss | `#9B2FD1`, belly `#F2A6E0` | `#E11D74`, belly `#FFB3D1` |
| Accent (buttons, numbers, stars) | gold `#FFD23F` | `#FFD60A` |
| Outline | `#3A1C0E` | `#000000` |

Squad against enemies is teal against purple: told apart with red–green colour blindness, and
unlike the red-and-blue of the games this genre is known for.

## Type

Luckiest Guy for everything on screen, white with a dark outline; numbers over the squad in gold
on a badge.

## Characters

- In the run: KayKit's CC0 hooded crossbowmen as the squad (washed teal), running between waves
  and aiming and shooting bolts while a wave is in range; skeletons as the enemies (washed
  purple), a giant skeleton warrior as the boss. Crowds step through four baked poses per clip;
  the leader and the boss play the full animation. Without the models the game falls back to its
  own toon figures (`LilloFigure`) with a horned monster boss.
- Credits for every third-party piece are on the About card (menu → About).
- In the menus: a waving soldier bottom-left and the swaying monster bottom-right
  (`LilloSticker`).

## Screens

- **Menu**: the title breathing, three mode buttons, night mode and the performance test.
- **Level map**: rays, clouds and hills; tiles of 3 columns upright and 5 sideways, stars in a
  tab, padlocks on locked levels; arrows, swipe and page dots; back bottom-left.
- **Run**: the chase camera; gates are translucent panels with their half of the track painted,
  teal when the gate keeps or grows the squad and purple when it shrinks it, except in grown-up
  mode, where every gate is the same gold so the colour gives nothing away; the gate the squad is
  heading through lights up and grows as it nears, the other dims; waves come down a lane and
  bolts fly to it, so the squad has to stand in front of a wave to hurt it;
  numbers rise when a gate is taken; "Think!" counts down before hard gates.
- **Result**: a dark card with stars popping in and confetti on a win.

## Icon

A teal squad in front of a calculator on the sunset sky (`tools/generate_icon.py`); checked at
48–144 px on light and dark wallpapers, round mask and monochrome before every release.
