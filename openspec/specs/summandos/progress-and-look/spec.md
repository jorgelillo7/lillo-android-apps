# Capability: progress-and-look

What the player keeps between runs (stars per level, which levels are open) and the colours
everybody must be able to tell apart: their squad against the enemies, bosses and track.

- **Source:** `engine/summandos/game/progress.gd` (`SummandosProgress`),
  `engine/summandos/game/run.gd` (`stars`), `engine/summandos/game/palette.gd` (`SummandosPalette`)
- **Verified by:** `engine/summandos/tests/` (`test_levels`, `test_palette`)

---

### Requirement: Stars keep the best result and open the next level

A win SHALL earn 1 to 3 stars by the soldiers left against the level's reference (3 at 75 %,
2 at 40 %). The saved stars SHALL be the best ever earned, and winning a level SHALL open the
next one, and only that one.

#### Scenario: stars for a win
- **WHEN** a won run keeps 75, 74, 40 or 39 of a reference of 100 **THEN** it earns 3, 2, 2 or 1 stars, and a lost run earns none
- *Verifies:* `test_stars_follow_the_share_of_the_reference`

#### Scenario: best stars, next level
- **WHEN** Captain level 1 is won with 2 stars and then with 1 **THEN** it keeps 2, level 2 is open and level 3 is not
- *Verifies:* `test_progress_keeps_the_best_stars_and_opens_the_next_level`

### Requirement: The squad is told apart by everyone

Every squad colour the player can pick SHALL stay at least `MIN_DIFFERENCE` (ΔE) from the
enemies, every boss colour and the track, in both themes and under simulated protanopia,
deuteranopia and tritanopia. Picking a squad colour SHALL change only the squad, and an
unknown choice SHALL fall back to the default.

A shooter where you cannot see your own squad against the horde is unplayable; colour
blindness is common enough to design for.

#### Scenario: colours apart
- **WHEN** every squad colour is compared in every theme with enemies, boss and track **THEN** the worst ΔE over every kind of vision is above the minimum
- **WHEN** every boss colour is compared with every squad and track **THEN** the worst ΔE is above the minimum
- **WHEN** orange is picked, or a colour that does not exist **THEN** only the squad changes, or nothing does
- *Verifies:* `test_every_squad_colour_stays_apart_from_enemies_and_track_for_everyone`,
  `test_boss_colours_stay_apart_from_every_squad_and_track`,
  `test_picking_a_squad_colour_changes_only_the_squad`
