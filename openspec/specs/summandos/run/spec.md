# Capability: run

One run of a level, without drawing: the squad runs forward, the player steers it left or
right through gates, it shoots the waves coming down the lanes, and the run ends won at the
finish or lost when no soldier is left. Stepped at a fixed rate, so it is deterministic.

- **Source:** `engine/summandos/game/run.gd` (`SummandosRun`); pausing: `main.gd`, `view/ui.gd`
- **Verified by:** `engine/summandos/tests/` (`test_run`, `test_levels`); pausing:
  `engine/summandos/tools/smoke_test.sh` on the emulator

---

### Requirement: The side the squad is on picks the gate

Passing a row SHALL apply the gate on the squad's side to the squad; a weapon crate SHALL
multiply the squad's damage and apply no gate. The squad SHALL never leave the track, however far
it is steered.

#### Scenario: gates, crates and the track edge
- **WHEN** 10 soldiers pass `+10 | ×3` on the left **THEN** there are 20, and on the right 30
- **WHEN** 10 soldiers pass `weapon ×2 | +30` on the left **THEN** they are still 10, with damage 2
- **WHEN** the squad is steered to 99 **THEN** it stands at the track's edge
- *Verifies:* `test_the_side_the_squad_is_on_picks_the_gate`,
  `test_taking_a_weapon_skips_the_gate_next_to_it`, `test_the_squad_never_leaves_the_track`

### Requirement: Waves are fought by aiming at their lane

Shots SHALL hit a wave only while the squad stands in front of its lane. A wave in range
SHALL wake and edge towards the squad. A big enough squad SHALL shoot a wave down before it
arrives; a wave that reaches the squad SHALL trade soldiers for enemies and can overrun it.
A stronger weapon SHALL kill sooner.

Steering has two jobs, gates and aim; that tension is the game.

#### Scenario: aiming, waking, winning and losing
- **WHEN** a wave comes down lane 3.5 **THEN** a squad at 3.5 aims fully and kills, and one at −3.5 aims at nothing and kills nothing
- **WHEN** a wave at lane 3 is awake and the squad is at −3 **THEN** within a second the wave has moved towards it
- **WHEN** 50 soldiers meet a wave of 20 single-hit enemies **THEN** the run is won with all 50 and 20 kills
- **WHEN** 3 soldiers meet 200 tough enemies **THEN** the run is lost with none left
- **WHEN** a squad with damage 3 and one with damage 1 fire at the same tough target for 2 s **THEN** the armed one kills it first
- *Verifies:* `test_shots_only_land_when_aimed_at_the_wave`,
  `test_an_awake_wave_edges_towards_the_squad`, `test_a_big_squad_shoots_a_wave_down_before_it_arrives`,
  `test_a_small_squad_is_overrun`, `test_a_stronger_weapon_kills_faster`

### Requirement: Thinking pauses stop the run until the player is ready

A row marked to think SHALL stop the run before it for the level's think time (5 s), with
nothing moving, and then go on by itself; the player SHALL be able to end the pause early.
Either way the row is then taken as usual.

#### Scenario: a think row
- **WHEN** the squad reaches a think row **THEN** the run is thinking, its position does not change for 4.9 s, and by 5.2 s it moves again
- **WHEN** the player is done thinking early **THEN** the run moves on and takes the row
- *Verifies:* `test_a_think_row_stops_the_run_for_five_seconds`

### Requirement: The first level teaches by being winnable

The hand-made first level SHALL be won by sensible choices.

#### Scenario: first level
- **WHEN** the first level is played taking the sensible side at each row **THEN** it is won
- *Verifies:* `test_the_first_level_can_be_won_by_good_choices`

### Requirement: A run can be paused

A run in progress SHALL stop where it stands, the squad, the waves, the boss and the thinking
countdown, when the player taps the pause button, presses back, or leaves the game (a call, the
home button, the notification shade), and show a card to carry on, start the level again or go
back to the level map. Back on that card SHALL carry on. A run that is over SHALL not pause.

#### Scenario: pausing on the phone
- **WHEN** the pause button is tapped during level 1 on the emulator **THEN** two screens taken 3 s apart are the same, and back carries on to the result card

> **GAP — no GUT test.** Pausing stops the whole scene tree, GUT's runner with it; the
> emulator smoke test (`engine/summandos/tools/smoke_test.sh`) checks it instead.
