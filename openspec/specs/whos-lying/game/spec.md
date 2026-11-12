# Capability: game

The impostor word game for 3 to 24 players on one phone: roles are dealt, the group
discusses and votes players out, the game ends when the civilians or the infiltrators
win, and a match of several games crowns whoever scored most.

- **Source:** `apps/whos-lying/domain/` (`Game`, `Settings`, the match scoring),
  `apps/whos-lying/app/` (`GameViewModel`)
- **Verified by:** `apps/whos-lying/domain/src/test/` (`GameTest`,
  `ExhaustiveGameTest`), `apps/whos-lying/app/src/test/` (`GameViewModelTest`,
  `GameFlowTest`)

---

### Requirement: Each mode deals the impostor what it promises

In **classic** the impostor SHALL get the decoy word and not know they are the impostor
(`knowsRole` false), while civilians share the real word. In **blind** the impostor SHALL
get no word and no hint, even with the category hint on, and knows it. In **drawing**
mode every impostor SHALL get no word, in every mode, knowing it, and a category hint
except in blind. **Drifter** mode SHALL add one drifter who never starts. The impostor
limits SHALL come from `Rules.maxImpostors` and `Rules.canStart`.

Classic only works if the impostor cannot tell from the screen, and blind only if nothing
leaks; the limits keep a game from being decided before it is played.

#### Scenario: what each role is dealt
- **WHEN** a classic game with one impostor is dealt **THEN** the impostor has the decoy and does not know their role, and every civilian has the real word
- **WHEN** a blind game with two impostors is dealt with the hint on, drawing or not **THEN** both have no word and no hint and know their role
- **WHEN** a drawing game is dealt in each mode **THEN** the impostor has no word and knows it, and has the category as a hint in every mode but blind
- **WHEN** a drifter game is dealt with 30 different seeds **THEN** there is exactly one drifter and the starter is never the drifter
- *Verifies:* `classicImpostorGetsTheDecoyAndDoesNotKnow`,
  `blindImpostorGetsNoWordAndNoHintEvenWithTheHintOn`,
  `drawingImpostorsGetNoWordInAnyMode`, `drifterModeAddsOneDrifterWhoNeverStarts`

#### Scenario: limits and chaos
- **WHEN** limits are asked **THEN** 3 players allow 1 impostor and 5 allow 2 in classic, 4 allow 1 in drifter, drifter cannot start with 3 players and 3 impostors cannot start with 5
- **WHEN** 400 blind chaos games are dealt **THEN** some have one impostor and some have everyone as an impostor
- **WHEN** every combination of mode, drawing, hint, chaos and player count is played out **THEN** each below the mode's minimum refuses to start and each other deals and ends consistently
- *Verifies:* `impostorLimitsKeepCiviliansAhead`, `chaosSometimesMakesEveryoneAnImpostor`,
  `everyCombinationDealsRightAndEveryGameEndsConsistently`

### Requirement: The game ends the moment someone wins

The civilians SHALL win when every infiltrator is out; the infiltrators SHALL win when at
most one civilian is left, together if impostor and drifter both remain, the drifter
alone if only it remains. An eliminated drifter SHALL get one guess at the word, ignoring
case, accents and spaces: a right guess SHALL end the game as `DrifterGuessed`, a wrong one
SHALL let the game go on, and a wrong guess by the last infiltrator SHALL give the
civilians the win. In a chaos game where everyone is an impostor, the game SHALL end as
`NoCivilians` when one player is left. A move SHALL NOT change the game it started from,
voting SHALL be refused once someone has won, and ending a game early SHALL end it as
`EndedEarly`, not `NoCivilians`.

#### Scenario: outcomes
- **WHEN** the only impostor of a classic game is eliminated **THEN** the civilians win, and the game dealt earlier still has no outcome
- **WHEN** one civilian of three is eliminated in classic **THEN** the infiltrators win with the impostor role
- **WHEN** two civilians of five are eliminated in drifter mode **THEN** impostor and drifter win together, each scoring `Scoring.INFILTRATOR_WIN`
- **WHEN** the impostor is eliminated and then two civilians **THEN** there is no outcome after the impostor, and the drifter wins alone, scoring `INFILTRATOR_WIN`
- *Verifies:* `civiliansWinWhenEveryInfiltratorIsOut`, `impostorsWinWhenOneCivilianIsLeft`,
  `impostorAndDrifterWinTogetherWhenOneCivilianIsLeft`,
  `theDrifterWinsAloneWhenTheImpostorWasCaught`

