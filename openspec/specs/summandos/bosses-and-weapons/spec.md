# Capability: bosses-and-weapons

A boss ends every fifth level, and weapon crates (pistols ×2, a bazooka ×3) multiply the
squad's damage. A boss is a real fight: it eats soldiers on contact instead of being traded
away, and the mage and the Necromancer throw fireballs the player can dodge.

- **Source:** `engine/summandos/game/run.gd`, `engine/summandos/game/level.gd`,
  `engine/summandos/view/models.gd` (`SummandosModels`)
- **Verified by:** `engine/summandos/tests/` (`test_run`, `test_levels`, `test_models`,
  `test_palette`)

---

### Requirement: A boss is a fight that lasts and that a weapon shortens

A boss SHALL NOT die on contact: it SHALL strike as it reaches the squad, eating its `slam` on
top of a second's `bite`, then strike every second while it touches the squad, and it SHALL
stand until its health is shot away; a squad that cannot shoot it down is eaten. A generated boss
SHALL be sized for the squad the best choices bring within range of it: it holds several seconds
of that squad's fire, no more than BOSS_MAX_SECONDS, and a weapon SHALL bring it down sooner.

Playtesting found bosses that vanished on contact, then bosses that only stood there while the
squad shot them; a boss the player does not notice is not a boss.

#### Scenario: biting and lasting
- **WHEN** a boss that bites 5 a second meets 30 soldiers for 3 s **THEN** it is still standing, 13 to 20 soldiers are left and its health bar is below full
- **WHEN** a boss with a slam of 10 and a bite of 5 reaches 30 soldiers it cannot be shot down by **THEN** its first blow leaves 15, and within 4 s the run is lost
- **WHEN** a squad with damage 2 and one with damage 1 fight the same boss for 3 s **THEN** the armed run is won and the other is still fighting
- **WHEN** Recruit level 5 is generated **THEN** its boss holds more than 3 s of the reference squad's fire
- *Verifies:* `test_a_boss_is_not_traded_away_on_contact_but_eats_soldiers`,
  `test_a_boss_strikes_as_it_arrives_and_can_eat_the_squad`,
  `test_a_weapon_brings_a_boss_down_sooner`, `test_a_boss_takes_seconds_of_fire`

### Requirement: Fireballs land where the squad stood

The mage and the Necromancer, and no other boss, SHALL throw fireballs while awake and out of
contact. A fireball SHALL aim at where the squad stands when thrown and burn soldiers only if
the squad is still within its ring when it lands.

#### Scenario: burning and dodging
- **WHEN** the squad stays put under a mage **THEN** one fireball lands on it and burns 4 soldiers
- **WHEN** the squad moves 3 m aside while a fireball flies **THEN** no soldier burns
- **WHEN** the six bosses of Recruit are generated **THEN** only the mage's (level 10) and the Necromancer's (level 25) waves are ranged and burn
- *Verifies:* `test_a_mage_fireball_burns_the_squad_where_it_stood_unless_it_moves`,
  `test_only_the_mage_and_the_necromancer_throw_fireballs`

### Requirement: Six bosses take turns, each bigger, none alike

Bosses SHALL take turns as warrior, mage, Golem, rogue, Necromancer and minion, each bigger
than the last in its mode, and no two bosses in a mode SHALL share both model and colour.
The Golem and the Necromancer come from a bought pack that is not in the repo; without it a
free stand-in (warrior, mage) SHALL be drawn and the fight SHALL be the same.

#### Scenario: looks and stand-ins
- **WHEN** every boss of every mode is looked up **THEN** no model and colour pair repeats, heights grow, and the level carries that look
- **WHEN** a boss model is missing on this machine **THEN** its stand-in is drawn (the Golem as a warrior, the Necromancer as a mage), and a stand-in is never itself a bought model
- *Verifies:* `test_no_two_bosses_in_a_mode_look_alike_and_each_is_bigger`,
  `test_a_missing_bought_model_falls_back_to_a_free_one`

### Requirement: Weapons show as what they are

A ×2 crate SHALL show a pair of pistols and a ×3 crate a bazooka, each scaled to the asked
length and standing on the crate.

#### Scenario: weapon models
- **WHEN** the ×2 and ×3 weapons are built at 1.5 m **THEN** the ×2 has two pistols, the ×3 one bazooka 1.5 m long, standing at height 0
- *Verifies:* `test_weapons_show_as_pistols_or_a_bazooka`
