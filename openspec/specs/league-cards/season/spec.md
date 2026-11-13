# Capability: season

A fantasy-league management game played by swiping cards: each matchday the league's
members write to you, you decide, four meters drift, and a season of 19 matchdays (or
half of that) ends in a final position or one of the silly endings.

- **Source:** `apps/league-cards/domain/` (`Game` and the `League` deck data),
  `apps/league-cards/app/` (`LeagueViewModel`)
- **Verified by:** `apps/league-cards/domain/src/test/` (`GameTest`),
  `apps/league-cards/app/src/test/` (`LeagueViewModelTest`, `SeasonFlowTest`)

---

### Requirement: The deck is data, validated, and the private one never ships

The sample deck SHALL have no problems. When the league's private deck exists on the
machine it SHALL also have no problems and a careful player SHALL finish at least 120 of
200 seasons on it; when it does not exist the test does nothing. Validation SHALL report
a card whose character is unknown and a choice whose follow-up card is unknown, and the
sample deck SHALL have ten endings, each with a text.

The funny content is the league's own and stays out of the repo; a broken private deck
must fail where it is built, not mid-season on a phone.

#### Scenario: validation
- **WHEN** the sample deck is validated **THEN** the list of problems is empty
- **WHEN** the private deck is on this machine **THEN** it has no problems and careful play finishes at least 120 of 200 seasons
- **WHEN** a card names character "nobody" and a choice the follow-up "missing" **THEN** the problems include `unknown character 'nobody'` and `unknown follow-up 'missing'`
- **WHEN** the sample's endings are listed **THEN** there are ten and every one has non-blank text
- *Verifies:* `the sample deck is valid`, `the private deck is valid when it is on this machine`,
  `problems catch broken references`, `every ending key has a text`

> **GAP — unverified.** The private-deck scenario cannot run in CI or on a machine without
> the deck (the test returns early), so only the sample deck is checked there.

### Requirement: A season has a length, and a short one keeps the late cards

A full season SHALL last the league's number of matchdays (`matchdays(league)`), also for
a `Season` saved before short seasons existed. A short season (`Game.shortLength`) SHALL
last `(league.matchdays + 1) / 2` matchdays, map them onto the calendar's later matchdays so
that late cards (`fromMatchday`) are still dealt, and end after them. A new season SHALL be
dealt at matchday 1 with a non-empty hand and no ending and saved; choosing or using the
joker with no season SHALL do nothing.

#### Scenario: length
- **WHEN** a league of three matchdays starts a full season **THEN** it lasts three, a season saved without a length lasts three, and its calendar matchday 2 is 2
- **WHEN** a short season of a league with a late card (from matchday 3) is played **THEN** it lasts two matchdays that map to calendar matchdays 2 and 3, and the late card is dealt
- **WHEN** a short season is started in the app **THEN** it lasts `(matchdays + 1) / 2`
- **WHEN** a new season is started in the app **THEN** it is saved at matchday 1 with a hand and no ending
- **WHEN** choosing and the joker are used before any season, and a season is then started **THEN** the saved season has hand index 0 and the joker unused
- *Verifies:* `a full season is the league's length, also for seasons saved before short ones`,
  `a short season ends after half the matchdays and still reaches the late cards`,
  `aShortSeasonHasHalfTheMatchdays`, `aNewSeasonIsDealtAndSaved`,
  `movesWithoutASeasonAreIgnored`

### Requirement: Choices decide the meters, the story and the end

A given seed and set of choices SHALL give the same season. A choice with a follow-up
SHALL deal that card first on the next matchday; a choice with a next card SHALL show it
straight after, in the same matchday, adding it to the hand. A gamble with certain risk
SHALL apply its own effects and outcome text. A matchday event marked "no points" SHALL
leave the player's points at 0 and appear in the report. The joker SHALL skip a card
applying its effects (here patience −10, from 50 to 40) once, after which it cannot be
used. A meter at zero SHALL end the season with that meter's ending; the last matchday
SHALL end it with the final position (champion for first, else the position). On the
sample deck a careful player SHALL finish at least 120 of 200 seasons and a random one
between 5 and 120.

#### Scenario: playing
- **WHEN** a seed and a rule of choosing are played twice **THEN** the results are equal
- **WHEN** a choice with follow-up "f" is made **THEN** "f" is not in the hand at first, and is the current card after the matchday's report is dismissed
- **WHEN** a choice with next card "story" is made **THEN** the current card is "story", the hand has three cards, and the report is not pending until the next choice
- **WHEN** a guaranteed-to-fail gamble is the first card **THEN** the matchday's points are −16 and the outcome text is shown
- **WHEN** a matchday event with no points is applied **THEN** the report shows 0 points and that event
- **WHEN** the joker is used **THEN** patience is 40, the hand index is 1, and it cannot be used again
- *Verifies:* `same seed and choices give the same season`,
  `a follow-up is dealt first next matchday`, `a next card comes straight after, this matchday`,
  `a gamble that always goes wrong applies its own effects and outcome`,
  `an invalid line-up scores nothing`, `the joker skips a card once a season`,
  `theJokerIsUsedOnce`, `choicesAdvanceTheHandAndTheReportIsDismissed`

#### Scenario: endings and balance
- **WHEN** a choice drops money by 100 **THEN** the season ends with the money-low ending
- **WHEN** a two-card league is played to the end **THEN** the ending has a position 1 or 2, its text is "champion" for 1, and the matchday is 4
- **WHEN** careful and random players each play 200 seasons of the sample **THEN** careful ones finish at least 120 and random ones between 5 and 120
- *Verifies:* `a meter at zero ends the season with its ending`,
  `the last matchday ends with the final position`,
  `a careful player usually finishes the season, a random one sometimes`

### Requirement: Chat lines do not repeat until they run out

The league's chat after a matchday SHALL not repeat a line within a season until the pool
is used up, and placeholders `{leader}`, `{position}` and `{last}` SHALL be filled.

#### Scenario: chat
- **WHEN** two matchday reports are generated from a pool of four lines for 20 seeds **THEN** the four lines appear once across them, with the placeholders replaced
- *Verifies:* `chat lines don't repeat until they run out, and placeholders are filled`

### Requirement: A whole season can be played and recorded

#### Scenario: the flow
- **WHEN** a season is played in the app by tapping choices and the joker until the ending shows **THEN** one season is recorded with one history entry, and the trophies screen shows that entry's ending
- *Verifies:* `aWholeSeasonEndsAndIsRecorded`

### Requirement: The rules are open for a new player, folded for a returning one

The home SHALL show the rules to a player with no season played or under way, and fold them
behind "¿Cómo se juega?" for a returning player, opening them when it is tapped.

#### Scenario: the rules
- **WHEN** nothing has been played, a season is under way, or one was played **THEN** the player is new only in the first case
- **WHEN** a new player opens the home **THEN** the rules show; a returning player sees only "¿Cómo se juega?" until it is tapped, then the rules
- *Verifies:* `a player is new until a season is under way or played`, `aNewPlayerSeesTheRules`,
  `aReturningPlayerOpensTheRulesWhenAsked`, `homeFirstTime`, `homeFirstTimeLight`
