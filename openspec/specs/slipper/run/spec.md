# Capability: run

A kid runs through an endless house chased by a grandma throwing slippers: tap to
jump (hold for higher), eat croquetas, land on furniture, and keep the gap to her open.
The run is a deterministic simulation stepped at a fixed rate.

- **Source:** `apps/slipper/domain/` (`Engine`, `World`, `House`, `GameConfig`)
- **Verified by:** `apps/slipper/domain/src/test/` (`EngineTest`, `FeaturesTest`,
  `BalanceTest`), `apps/slipper/app/src/test/` (`SlipperFlowTest`)

---

### Requirement: The kid runs, speeds up and jumps as the finger asks

The kid SHALL run at `RUN_SPEED` or more, speeding up with distance up to
`MAX_RUN_SPEED`. A tap SHALL jump and the kid SHALL land again; holding for 0.3 s SHALL
jump at least 1.3 times as high as not holding; a tap just before landing SHALL be
buffered (`JUMP_BUFFER`) and jump on landing. A high jump SHALL land on the table, standing
at the table's height without bumping it, and walking off the table SHALL fall back to
the floor.

Input in a game this fast is never frame-perfect; the buffered tap is what makes a miss
feel like the player's fault and not the phone's.

#### Scenario: movement
- **WHEN** the run lasts 10 s on bare floor **THEN** the distance exceeds `RUN_SPEED` × 10, the speed at 20 s is above the speed at 0, and at distance 1,000 it equals `MAX_RUN_SPEED`
- **WHEN** the player taps **THEN** the kid is in the air, and standing again within 1.5 s
- **WHEN** the player holds for 0.3 s **THEN** the peak is more than 1.3 times that of a tap alone
- **WHEN** a tap comes while the kid is falling, almost down **THEN** the kid jumps on landing and is above 2.0 within 0.2 s
- **WHEN** a held jump meets a table **THEN** the kid ends standing at the table's height and the table is not bumped
- **WHEN** the kid stands on a table that ends **THEN** the kid falls to height 0
- *Verifies:* `theKidRunsAndSpeedsUp`, `aTapJumpsAndTheKidLandsAgain`,
  `holdingJumpsHigher`, `aTapJustBeforeLandingStillJumps`,
  `aHighJumpLandsOnTheTableAndRunsAcrossIt`, `walkingOffTheTableFallsBackToTheFloor`

### Requirement: The chase gap decides the run

Running clean SHALL open the gap to the grandma up to `MAX_GAP`. A chair SHALL be bumped
and close the gap by more than 1.5 below `START_GAP`, after which the gap grows again; a
low slipper that reaches a kid on the floor SHALL be consumed and cost more than
`HIT_LOSS`, and a high one SHALL pass over a kid on the floor with no loss. She SHALL catch
the kid when the gap closes (a caught run is `over` and stays unchanged by further input).
The more croquetas eaten, the harder she runs: with 20 eaten the gap ends below the gap
of a calm run.

#### Scenario: gap, hits and catching
- **WHEN** a run starts one below `MAX_GAP` **THEN** after 10 s the gap equals `MAX_GAP`
- **WHEN** the kid runs into a chair **THEN** it is bumped, the gap is below `START_GAP` − 1.5, and a second later it is larger
- **WHEN** a low slipper reaches a kid on the floor **THEN** it is gone and the gap is below `START_GAP` − `HIT_LOSS`
- **WHEN** a high slipper passes a kid on the floor **THEN** the gap is not below `START_GAP`
- **WHEN** the gap is 4 and the kid meets a chair **THEN** she catches him, the run is over, and a tap changes nothing
- **WHEN** 20 croquetas have been eaten over 5 s from the same gap **THEN** the gap is smaller than without them
- *Verifies:* `runningCleanOpensTheGapUpToTheMax`,
  `runningIntoAChairStumblesOnceAndSheClosesIn`, `aLowSlipperHitsAKidOnTheFloor`,
  `aHighSlipperFliesOverAKidOnTheFloor`, `sheCatchesYouWhenTheGapCloses`,
  `angerMakesHerRunHarder`

