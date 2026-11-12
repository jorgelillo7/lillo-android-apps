# Capability: swiss

Swiss rounds: everyone plays every round against a similarly ranked opponent, with no
rematches, one bye per player, organised-play points and tie-breakers, and an optional
knockout of the best.

- **Source:** `apps/tournaments/domain/` (`Swiss`)
- **Verified by:** `apps/tournaments/domain/src/test/` (`SwissTest`)

---

### Requirement: Rounds pair by ranking without rematches, one bye each

The recommended number of rounds (`Swiss.recommendedRounds`) SHALL be enough for one
undefeated player: 1 for 2 players, 2 for 4, 3 for 6 and 8, 4 for 11, 5 for 32. Pairing
SHALL never repeat a match, and with an odd number of players every round SHALL give one
player a bye, never the same player twice; with an even number there SHALL be none. A
bye SHALL count as a win (`Standings.WIN` points). A paired round with no result yet
SHALL be re-pairable, and once a result is in it SHALL NOT.

#### Scenario: pairing
- **WHEN** rounds are recommended for 2, 4, 6, 8, 11 and 32 players **THEN** the counts are 1, 2, 3, 3, 4 and 5
- **WHEN** 3 to 12 players play a full Swiss with five seeds each **THEN** no pair meets twice, each round has one bye for odd groups and none for even, nobody has two byes, and every player is in the standings
- **WHEN** a five-player round is played **THEN** the player with the bye has `Standings.WIN` points
- **WHEN** the next round is paired and untouched **THEN** it can be unpaired, back to round 1; once a result is recorded it cannot
- *Verifies:* `recommendedRounds`, `noRematchesAndOneByeEach`, `byeCountsAsAWin`,
  `unpairWhileTheRoundIsUntouched`

### Requirement: Standings follow the organised-play rules

A win SHALL score `Standings.WIN` = 3 and a draw `Standings.DRAW` = 1. Ties SHALL break by
the opponents' match-win percentage, then the player's game-win percentage, then the
opponents' game-win percentage, each floored at one third. Without a cut the Swiss
SHALL end when no further round can be paired, and the leader SHALL be the champion;
with a top cut the best SHALL be seeded into a bracket, first against fourth in the
semifinals of a top four, and the bracket's winner is champion.

#### Scenario: points and the leader
- **WHEN** a four-player round has one draw and one 2–1 result **THEN** the standings read 3, 1, 1, 0 with the 2–1 winner first and a game-win rate above 0.6
- **WHEN** a four-player Swiss without a cut plays its two rounds **THEN** there is no champion after the first, no further round can be paired after the second, and the leader is champion
- **WHEN** an eight-player Swiss with a top cut of four finishes **THEN** the top cut can start, seeds the first and fourth into the same semifinal, and the cut's winner is champion
- *Verifies:* `drawsAndTieBreakers`, `swissWithoutCutEndsWithTheLeader`,
  `topCutSeedsTheBestIntoABracket`

> **GAP — unverified.** The order of the three tie-breakers and the one-third floor
> (`FLOOR` in `Swiss.kt`) are not asserted by any test: the draw test only checks points
> and one game-win rate. A test would build two players level on points and assert the
> opponents' match-win percentage decides, and a winless player's percentage is 1/3.
