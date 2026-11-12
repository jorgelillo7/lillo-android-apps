# Capability: wheel

A wheel of options the user spins to decide, where each option's slice of the circle is
its real chance of winning, recently accepted results are less likely to come up again,
and the user's wheels, history and settings persist.

- **Source:** `apps/decision-wheel/domain/` (`DecisionEngine`, `Spin`, `Model`,
  `ClickSynth`), the persistence and screens in `apps/decision-wheel/app/`
- **Verified by:** `apps/decision-wheel/domain/src/test/`,
  `apps/decision-wheel/app/src/test/` (the `WheelFlowTest` end-to-end flow, the
  `WheelsViewModelTest`)

---

### Requirement: A slice's size is its chance, and the landing is uniform

Each option still in play SHALL get a slice whose share of the circle equals its weight
over the total, laid out in the user's order. A spin SHALL end at a uniformly random angle
after several full turns (`Spin.plan`), so each option wins with its share; a harder fling
SHALL add time. The option under the fixed top pointer after a turn SHALL be the winner.

The wheel is only honest if what is drawn is what is drawn from; a landing that favoured
some angles would make the picture lie.

#### Scenario: shares and landing
- **WHEN** options weighted 1, 0.25 and 0.75 are laid out **THEN** their slices sweep 0.5, 0.125 and 0.375 of the circle, in that order
- **WHEN** four equal slices are turned to 0°, 100°, 190° and 10° **THEN** the pointer reads the first, third, second and fourth, and a rotation of whole extra turns reads the same slice
- **WHEN** 20,000 seeded spins land on a wheel of weights 1 and 0.25 **THEN** the small slice wins between 18 % and 22 % of them
- **WHEN** a soft and a hard fling are planned from the same angle **THEN** the soft one still turns more than four full turns and the hard one lasts longer
- *Verifies:* `segmentsShareTheCircleByWeight`, `pointerReadsTheSegmentUnderTheTop`,
  `uniformLandingMatchesTheWeights`,
  `spinsTurnSeveralTimesForwardAndLongerWhenFlungHarder`

### Requirement: Recently accepted results are less likely, for a while

With "avoid repeats" on, the most recent distinct accepted results of the wheel (newest
first) SHALL get the chance multipliers `DecisionEngine.RECENT_PENALTIES` = 0.25, 0.5 and
0.75, so at most three options are penalised; options never accepted keep full weight.
History older than `RECENT_WINDOW_MILLIS` (30 days) SHALL NOT penalise, and with the
setting off every option SHALL weigh the same. Vetoed options SHALL be left out and the
order kept. Only accepted results count as history.

Without the penalty a wheel that keeps landing on the same dinner stops being a decision
aid; the cap and the window stop an old result from haunting an option forever.

#### Scenario: weights
- **WHEN** there is no history **THEN** every option has the same weight
- **WHEN** three results were accepted, one 1 day ago, one 3 and one 6 days ago **THEN** the newest weighs 0.25, the next 0.5, the third 0.75 and an unused option 1.0
- **WHEN** the only result is 40 days old, or the setting is off **THEN** nothing is penalised
- **WHEN** an option is vetoed **THEN** it is out and the others keep their order
- *Verifies:* `everyOptionHasTheSameWeightWithoutHistory`,
  `recentResultsGetLessChanceNewestFirst`, `oldHistoryAndDisabledSettingDoNotPenalise`,
  `vetoedOptionsAreOutAndOrderIsKept`

> **GAP — unverified.** No test records more than three distinct results to show the cap,
> or shows that only accepted results (not spins) enter the history. A test would feed
> four distinct accepted results and assert the oldest keeps weight 1.0.

### Requirement: The wheel reads well and clicks as it turns

Neighbouring slices, including the last and the first, SHALL never share a colour, cycling
a palette (`colorSlots` requires at least three colours). The number of boundaries crossed
between two rotations SHALL drive a click per boundary, and the click sound SHALL be a
short WAV.

#### Scenario: colours and click
- **WHEN** 1 to 20 slices are coloured from a palette of six **THEN** no two neighbours match, the last and first included
- **WHEN** four equal slices turn from 10° to 80°, 80° to 100° and 0.5° to 720.5° **THEN** 0, 1 and 8 boundaries are crossed
- **WHEN** the click is rendered **THEN** it has 500 to 1,500 samples and its WAV starts with `RIFF` and is 44 bytes plus two per sample
- *Verifies:* `neighboursNeverShareAColour`, `countsBoundariesForTheClick`,
  `clickIsShortAndProducesAValidWav`

