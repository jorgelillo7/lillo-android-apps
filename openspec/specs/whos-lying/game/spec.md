# Capability: game

The impostor word game for 3 to 24 players on one phone: roles are dealt, the group
discusses and votes players out, the game ends when the civilians or the infiltrators
win, and a match of several games crowns whoever scored most.

- **Source:** `apps/whos-lying/domain/` (`Game`, `Settings`, the match scoring),
  `apps/whos-lying/app/` (`GameViewModel`, the home)
- **Verified by:** `apps/whos-lying/domain/src/test/` (`GameTest`,
  `ExhaustiveGameTest`), `apps/whos-lying/app/src/test/` (`GameViewModelTest`,
  `GameFlowTest`, `RoundScreensScreenshotTest`)

---

### Requirement: Each mode deals what it promises

In **Original** every player SHALL be told their role; impostors SHALL get no word, and with
the impostor hint on, the entry's similar word as a hint. In **Mysterious** nobody SHALL be
told their role but journalists: impostors SHALL get the similar word as if it were the word,
and journalists (0 or more) SHALL get no word, know it, and never start. In **Chaos** about one
game in three (`Game.CHAOS_PROBABILITY`) SHALL deal more impostors than usual, up to
everyone, each with or without a hint; the others SHALL deal the usual count for the group
(`Rules.recommendedImpostors`, about one per five players) with a hint; everyone
SHALL know their role and the game SHALL never score. In **drawing** mode a Mysterious impostor
SHALL draw blind, knowing it. Impostors and journalists together SHALL be at most half the group
(`Rules.maxInfiltrators`), at least one of them, with an impostor except in Mysterious; Chaos
ignores the set count.

Mysterious only works if the impostor cannot tell from the screen; the cap keeps a game from
being decided before it is played.

#### Scenario: what each role is dealt
- **WHEN** an Original game with two impostors is dealt with the hint on and off **THEN** both know their role and have no word, and the hint is the similar word only when on; civilians have the word and know their role
- **WHEN** a Mysterious game is dealt **THEN** the impostor has the similar word and does not know their role, and civilians do not know theirs
- **WHEN** a drawing game is dealt in each mode **THEN** the impostor has no word and knows it
- **WHEN** a Mysterious game with two journalists and no impostor is dealt with 30 seeds **THEN** both journalists have no word, know it, and never start
- *Verifies:* `originalTellsEveryoneTheirRoleAndTheHintIsTheSimilarWord`,
  `mysteriousImpostorGetsTheSimilarWordAndDoesNotKnow`, `drawingImpostorsGetNoWordInAnyMode`,
  `journalistsGetNoWordKnowItAndNeverStart`

#### Scenario: limits and chaos
- **WHEN** limits are asked **THEN** 3 players allow 1 infiltrator, 4 allow 2 and 24 allow 12; Mysterious starts with 3 players and one journalist alone, and not with nobody; settings beyond the cap are brought within it
- **WHEN** the usual count is asked **THEN** 3 and 7 players get 1, 8 and 12 get 2, 13 get 3 and 24 get 5; 300 Chaos games of 12 never have fewer than 2 impostors and mostly 2
- **WHEN** 400 Chaos games of five are dealt **THEN** most have one impostor, some have two or everyone, all tell every role, none is scored, and struck games have impostors with and without a hint
- **WHEN** every combination of mode, drawing, hint, players, impostors and journalists is played out **THEN** each invalid one refuses to start and each other deals and ends consistently
- *Verifies:* `impostorsAndJournalistsAreAtMostHalfTheGroup`,
  `chaosUsuallyHasImpostorsForTheGroupSizeAndSometimesManyOrEveryone`,
  `everyCombinationDealsRightAndEveryGameEndsConsistently`

#### Scenario: settings saved by earlier versions
- **WHEN** settings written with modes CLASSIC, BLIND, DRIFTER or the chaos switch are read **THEN** they become Mysterious, Original without hint, Mysterious with one journalist and Chaos, and an unknown mode reads as Original
- **WHEN** new settings are written and read back **THEN** they are the same and use no old name
- *Verifies:* `theModesOfEarlierVersionsMapOntoTheNewOnes`, `theNewSettingsReadBackAsWritten`,
  `theStateTheReleasedAppWritesStillReads`

### Requirement: The game ends the moment someone wins

The civilians SHALL win when every infiltrator is out; the infiltrators SHALL win when at
most one civilian is left, together if impostors and journalists both remain, the
journalists alone if only they remain. An eliminated journalist SHALL get one guess at the
word, ignoring case, accents and spaces: a right guess SHALL end the game as
`JournalistGuessed`, a wrong one
SHALL let the game go on, and a wrong guess by the last infiltrator SHALL give the
civilians the win. In a chaos game where everyone is an impostor, the game SHALL end as
`NoCivilians` when one player is left. A move SHALL NOT change the game it started from,
voting SHALL be refused once someone has won, and ending a game early SHALL end it as
`EndedEarly`, not `NoCivilians`.

