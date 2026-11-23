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

**Squad colour** (menu → Squad colour): the palette's own, sky `#38BDF8`, orange `#FF8A1F`, lime
`#A3E635` or white `#F2F2F7`. Each keeps a colour difference of ΔE ≥ 19 from both palettes'
enemies, boss and track with normal vision and simulated protanopia, deuteranopia and tritanopia
(`tests/test_palette.gd`, `LilloColours`). Rejected for failing it: blue, red, pink, yellow, navy,
black. Models are repainted with `LilloMaterials.team`, which keeps the texture's shading and
takes the hue from the squad colour.

## Type

Luckiest Guy for titles, buttons and numbers, white with a dark outline; numbers over the squad in
gold on a badge. Fredoka at a medium weight for text meant to be read (the About card's names and
licences), since Luckiest Guy is capitals only.

## Characters

- In the run: KayKit's CC0 hooded crossbowmen as the squad (washed teal), running between waves
  and aiming and shooting bolts while a wave is in range; skeletons as the enemies (washed
  purple). Bosses every fifth level take turns as six skeletons (warrior, mage, Golem,
  rogue, Necromancer, minion) in six boss colours, each boss bigger than the last and the Golem a
  third bigger again; the mage and the Necromancer throw fireballs. The Golem and Necromancer
  come from a bought pack (KayKit Skeletons EXTRA); a build without it draws the warrior and the
  mage in their place, with the same fight (`SummandosModels.STAND_INS`). No two bosses in a mode share both
  model and colour (`SummandosLevel.boss_look`). Boss colours keep ΔE ≥ 19 from every squad
  colour and both tracks for every kind of colour vision (tests). Crowds step through four baked poses per clip;
  the leader and the boss play the full animation. Without the models the game falls back to its
  own toon figures (`LilloFigure`) with a horned monster boss.
- Credits for every third-party piece are on the About card (menu → About).
- In the menus and the level map: the crossbowman cheering bottom-left in the player's squad
  colour, and the next boss of the chosen mode bottom-right in its combat stance
  (`LilloModelStage`); without the models, the drawn soldier and monster (`LilloSticker`).

## Screens

- **Menu**: the title breathing and the four rank buttons (Recruit, Soldier, Captain, Legend),
  each with its operations in small type underneath, nothing else. Two quiet round icons
  top-right: a "?" with the How to play card, a speaker that mutes or unmutes everything, and a gear that
  opens **Settings** (sound, music, vibration, night mode, squad colour, About). The performance
  test is only a command-line option (`--mode=stress`).
- **About**: the name and version, one block per credit (side by side when sideways) (what, who, licence and a tappable link),
  and the no-ads line. The bought pack (KayKit Skeletons EXTRA) gets its own line when its models
  are in the build.
- **Level map**: rays, clouds and hills; tiles of 3 columns upright and 5 sideways, stars in a
  tab, padlocks on locked levels; arrows, swipe and page dots; back bottom-left.
- **Run**: the chase camera; gates are translucent panels with their half of the track painted,
  teal when the gate keeps or grows the squad and purple when it shrinks it, except in grown-up
  mode, where every gate is the same gold so the colour gives nothing away; the gate the squad is
  heading through lights up and grows as it nears, the other dims; waves come down a lane and
  bolts fly to it, so the squad has to stand in front of a wave to hurt it;
  numbers rise when a gate is taken; "Think!" counts down in the top-left corner, clear of the gates; the mode and level sit above
  the counts, and a boss's health bar under them while it is near. The mage boss throws fireballs at where the squad
  stands, over a red ring on the track; moving out of the ring dodges them. Weapons go by name and
  model (Kenney Blaster Kit): a pair of pistols ×2, a bazooka ×3, turning on their crate, with
  thicker bolts; once taken, the weapon turns in a badge top-right with its name.
- **Result**: the mode and level, then a dark card with stars popping in and confetti on a win.

## Expert

Ten levels for the very keen: every gate is a two-step sum, every gate pauses for 5 seconds, gates
are all gold, and taking the worse gate ends the level on the spot with "Wrong gate! ×(√100÷2)
left 45 soldiers and +(2³+9) left 26." Random choices never win one (balance report).

## Sound

Every effect is synthesised by `tools/generate_sounds.py` (nothing sampled): a pop on every button,
a rising arpeggio through a gate that adds (higher for a bigger gain) and a falling one through a
gate that takes, soft crossbow ticks while shooting, bone crunches on hits (throttled, never more
than one every 60 ms), a ticking countdown while thinking (sharper in the last three seconds), a
low boom and a buzz when the boss wakes, a fanfare or a fall at the end, a ding per star. Two
chiptune loops, calm for the menus and faster for the run, stand in until CC0 music is chosen.
Sound, music and vibration each have a switch in Settings, saved, and the corner speaker mutes
both sound and music at once; music pauses with the app.

## Icon

A teal squad in front of a calculator on the sunset sky (`tools/generate_icon.py`); checked at
48–144 px on light and dark wallpapers, round mask and monochrome before every release.
