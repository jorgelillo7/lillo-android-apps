# Capability: bracket

Casual knockout tournaments for any number of players: a seeded bracket with byes, the
best-of series per match, a two-sided layout, and the champion's path.

- **Source:** `apps/tournaments/domain/` (`Bracket`, `BracketLayout`, `Model`),
  `apps/tournaments/app/` (`TournamentsViewModel`)
- **Verified by:** `apps/tournaments/domain/src/test/` (`BracketTest`,
  `LayoutStatsLibraryTest`), `apps/tournaments/app/src/test/`
  (`TournamentFlowTest`, `TournamentsViewModelTest`)

---

### Requirement: Any number of players fit a bracket with fair byes

The bracket SHALL be the smallest power of two that fits the players (`Bracket.size`),
with `Bracket.rounds` rounds, seeded in the standard order so the best seeds meet last.
Every player SHALL appear exactly once in the first round, no match SHALL be two empty
places, the byes SHALL number the bracket size minus the players, and the bracket SHALL
hold size − 1 matches. With six players the byes go straight to the semifinals.

Casual groups are never a power of two, and a bye that plays a bye would stall a round.

#### Scenario: size, seeds and byes
- **WHEN** 1, 2, 3, 5, 8, 11 and 16 players are sized **THEN** the brackets are 2, 2, 4, 8, 8, 16 and 16, with 3 rounds for 6 players and 4 for 11
- **WHEN** an eight-slot bracket is seeded **THEN** the order is 1, 8, 4, 5, 2, 7, 3, 6
- **WHEN** any number from 2 to 33 players is drawn **THEN** the first round has size/2 matches, none empty, each player once, size − n byes, and size − 1 matches overall
- **WHEN** six players are drawn **THEN** the two byes are exactly the players in the semifinals, each semifinal having one place filled and no result yet
- *Verifies:* `sizesAndRounds`, `standardSeedOrder`, `everyPlayerOnceAndNoByeAgainstBye`,
  `sixPlayersByesGoStraightToSemis`

### Requirement: Results advance winners, and a changed result clears what followed

Recording every pending match SHALL crown the final's winner as champion and finish the
tournament. Changing an earlier result SHALL clear the downstream matches' scores and
comments and the champion, and put the new winner in the next round. A match SHALL be won
by whoever reaches the majority of its best-of 1, 3 or 5. The random draw SHALL be
redoable only before any result is recorded.

A result edited under later rounds would leave a bracket nobody actually played.

#### Scenario: advancing and editing
- **WHEN** 11 players play every pending match 2–1 **THEN** the final's first player is the champion and the tournament is over
- **WHEN** a four-player bracket is played to its champion and a semifinal result is then reversed **THEN** the final holds the other semifinalist with 0–0, no comment and no champion
- **WHEN** a best-of-1 match is recorded 1–0, a best-of-5 match 2–1 and then 2–3 **THEN** the first has a champion, the best-of-5 has none after 2–1 and the second player after 2–3
- **WHEN** a tournament has no results **THEN** it can be redrawn, which changes the matches; after one result it cannot
- *Verifies:* `playingItOutCrownsAChampion`, `changingAnEarlierResultClearsWhatFollowed`,
  `bestOfOneAndFive`, `redrawOnlyBeforeResults`

> **GAP — unverified.** That only valid scores are offered for a best-of length (no 3–3
> in best of 5) lives in the screens and no test names it; a test would list the offered
> scores for each best-of.

### Requirement: The two-sided layout and the champion's path

For three rounds the layout SHALL have 5 columns and 2 rows with 7 cells, the first-round
cells split into a left and a right side, the semifinals one column in from each end and
the final in the middle column with the centre side. A bracket of a single round (just a
final) SHALL have one column. The champion's path SHALL be the matches the champion won:
three of them in an eight-player bracket.

#### Scenario: layout
- **WHEN** three rounds are laid out **THEN** there are 5 columns, 2 rows and 7 cells, with left and right sides, semifinals in columns 1 and 3 and the final in column 2
- **WHEN** only a final exists **THEN** the layout has one column
- **WHEN** an eight-player bracket is played out **THEN** the champion's path has three matches
- *Verifies:* `twoSidedLayoutForEightPlayers`, `aFinalAloneIsCentred`,
  `championPathEndsAtTheFinal`

> **GAP — unverified.** `championPathEndsAtTheFinal` only counts the path (3 matches);
> that the last of them is the final is not asserted.

### Requirement: A knockout can be played from creation to champion

#### Scenario: the flow
- **WHEN** a user creates "Smoke cup" with Ann, Bob, Cat and Dan, starts it and records the three matches in the matches tab **THEN** the champion is shown and one tournament named "Smoke cup" is stored
- *Verifies:* `aKnockoutFromNewTournamentToChampion`

> **GAP — unverified.** The tournament's own bookkeeping — the version bump on every
> change (`revision`), days-since-1970 dates, the champion field's persistence —
> has no test of its own; a test would edit a result and assert the version increased.