#### Scenario: the drifter's guess
- **WHEN** the drifter is eliminated and guesses `"  cafe "` for the word "Café" **THEN** the guess is right and the outcome is `DrifterGuessed`
- **WHEN** the drifter guesses wrong in a game of five **THEN** nothing is decided, no guess is pending and four players are alive
- **WHEN** the impostor and then the drifter are eliminated and the guess is wrong **THEN** the civilians win
- *Verifies:* `eliminatedDrifterGuessesWithAccentsAndCaseIgnored`,
  `wrongDrifterGuessKeepsTheGameGoing`,
  `aWrongGuessByTheLastInfiltratorHandsTheWinToTheCivilians`

#### Scenario: the end of a game
- **WHEN** four all-impostor chaos players are eliminated one by one **THEN** nothing is decided after two and the game ends as `NoCivilians` with one player alive after three
- **WHEN** someone has won in a drifter game of four **THEN** a further elimination fails
- **WHEN** a drifter game is revealed with nothing decided **THEN** the outcome is `EndedEarly`
- *Verifies:* `anAllImpostorGameEndsWhenTheVotesLeaveOnePlayer`, `votingStopsAsSoonAsSomeoneWins`,
  `endingEarlyIsNotANoCiviliansGame`

### Requirement: A match scores winners and counts only finished games

Winners SHALL score `Scoring.CIVILIAN_WIN` = 5 (civilians) or `Scoring.INFILTRATOR_WIN` =
15 (the infiltrators who won, a drifter whose guess was right included). An early end
SHALL score nobody and SHALL NOT count as a round (`Match.counts`), while `NoCivilians`
does count. A match SHALL be over when `Match.isOver` (rounds played reach the chosen
number; 0 means no limit), the choices being `Match.ROUND_OPTIONS` = 3, 5, 7, 10 and 0,
recommended by group size, and ties SHALL share the leadership with nobody leading at
zero points. Catching the impostor in the app SHALL score the game once.

The impostor side is rarer and harder to play, so it pays more; counting abandoned games
would let a match end without anyone having played it.

#### Scenario: scoring a game
- **WHEN** the civilians win a classic game **THEN** every civilian scores 5
- **WHEN** the infiltrators win with three players **THEN** only the impostor scores 15
- **WHEN** an eliminated drifter guesses right **THEN** the drifter scores 15
- **WHEN** a game is revealed early **THEN** nobody scores
- *Verifies:* `winnersScoreWhetherEliminatedOrNot`

#### Scenario: a match
- **WHEN** rounds are counted **THEN** 9 of 10 is not over, 10 of 10 is, a limit of 0 never is, and 7 of 10 leaves 3
- **WHEN** outcomes are counted **THEN** `EndedEarly` is not a round and `NoCivilians` is
- **WHEN** scores tie at the top **THEN** both leaders are listed, and nobody leads at zero
- **WHEN** rounds are recommended **THEN** 4 players get 10 and 10 players get 5, and 24 players get one of the options
- **WHEN** random drifter games are played until three finished ones count **THEN** exactly three are counted, an early end among them scores nothing, and the leaders are those with the best positive score
- *Verifies:* `matchEndsAfterTheChosenRoundsAndTiesShareTheWin`,
  `aMatchCountsOnlyFinishedGamesAndCrownsTheLeaders`

#### Scenario: in the app
- **WHEN** a game is started with four players and revealed early **THEN** its outcome is `EndedEarly`, and after a later save no round is counted and no score exists
- **WHEN** the impostor is eliminated in a four-player game **THEN** the civilians win, the round count becomes 1 and three players are scored, matching the points shown, and revealing and saving again leaves the round count at 1 and the scores unchanged
- *Verifies:* `aGameEndedEarlyIsNotARound`, `catchingTheImpostorScoresTheGameOnce`

### Requirement: Setup keeps the players and limits valid

Names SHALL be trimmed and blanks and case-insensitive duplicates skipped; a requested
impostor count SHALL be reduced to what the players allow; a game SHALL not start with
too few players; starting SHALL deal the game and remember its civilian word as a recent
word. A whole game SHALL be playable from the welcome screen to catching the impostor.

#### Scenario: setup and flow
- **WHEN** `"  Ana "`, `""`, `"ana"` and `"Bea"` are added **THEN** the players are Ana and Bea; removing Ana leaves Bea
- **WHEN** 5 impostors are requested with seven players **THEN** the setting becomes 3
- **WHEN** two players try to start **THEN** it returns false and there is no game
- **WHEN** four players start **THEN** the game has one impostor and the civilian word is stored in the recent words
- **WHEN** three players reveal their cards and vote for the impostor in the app **THEN** the outcome is `CiviliansWin`
- *Verifies:* `addPlayerTrimsAndSkipsBlanksAndDuplicates`,
  `updateSettingsKeepsImpostorsWithinWhatThePlayersAllow`, `aGameNeedsEnoughPlayers`,
  `startingAGameDealsAndRemembersTheWord`, `aWholeGameEndsWithTheImpostorCaught`
