# Capability: maths

The shared maths every engine game builds its choices from: a gate that changes a crowd,
and a puzzle that writes a number as something to work out.

- **Source:** `engine/core/addons/lillo_core/maths/` (`LilloGate`, `LilloMathPuzzle`)
- **Verified by:** `engine/core/tests/` (`test_gate`, `test_math_puzzle`),
  `engine/summandos/tests/test_core_link.gd`

---

### Requirement: A gate changes a crowd as its label says

A gate SHALL add, subtract, multiply, divide (whole numbers) or take a percentage; the crowd
SHALL never go below zero, and dividing by zero SHALL leave it unchanged. Which of two gates is
better SHALL depend on the crowd, with a tie when both give the same. A label SHALL read like
the gate on screen (`×2`, `−15`, `50 %`), and a gate showing a puzzle SHALL bracket it when it
has more than one term.

#### Scenario: gates
- **WHEN** 10 pass `+20`, `−4`, `×3`, `÷4` **THEN** they become 30, 6, 30, 2; and 9 through `50 %` become 4
- **WHEN** 10 pass `−15` or `÷0` **THEN** they become 0, or stay 10
- **WHEN** `×3` and `+30` are compared for 20, 10 and 15 **THEN** the first, the second, then neither is better
- **WHEN** `×7` shows `√49`, `3+4` or `√100÷2` **THEN** it reads `×√49`, `×(3+4)`, `×(√100÷2)`
- *Verifies:* `test_each_operation_changes_the_crowd`, `test_the_crowd_never_goes_below_zero`,
  `test_dividing_by_zero_leaves_the_crowd_as_it_was`, `test_the_better_gate_depends_on_the_crowd`,
  `test_labels_read_like_the_gates_on_screen`, `test_a_gate_shows_its_puzzle`,
  `test_the_shared_gate_is_available`

### Requirement: Every puzzle equals its value

A puzzle SHALL always evaluate to the number it stands for, at every tier: tier 0 the plain
number, tier 1 one operation, tier 2 powers, roots and equations, tier 3 two-step expressions. Tiers 2
and 3 SHALL be marked hard, which is what earns a thinking pause.

A wrong puzzle would teach wrong maths; this is the one thing the game cannot get wrong.

#### Scenario: puzzles
- **WHEN** 25 is written at tier 0 **THEN** it reads `25`
- **WHEN** every value from 2 to 60 is written at tiers 1 and 2, and 2 to 120 at tier 3 **THEN** each evaluates to its value
- **WHEN** 32 is written at tiers 1 and 2 **THEN** only tier 2 is hard; 32 and 49 are found as `2⁵` and `7²`, and 30 as no power
- *Verifies:* `test_tier_zero_is_the_plain_number`, `test_every_puzzle_equals_its_value`,
  `test_two_step_puzzles_equal_their_value`, `test_adult_puzzles_are_worth_a_pause_and_easier_ones_are_not`,
  `test_powers_are_found`