#### Scenario: the extras
- **WHEN** a high slipper passes over a standing kid **THEN** there is one near miss worth `NEAR_MISS_POINTS` bonus and the gap is above `START_GAP` − 1
- **WHEN** a sandwich is picked up and two chairs follow **THEN** the first is blocked with no stumble and the shield is gone, and the second trips him
- **WHEN** skates are picked up before a chair **THEN** the chair is crashed through with no stumble and the gap ends above `START_GAP`
- **WHEN** the kid runs 2 s on skates and 2 s on foot from the same start **THEN** on skates he covers `SKATES_SPEED` (1.35) times the ground
- **WHEN** a cat is ahead **THEN** it runs away (`fled`) and the kid stumbles
- **WHEN** she throws from a full gap over 40 s **THEN** the kind warned is the kind thrown, every throw not close behind another is preceded by about a wind-up, and more than ten throws are made
- **WHEN** 90 s are played with a full gap **THEN** some throw follows the previous within `DOUBLE_GAP`
- *Verifies:* `aHighSlipperOverAStandingKidIsANearMissAndOpensTheGap`,
  `theSandwichBlocksOneHit`, `skatesAreFasterAndCrashThroughFurniture`, `onSkatesHeCoversMoreGround`,
  `theCatWalksTowardsTheKidAndTripsHim`,
  `sheWindsUpBeforeEachThrowAndTheNextKindIsKnown`, `laterSomeThrowsComeInPairs`

### Requirement: Croquetas score and chain into a combo

Eating a croqueta SHALL count it and add `POINTS_PER_CROQUETA`; the score SHALL be
`distance × POINTS_PER_UNIT` plus the bonus. Croquetas eaten in a row SHALL build a combo
worth 10, 20, 30 and the combo SHALL reset after a pause longer than `COMBO_WINDOW`.
Eating SHALL never change the number or timing of the next throws.

A throw that shifted because the player ate would make a seeded run unrepeatable.

#### Scenario: scoring
- **WHEN** one croqueta is eaten **THEN** one is counted and the score is distance × `POINTS_PER_UNIT` plus `POINTS_PER_CROQUETA`
- **WHEN** three are eaten in quick succession **THEN** the combo is 3, the bonus 10 + 20 + 30 and the events carry 10, 20 and 30
- **WHEN** two are eaten further apart than the combo window **THEN** both count and the bonus is 20
- **WHEN** the distance is 100 and the bonus 25 **THEN** the score is 100 × `POINTS_PER_UNIT` plus 25
- **WHEN** runs with and without 10 croquetas eaten run 3 s **THEN** both made the same throws and have the same next throw time
- *Verifies:* `croquetasAreEatenAndScore`, `croquetasInARowBuildACombo`,
  `theComboResetsAfterAPause`, `scoreIsDistanceAndBonus`,
  `eatingCroquetasNeverMovesTheNextThrow`

### Requirement: The house is built ahead from a seed, the same every time

The house SHALL be built in chunks ahead of the kid (`BUILD_AHEAD`) and forgotten behind
(`KEEP_BEHIND`), the chunks contiguous and numbered in order. The same seed and inputs
SHALL give the same run and another seed a different house. No furniture SHALL stand in
the first `WARM_UP_CHUNKS` chunks, and at the start the house SHALL already reach 20 units
behind the kid. Rooms SHALL follow in the order kitchen, hall, living room and then
kitchen again every `CHUNKS_PER_ROOM`. Before `HIGH_FROM` seconds only low slippers SHALL
be thrown. Over 200 seeds a decent bot SHALL last a median of 20 to 60 s, a careless one
under 20 s, almost every run ending, and skill SHALL matter (a 1.6 times longer median).

#### Scenario: determinism and difficulty
- **WHEN** the same seed and a fixed tapping pattern play 20 s twice **THEN** the runs are equal, and seed 43 builds a different house from seed 42
- **WHEN** 20 s are played with jumps held **THEN** the last chunk ends past the distance plus `BUILD_AHEAD`, the first past the distance minus `KEEP_BEHIND`, and chunks are contiguous
- **WHEN** a run starts **THEN** no furniture is before the end of the warm-up chunks and the first chunk starts before −20
- **WHEN** chunk indexes 0, `CHUNKS_PER_ROOM`, 2× and 3× are asked **THEN** the rooms are kitchen, hall, living room, kitchen
- **WHEN** a throw is due before `HIGH_FROM` **THEN** every slipper in the air is low
- **WHEN** 200 seeded runs are simulated by a decent and a careless bot **THEN** the medians, the end and the 1.6 ratio above hold
- *Verifies:* `theSameSeedAndInputsGiveTheSameRun`, `theHouseIsBuiltAheadAndForgottenBehind`,
  `theFirstChunksAreBareFloor`, `theStartIsNeverEmpty`, `roomsFollowEachOther`,
  `noHighSlippersInTheFirstSeconds`, `runsLastLongEnoughToBeFunAndAlwaysEnd`

### Requirement: A run can be played from start to being caught

#### Scenario: the flow
- **WHEN** a run is played in the app until the final score shows **THEN** one run and a best score above zero are stored; retrying starts a new one, and after three runs a best for today exists
- *Verifies:* `aRunEndsCaughtAndTheNextOneStarts`