### Requirement: Favourites lead, shortcuts follow use, presets can be restored

Wheels SHALL list favourites first, each group in the user's order. Launcher shortcuts
SHALL be the favourites then the most recently used wheels, up to `AppState.MAX_SHORTCUTS`
= 3. Restoring presets SHALL bring back only the example wheels that were deleted, never
overwriting an edited one or the user's own.

#### Scenario: order, shortcuts and presets
- **WHEN** wheels c and b are starred in that order **THEN** the list is b, c, a, d, and unstarring b removes its star
- **WHEN** d is a favourite and d, c, a were used most recently in that order **THEN** the shortcuts are d, c, a; with no history and no favourite there are none
- **WHEN** presets are restored after one was deleted and another edited **THEN** the deleted one returns after the user's wheels and the edited one, with its new name and star, is kept
- *Verifies:* `favouritesComeFirstKeepingTheUsersOrder`, `shortcutsAreFavouritesThenRecent`,
  `restoringBringsBackOnlyDeletedPresets`,
  `restoringPresetsBringsBackOnlyTheMissingOnes`

### Requirement: State persists, history is capped, an old save still loads

The wheels, the history and the sound setting SHALL survive a JSON round trip; the history
SHALL be kept newest first and capped at `AppState.MAX_HISTORY` = 300 results, and a
state saved by version 1 (no favourites) SHALL still load with nothing starred. Saving
SHALL add a new wheel or replace the one with the same id in its place; deleting a wheel
SHALL drop its history and no one else's; accepting SHALL record the decision newest first.

#### Scenario: persistence
- **WHEN** a state with a wheel, a decision and the sound off is encoded and decoded **THEN** it is equal
- **WHEN** 310 decisions are added **THEN** 300 remain and the last one added is first
- **WHEN** a JSON saved without favourites is loaded **THEN** the wheel is not a favourite
- **WHEN** a new wheel is saved, then an existing one with the same id **THEN** the first is appended and the second replaces the original in its place
- **WHEN** a wheel with decisions is deleted **THEN** only its decisions go
- **WHEN** two results are accepted **THEN** the later is first; clearing a wheel's history empties it
- **WHEN** avoid-repeats and sound are turned off and a wheel is starred **THEN** the stored state shows them and lists the starred wheel first
- *Verifies:* `stateSurvivesAJsonRoundTrip`, `historyIsNewestFirstAndCapped`,
  `stateSavedByVersion1StillLoads`, `savingAddsANewWheelAndReplacesAnExistingOne`,
  `deletingAWheelDropsItsHistoryOnly`, `acceptingRecordsTheDecisionNewestFirst`,
  `settingsAndFavouritesAreSaved`

### Requirement: The home shows the last result when there is room

The home SHALL show the latest decision whose wheel still exists in a card under the wheels,
with that wheel's name and day and a "Spin again" button that opens the wheel; with no decision
yet, the card SHALL offer a first spin on the first favourite, else the first wheel. The card
SHALL show only while the list is unscrolled and leaves at least 240dp free under the wheels,
and never on short screens. The last decision SHALL NOT also be repeated under the title.

#### Scenario: the last result card
- **WHEN** the history holds a decision for a deleted wheel and older ones **THEN** the last result is the newest decision whose wheel exists; with no history the first spin is offered on the favourite
- **WHEN** the home has two wheels and decisions, or none, in either look **THEN** the card shows the last result, or the first-spin invitation
- **WHEN** ten wheels fill the screen **THEN** no card shows
- **WHEN** a result was just accepted and the user goes back home and taps "Spin again" **THEN** that wheel opens
- *Verifies:* `theHomeOffersTheLastResultElseAFirstSpinOnTheFavourite`, `homeFirstSpin`,
  `homeFirstSpinLight`, `theLastResultNeedsRoom`, `spinAcceptAndCreateAWheel`

### Requirement: Spinning, accepting and creating works end to end

A user SHALL be able to spin a wheel, accept the result and create a wheel in one session.

#### Scenario: the flow
- **WHEN** the user opens the eat-out preset, spins and accepts
- **THEN** one decision is stored for that wheel with one of its options, and a new wheel "Smoke" with options One, Two and Three is created, saved with those options, and opens ready to spin
- *Verifies:* `spinAcceptAndCreateAWheel`
