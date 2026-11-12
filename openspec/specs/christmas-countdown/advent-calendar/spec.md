# Capability: advent-calendar

Twenty-four doors, one unlocking each day of December, behind them a different activity
or song every year, in an order that is the same for everyone within a year.

- **Source:** `apps/christmas-countdown/domain/` (`AdventCalendar`, `AdventPlan`)
- **Verified by:** `apps/christmas-countdown/domain/src/test/` (`AdventCalendarTest`,
  `AdventPlanTest`)

---

### Requirement: One door a day in December

No door SHALL be unlocked outside December, door N SHALL be openable on December N and
doors beyond today SHALL stay closed (door 0 is never a door), and all 24 SHALL be open from
Christmas Eve to the end of December.

#### Scenario: opening
- **WHEN** it is 30 November or 1 January **THEN** zero doors are unlocked, and 2 October is not in the advent season
- **WHEN** it is 5 December **THEN** 5 doors are unlocked; door 5 can be opened and door 6 and door 0 cannot
- **WHEN** it is 24 or 31 December **THEN** 24 doors are unlocked
- *Verifies:* `lockedOutsideDecember`, `oneDoorPerDayInDecember`,
  `allDoorsOpenFromChristmasEve`

### Requirement: The layout shuffles each year and a published year never changes

The order of the doors SHALL be a permutation of 1 to 24 for each year, stable within a
year and different between years, and the order for 2026 SHALL be exactly the published
one (22, 19, 9, 16, …, 11), so what a child already opened stays where it was.

A reshuffle after release would put different things behind doors a family had already
opened.

#### Scenario: shuffle and stability
- **WHEN** the order is built for each year from 2026 to 2040 **THEN** sorted it is 1 to 24
- **WHEN** it is built twice for 2026 **THEN** it is equal, and 2027's differs
- **WHEN** 2026's order is built **THEN** it equals the published list
- *Verifies:* `doorOrderIsAShuffleOfAllDoors`,
  `doorOrderIsStableWithinAYearAndChangesBetweenYears`,
  `doorOrderForAPublishedYearNeverChanges`

### Requirement: What is behind the doors follows the plan

For each tested year every door 1 to 24 SHALL hold something and nothing SHALL repeat.
Door 24 SHALL always be the Christmas Eve activity. Each year SHALL have two or three songs,
all between the 8th and the 23rd, and each other activity SHALL sit in a day of its own
block. The plan SHALL differ between years, every activity SHALL come out within the first
three years, activities of one block SHALL be used within one of each other, and the plan for 2026 SHALL be exactly the published one. A year's plan
SHALL be stable, and a year before the first one SHALL get the first year's plan.

#### Scenario: the plan
- **WHEN** each tested year's plan is built **THEN** it has doors 1 to 24, with no repeated content
- **WHEN** door 24 of each tested year is read **THEN** it is the Christmas Eve activity
- **WHEN** songs and activities are checked **THEN** each year has two or three songs, all on days 8 to 23, and every activity sits in its block's days
- **WHEN** 2026 and 2027 are compared **THEN** the plans differ, across 2026 to 2028 every activity appears, and over thirteen years the uses of the activities within a block differ by at most one
- **WHEN** 2026's plan is listed **THEN** it equals the published sequence
- **WHEN** a plan is built twice, and for 2020 **THEN** it is equal, and 2020 gets 2026's
- *Verifies:* `everyDoorHasSomethingAndNothingRepeatsInAYear`,
  `christmasEveIsAlwaysDoor24`,
  `activitiesStayInTheirBlockAndSongsBetweenThe8thAndThe23rd`,
  `theCalendarChangesEveryYearAndEverythingComesRound`, `the2026CalendarNeverChanges`,
  `aYearsPlanIsStable`
