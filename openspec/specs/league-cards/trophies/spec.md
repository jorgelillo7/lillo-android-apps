# Capability: trophies

Achievements tied to cards or to how the season ends, and the ten endings to collect.

- **Source:** `apps/league-cards/domain/` (`Game`)
- **Verified by:** `apps/league-cards/domain/src/test/` (`GameTest`)

---

### Requirement: Achievements unlock once, from choices, gambles or the end

An achievement tied to a card SHALL unlock after its count of choices (two yeses for a
count of 2), be reported as just unlocked once, and stay unlocked without being reported
again. A gamble that fails SHALL count its own achievement and not the choice's. At the end
of the season the built-in achievements (here "survivor" and "podium") SHALL unlock and be
saved, the season SHALL be recorded once in the history with its ending marked as seen,
even when the report is dismissed and saved again.

#### Scenario: unlocking
- **WHEN** an achievement of count 2 gets one choice, then a second, then a third **THEN** it is unlocked only after the second, reported as just unlocked then, and not reported again after the third
- **WHEN** a failing gamble carries its own achievement "flop" and the choice carries "safe" **THEN** only "flop" is unlocked
- **WHEN** a season is played to its end and saved at each step **THEN** the built-in achievements are unlocked and saved, one season is recorded with one history entry, and the ending is marked as seen
- *Verifies:* `an achievement unlocks after its count of choices, once`,
  `a gamble counts its own achievement, not the choice's`,
  `the end of the season unlocks the built-in achievements and is recorded once`

> **GAP — unverified.** The trophies screen's "endings locked until seen" and the cap of
> the last 30 seasons are not asserted: `aWholeSeasonEndsAndIsRecorded` checks that the
> ended season's trophy shows, but no test hides an unseen ending or records 31 seasons.
