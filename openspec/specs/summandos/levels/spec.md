# Capability: levels

Every level is generated from its mode and number: rows of two choices (maths gates or a
weapon crate), enemy waves down the lanes and, every fifth level, a boss. Four modes:
Recruit, Soldier and Captain with 30 levels each, Legend with 10.

- **Source:** `engine/summandos/game/level.gd` (`SummandosLevel`), gates and puzzles from
  `engine/core/addons/lillo_core/maths/`
- **Verified by:** `engine/summandos/tests/` (`test_levels`, `test_gate_text`)

---

### Requirement: A level is the same every time and grows with its number

Generating the same mode and number SHALL give the same rows, labels, pauses and reference (the
soldiers that best play keeps). A later level SHALL have more rows than an earlier one.

Players retry a level to beat their stars; a level that changed between tries would make
the stars meaningless.

#### Scenario: same level, longer later
- **WHEN** Soldier level 7 is generated twice **THEN** both have the same rows, gate labels on both sides, pauses and reference
- **WHEN** Recruit level 1 and level 30 are compared **THEN** level 30 has more rows
- *Verifies:* `test_a_level_is_the_same_every_time`, `test_later_levels_are_longer`

### Requirement: Each mode asks for its own maths

Recruit gates SHALL show plain numbers and only +, −, ×2 and ÷2. Captain SHALL write some
operands as puzzles (powers, roots, equations) and stop the run to think before at least one
row of every level (one to three). Legend SHALL be 10 levels where every gate is a two-step sum
(no percentages, which never become puzzles), every row pauses for 5 seconds, there are no
weapon crates, and taking the worse gate SHALL lose the level at once.

The point of the game is the sum: harder modes must make the player work it out, and the
thinking pause is what makes working it out fair on a running track.

#### Scenario: plain, puzzles and Legend
- **WHEN** every Recruit level is generated **THEN** no gate shows a puzzle, and every gate is +, −, ×2 or ÷2
- **WHEN** Captain level 20 is generated **THEN** more than two gates show puzzles and one to three rows pause to think
- **WHEN** every Captain level is generated **THEN** each has at least one row that pauses to think
- **WHEN** Legend is counted and every level generated **THEN** there are 10 levels, strict, pausing 5 s at every row, with a two-step sum on both sides of every row
- **WHEN** the worse gate of Legend level 1 is taken **THEN** the run is lost at the first row, and the other gate would have left more soldiers
- *Verifies:* `test_easy_gates_are_plain_and_adult_ones_are_puzzles`,
  `test_expert_is_ten_levels_of_two_step_sums_with_a_pause_at_every_gate`,
  `test_in_expert_the_worse_gate_loses_at_once`

### Requirement: The best choices win, and still cost soldiers

Every level SHALL be won, with three stars, by taking the better side at every row. The waves
SHALL be sized so that even the best play loses part of the squad (a larger share in harder
modes), so the choices matter all the way.

#### Scenario: winnable but not free
- **WHEN** every third level of every mode is played with the best sides **THEN** it is won with three stars
- **WHEN** Recruit and Captain level 10 are played with the best sides **THEN** they are won with fewer soldiers than the same gates without waves leave, and Captain keeps the smaller share
- *Verifies:* `test_every_level_can_be_won_with_the_best_choices`,
  `test_the_best_choices_lose_a_share_of_the_squad_and_more_in_harder_modes`

### Requirement: Every gate text fits its gate

Every text a generated level puts on a gate (the operation, the equation under it, a weapon's
name) SHALL fit inside its half of the track at a readable size, in Spanish and English; a long
equation SHALL shrink and a short label SHALL stay at full size.

#### Scenario: texts fit
- **WHEN** every gate of every level is measured in both languages **THEN** none is wider than its gate
- **WHEN** `+x  3x+4=x+18` and `+5` are fitted **THEN** the equation gets a smaller size and `+5` keeps the largest
- *Verifies:* `test_every_gate_text_fits_its_gate`,
  `test_a_long_equation_shrinks_and_a_short_one_stays_big`
