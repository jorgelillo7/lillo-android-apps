# Capability: answer

"¿Hay jaleo?": one answer, in big letters, to whether tomorrow is busy around the
Metropolitano stadium, and what the next busy days are.

- **Source:** `apps/stadium-events/domain/` (`Jaleo`, `Feed`),
  `apps/stadium-events/app/` (`JaleoViewModel`, the texts)
- **Verified by:** `apps/stadium-events/domain/src/test/` (`JaleoTest`),
  `apps/stadium-events/app/src/test/` (`JaleoTextsTest`, `JaleoViewModelTest`,
  `JaleoFlowTest`)

---

### Requirement: Tomorrow is busy, calm or maybe

An event dated tomorrow SHALL make it a busy day with that event, a day with no event SHALL
be calm (carrying its date), and an event whose day is not fixed (`either`) SHALL make a busy
day marked `maybe`. A fixed event SHALL beat a maybe, and among events the biggest SHALL
win: a concert beats a match and a maybe. Every night of a tour SHALL be busy. The busy
period SHALL start two hours before the event's start time (21:00 → 19:00, 18:30 → 16:30),
and have no start when the time is missing or is "por confirmar".

An elderly neighbour deciding whether to drive needs a definite answer, with the doubt
stated rather than hidden.

#### Scenario: the answer
- **WHEN** a Viking FK match is on 25 November and it is the 24th **THEN** the answer is busy with that event, dated the 25th and not a maybe
- **WHEN** the date is the 25th and nothing is on the 26th **THEN** the answer is calm, dated the 26th
- **WHEN** only an unfixed Betis match is on 4 or 5 December and it is 4 December **THEN** the answer is busy and a maybe
- **WHEN** a maybe, a match and a concert fall on the same day **THEN** the concert is the busy event
- **WHEN** only a maybe and a match fall on the same day, in either order **THEN** the match is the busy event and the answer is not a maybe
- **WHEN** it is the day before the second night of a tour **THEN** the answer is busy
- **WHEN** the start is "21:00", "18:30", null or "por confirmar" **THEN** busy-from is "19:00", "16:30", none and none
- *Verifies:* `aMatchTomorrowIsABusyDay`, `nothingTomorrowIsACalmDay`,
  `anUnfixedDayIsAMaybe`, `aFixedEventBeatsAMaybeAndTheBiggestWins`, `aFixedMatchBeatsAMaybeOnItsOwn`,
  `everyNightOfATourIsBusy`, `busyFromIsTwoHoursBeforeTheStart`

### Requirement: The next busy days are few and skip tomorrow and the past

The list of upcoming days SHALL leave out tomorrow's event and past events, be ordered by
date, carry the maybe flag, and hold at most three entries by default. Titles SHALL be
shortened: "A vs B" becomes "A – B" with "Atlético de Madrid" as "Atlético", and a leading year is dropped.

#### Scenario: the list
- **WHEN** a concert, a past event, the Betis match and tomorrow's Viking match are listed **THEN** the upcoming are the Betis match (5 December, maybe) and the concert, in that order
- **WHEN** nine events are listed **THEN** three are returned
- **WHEN** titles are shortened **THEN** "Atlético de Madrid vs Viking FK" is "Atlético – Viking FK", "España vs Inglaterra" is "España – Inglaterra", "Harry Styles" is unchanged and "2027 UEFA Champions League Final" is "UEFA Champions League Final"
- *Verifies:* `upcomingSkipsTomorrowsEventAndThePast`, `upcomingIsShort`, `shortTitles`

### Requirement: The texts say what, when and from when

The reminder text SHALL give the kind, time, title, competition, and the time traffic
rises with the places affected; the tomorrow label SHALL name the weekday and day. An unfixed match
SHALL read "El sábado 5 o domingo 6: el día aún no está confirmado" and its row "día por confirmar"
with both dates, a tour SHALL show its days count, and the last check SHALL read "hoy a las 8:05" the same day
and "el 24 nov a las 8:05" later.

#### Scenario: texts
- **WHEN** tomorrow's busy answer is the Viking match **THEN** the notification reads "Partido · 21:00 · Atlético – Viking FK (Champions). Más gente y tráfico desde las 19:00 aprox. por Canillejas, Las Rosas, Coslada y la M-40." and the label is "Mañana, miércoles 25"
- **WHEN** the match is unfixed **THEN** the hint, the row's date and its description read as above
- **WHEN** a four-day tour is listed **THEN** it reads "Concierto · Karol G · 4 días"
- **WHEN** the last check was at 08:05 on the 24th **THEN** it reads "Revisado hoy a las 8:05" that day and "Revisado el 24 nov a las 8:05" the next
- *Verifies:* `theReminderSaysWhatWhenAndFromWhen`, `anUnfixedMatchShowsBothDays`,
  `aTourCountsItsNights`, `checkedTodayOrOnADay`

### Requirement: The first open answers tomorrow

#### Scenario: the first open
- **WHEN** the app is opened for the first time **THEN** it shows the busy answer, the Viking match, the Betis match and the Champions League final rows, stores the fetch time, and the about screen opens and closes back to the answer
- *Verifies:* `firstOpenAnswersTomorrowAndShowsWhatsNext`
