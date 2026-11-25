# Capability: levels

Every level is generated from its mode and number: rows of two choices (maths gates or a
weapon crate), enemy waves down the lanes and, every fifth level, a boss. Four modes:
Recruit, Soldier and Captain with 30 levels each, Legend with 10; and the endless run, opened
by winning every mode's last level.

- **Source:** `engine/summandos/game/level.gd` (`SummandosLevel`), gates and puzzles from
  `engine/core/addons/lillo_core/maths/`
- **Verified by:** `engine/summandos/tests/` (`test_levels`, `test_levels_ahead`, `test_gate_text`, `test_endless`)

---

### Requirement: A level is the same every time and grows with its number

Generating the same mode and number SHALL give the same rows, labels, pauses and reference (the
soldiers that best play keeps). A later level SHALL have more rows than an earlier one.

Players retry a level to beat their stars; a level that changed between tries would make
the stars meaningless.

#### Scenario: same level, longer later
- **WHEN** Soldier level 7 is generated twice **THEN** both have the same rows, gate labels on both sides, pauses and reference
- **WHEN** Recruit level 1 and level 30 are compared **THEN** level 30 has more rows
- **WHEN** a level is made ahead on a worker thread and then taken twice **THEN** it matches the generated one and the second take is the same level
- *Verifies:* `test_a_level_is_the_same_every_time`, `test_later_levels_are_longer`,
  `test_a_level_made_ahead_is_the_generated_one_and_is_kept`

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

Every level SHALL be won, with three stars, by taking the better side at every row, and that
play SHALL never leave the squad below its floor: the starting squad in Recruit; in the other
modes the starting squad or what the gates alone bring it down to, and never under 3. A level
whose gates cannot keep it SHALL draw other gates. The waves SHALL be sized so that even the best
play loses part of the squad (a larger share in harder modes), so the choices matter all the way.

Playtesting found a Recruit level that good maths could not win (the squad fell to one soldier
between waves) and one that best play lost outright.

#### Scenario: winnable but not free
- **WHEN** every level of every mode is played with the best sides **THEN** it is won with three stars and the squad never falls below its floor (in Recruit, below the soldiers it started with)
- **WHEN** Recruit and Captain level 10 are played with the best sides **THEN** they are won with fewer soldiers than the same gates without waves leave, and Captain keeps the smaller share
- *Verifies:* `test_every_level_can_be_won_with_the_best_choices`,
  `test_the_best_choices_lose_a_share_of_the_squad_and_more_in_harder_modes`

### Requirement: One wrong gate is forgiven in the easy modes, not in the hard ones

With one row's gate the wrong one and best play everywhere else, at least 75 % of those runs
SHALL still win in Recruit, 50 % in Soldier and 25 % in Captain; a level short of that SHALL ease
its waves and boss until it gets there. In Legend a wrong gate loses at once.

Playtesting found one slip lost the level in every mode, Recruit included.

#### Scenario: forgiveness by mode
- **WHEN** every Recruit, Soldier and Captain level is played with one wrong gate at each row in turn **THEN** each level wins at least its mode's share, and Recruit forgives more than Soldier, Soldier more than Captain
- *Verifies:* `test_one_wrong_gate_is_forgiven_more_in_easier_modes`

### Requirement: Every gate text fits its gate

Every text a generated level puts on a gate (the operation, the equation under it, a weapon's
name) SHALL fit inside its half of the track at a readable size, in Spanish and English; a long
equation SHALL shrink and a short label SHALL stay at full size.

#### Scenario: texts fit
- **WHEN** every gate of every level is measured in both languages **THEN** none is wider than its gate
- **WHEN** `+x  3x+4=x+18` and `+5` are fitted **THEN** the equation gets a smaller size and `+5` keeps the largest
- *Verifies:* `test_every_gate_text_fits_its_gate`,
  `test_a_long_equation_shrinks_and_a_short_one_stays_big`

### Requirement: The endless run opens after every rank and goes on until a level is lost

Once the last level of Recruit, Soldier, Captain and Legend has been won, or the menu's
crossbowman has been tapped seven times in a row (a hidden shortcut, like Android's developer
options), the menu SHALL offer Endless with the record underneath. It SHALL play generated levels one after another, each the
same every time, with Captain's gates and waves, enemies and a boss every fifth level that grow
past Captain's last level; every one SHALL be winnable with the best choices. A won level SHALL
lead to the next; the first lost one SHALL end the run, and the levels cleared SHALL be kept as
the record when they beat it. Each next level SHALL be generated while the current one is played.

#### Scenario: opening and the record
- **WHEN** the last level of each rank is won in turn **THEN** Endless opens only after the fourth; a run of 7 sets the record and a later run of 4 keeps it
- *Verifies:* `test_endless_opens_once_every_rank_is_finished_and_keeps_the_record`

#### Scenario: the secret way in
- **WHEN** the menu's crossbowman is tapped seven times in quick succession (under 1.5 s apart) **THEN** the last three taps say how many are left, the seventh opens Endless without finishing the ranks and says so; once open, seven taps say it already is
- **WHEN** a pause longer than 1.5 s breaks the taps **THEN** the count starts again
- *Verifies:* `test_the_secret_taps_open_endless_without_finishing_the_ranks`,
  `test_seven_quick_taps_unlock_and_count_down_on_the_way`, `test_a_pause_starts_the_count_again`

#### Scenario: winnable, harder and repeatable
- **WHEN** endless levels 1 to 60 are played with the best choices **THEN** each is won and the squad never falls below its floor
- **WHEN** levels 30 to 34 are compared with levels 1 to 5 **THEN** the best choices keep a smaller share of their soldiers; level 12 is the same twice and level 10 has a boss
- **WHEN** an endless level is made ahead **THEN** it is the one generated on the spot
- *Verifies:* `test_every_endless_level_can_be_won_with_the_best_choices`,
  `test_endless_levels_get_harder_and_are_the_same_every_time`, `test_endless_levels_are_made_ahead`
