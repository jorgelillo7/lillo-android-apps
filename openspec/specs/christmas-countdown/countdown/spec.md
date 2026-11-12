# Capability: countdown

A live countdown to the next Christmas with real time left.

- **Source:** `apps/christmas-countdown/domain/` (the countdown rules),
  `apps/christmas-countdown/app/` (modes, widget)
- **Verified by:** `apps/christmas-countdown/domain/src/test/` (`ChristmasCountdownTest`),
  `apps/christmas-countdown/app/src/test/` (`CountdownFlowTest`)

---

### Requirement: The countdown is real time to the next Christmas

The countdown SHALL target 25 December and report days, hours, minutes and seconds left
and the nights ("sleeps") left. Christmas Day SHALL be its own state for the whole day,
and after it the target SHALL roll over to the next year. The hour gained or lost at a
daylight-saving change between now and Christmas SHALL be part of the time left.

A countdown that ignored the clock change would be an hour off for the children counting.

#### Scenario: edges of the year
- **WHEN** it is 2 October 2026 at 12:00 **THEN** 83 days and 12 hours are left, 84 sleeps, with 25 December 2026 as target
- **WHEN** it is 24 December at 23:59:59 **THEN** one second and one sleep are left
- **WHEN** it is 25 December at 00:00 or at 23:59:59 **THEN** the state is Christmas Day
- **WHEN** it is 26 December 2026 **THEN** 364 days and 364 sleeps are left, with 25 December 2027 as target
- **WHEN** it is 2 October 2026 at 18:00 in Madrid **THEN** 83 days and 7 hours are left, with the clock-change hour counted
- *Verifies:* `countsDownWithinTheSameYear`, `lastSecondBeforeChristmas`,
  `christmasDayLastsAllDay`, `rollsOverToNextYearAfterChristmas`,
  `includesTheDaylightSavingHour`

### Requirement: The app offers its modes

The user SHALL be able to switch between the sleeps mode and the time mode (stored),
open the advent calendar and its about screen and come back, and turn the music on and
off (stored).

#### Scenario: the modes
- **WHEN** the user taps the sleeps mode, then the time mode **THEN** the sleeps setting is stored on, then off
- **WHEN** the user opens the advent tab and the about screen and goes back to the countdown **THEN** the screens are reachable
- **WHEN** the music is switched on and off **THEN** the music setting is stored on, then off
- *Verifies:* `modesAdventAboutAndCarols`