#### Scenario: outcomes
- **WHEN** the only impostor of a Mysterious game is eliminated **THEN** the civilians win, and the game dealt earlier still has no outcome
- **WHEN** one civilian of three is eliminated in Mysterious **THEN** the infiltrators win with the impostor role
- **WHEN** two civilians of five are eliminated in Mysterious with a journalist **THEN** impostor and journalist win together, each scoring `Scoring.INFILTRATOR_WIN`
- **WHEN** the impostor is eliminated and then two civilians **THEN** there is no outcome after the impostor, and the journalist wins alone, scoring `INFILTRATOR_WIN`
- *Verifies:* `civiliansWinWhenEveryInfiltratorIsOut`, `impostorsWinWhenOneCivilianIsLeft`,
  `impostorAndJournalistWinTogetherWhenOneCivilianIsLeft`,
  `theJournalistWinsAloneWhenTheImpostorWasCaught`

#### Scenario: the journalist's guess
- **WHEN** the journalist is eliminated and guesses `"  cafe "` for the word "Café" **THEN** the guess is right and the outcome is `JournalistGuessed`
- **WHEN** the journalist guesses wrong in a game of five **THEN** nothing is decided, no guess is pending and four players are alive
- **WHEN** the impostor and then the journalist are eliminated and the guess is wrong **THEN** the civilians win
- *Verifies:* `eliminatedJournalistGuessesWithAccentsAndCaseIgnored`,
  `wrongJournalistGuessKeepsTheGameGoing`,
  `aWrongGuessByTheLastInfiltratorHandsTheWinToTheCivilians`

#### Scenario: the end of a game
- **WHEN** four all-impostor chaos players are eliminated one by one **THEN** nothing is decided after two and the game ends as `NoCivilians` with one player alive after three
- **WHEN** someone has won in a game of four with a journalist **THEN** a further elimination fails
- **WHEN** a game with a journalist is revealed with nothing decided **THEN** the outcome is `EndedEarly`
- *Verifies:* `anAllImpostorGameEndsWhenTheVotesLeaveOnePlayer`, `votingStopsAsSoonAsSomeoneWins`,
  `endingEarlyIsNotANoCiviliansGame`

### Requirement: A match scores winners and counts only finished games

Winners SHALL score `Scoring.CIVILIAN_WIN` = 5 (civilians) or `Scoring.INFILTRATOR_WIN` =
15 (the infiltrators who won; a journalist whose guess was right scores alone); a Chaos game
SHALL score nobody. An early end
SHALL score nobody and SHALL NOT count as a round (`Match.counts`), while `NoCivilians`
does count. A match SHALL be over when `Match.isOver` (rounds played reach the chosen
number; 0 means no limit), the choices being `Match.ROUND_OPTIONS` = 3, 5, 7, 10 and 0,
recommended by group size, and ties SHALL share the leadership with nobody leading at
zero points. Catching the impostor in the app SHALL score the game once.

The impostor side is rarer and harder to play, so it pays more; counting abandoned games
would let a match end without anyone having played it.

#### Scenario: scoring a game
- **WHEN** the civilians win a Mysterious game **THEN** every civilian scores 5
- **WHEN** the infiltrators win with three players **THEN** only the impostor scores 15
- **WHEN** one of two journalists is eliminated and guesses right **THEN** only that journalist scores 15
- **WHEN** the civilians win a Chaos game **THEN** nobody scores
- **WHEN** a game is revealed early **THEN** nobody scores
- *Verifies:* `winnersScoreWhetherEliminatedOrNot`

#### Scenario: a match
- **WHEN** rounds are counted **THEN** 9 of 10 is not over, 10 of 10 is, a limit of 0 never is, and 7 of 10 leaves 3
- **WHEN** outcomes are counted **THEN** `EndedEarly` is not a round and `NoCivilians` is
- **WHEN** scores tie at the top **THEN** both leaders are listed, and nobody leads at zero
- **WHEN** rounds are recommended **THEN** 4 players get 10 and 10 players get 5, and 24 players get one of the options
- **WHEN** random games with a journalist are played until three finished ones count **THEN** exactly three are counted, an early end among them scores nothing, and the leaders are those with the best positive score
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
- **WHEN** 5 impostors are requested with seven players **THEN** the setting becomes 3 (at most half)
- **WHEN** two players try to start **THEN** it returns false and there is no game
- **WHEN** four players start **THEN** the game has one impostor and the civilian word is stored in the recent words
- **WHEN** three players reveal their cards and vote for the impostor in the app **THEN** the outcome is `CiviliansWin`
- *Verifies:* `addPlayerTrimsAndSkipsBlanksAndDuplicates`,
  `updateSettingsKeepsImpostorsWithinWhatThePlayersAllow`, `aGameNeedsEnoughPlayers`,
  `startingAGameDealsAndRemembersTheWord`, `aWholeGameEndsWithTheImpostorCaught`

### Requirement: Settings are one tap away from the home

The home SHALL open the settings sheet (language, look, rules, sharing, rating, privacy) from an
info icon at its top right, the place every app keeps its About, upright and on a short landscape
screen; the sheet opens expanded and scrolls (core `LilloSheet`).

#### Scenario: a landscape phone
- **WHEN** the home is shown on a landscape phone and the top-right icon is tapped **THEN** it is on screen and opens the settings
- *Verifies:* `theHomeOpensTheSettingsFromTheTopRight`
