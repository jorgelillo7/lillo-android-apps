# Capability: carols-and-reminders

Carols played by an on-device music-box synthesiser (no audio files), a shuffled playlist
that does not repeat back to back, and reminders at the moments that matter.

- **Source:** `apps/christmas-countdown/domain/` (`Melody`, `ShuffledPlaylist`,
  `Reminders`)
- **Verified by:** `apps/christmas-countdown/domain/src/test/` (`MelodyTest`,
  `ShuffledPlaylistTest`, `RemindersTest`)

---

### Requirement: Carols are scores the synthesiser renders cleanly

A score line `name:beats | …` SHALL parse note names with sharps and rests to MIDI
numbers (A4 is 69 and 440 Hz, F#5 is 78, C4 is 60, a rest has no pitch or frequency) and
sum their beats. Every built-in carol SHALL fill whole bars and stay between MIDI 72 and
89, the music-box range. Rendering SHALL produce exactly the carol's duration of samples
at the given rate, with a peak between 20,000 and the 16-bit maximum; a non-looping
render SHALL be longer than a looping one, so the last note rings out.

#### Scenario: scores and synthesis
- **WHEN** `A4:1 | F#5:0.5 R:2 C4:1` is parsed at 120 bpm **THEN** the notes are 69, 78, a rest and 60, the total is 4.5 beats and A4 is 440 Hz
- **WHEN** each built-in carol is checked **THEN** its length is a whole number of bars and its pitches are within 72–89
- **WHEN** "Jingle Bells" is rendered at 8 kHz **THEN** the sample count matches its duration and the peak is between 20,000 and 32,767
- **WHEN** "Silent Night" is rendered without looping **THEN** it has more samples than the looped render
- *Verifies:* `parsesPitchesSharpsAndRests`, `carolsFillWholeBars`,
  `carolsStayInAMusicBoxRange`, `synthRendersAudibleUnclippedLoop`,
  `nonLoopingRenderLetsTheLastNoteRingOut`

### Requirement: The playlist plays everything once a round and never repeats back to back

Every item SHALL play once per round, never the same item twice in a row (over 1,000
plays), and a single item SHALL simply repeat.

#### Scenario: shuffling
- **WHEN** 20 rounds are played **THEN** each round contains every item exactly once
- **WHEN** 1,000 items are played **THEN** no two neighbours are equal
- **WHEN** there is one item **THEN** it plays again each time
- *Verifies:* `everyItemPlaysOncePerRound`, `neverRepeatsTheSameItemBackToBack`,
  `singleItemJustRepeats`

### Requirement: Reminders fall on the milestones, the morning, and open doors only

Morning reminders SHALL be due on 25 November (one month left), 1 December (the advent
starts), 18 December (one week left) and 25 December (Christmas Day), and not on other
days. The evening reminder (a door waiting) SHALL be due from 2 December to 24 December
only while the day's door has not been opened, and not on 1 December, on 25 December or
before the season. The next slot SHALL walk the season: from October to 25 November's
morning, from 25 November to 1 December's morning, from 1 December to 2 December's
evening, and so on, wrapping to next year after Christmas.

#### Scenario: when reminders fire
- **WHEN** the morning of 25 Nov, 1 Dec, 18 Dec and 25 Dec is asked **THEN** the milestone, advent start, one week and Christmas Day reminders are due, and 10 December's morning has none
- **WHEN** the evening of 2 or 24 December is asked with the door closed **THEN** the door reminder is due; with it open, or on 1 Dec, 25 Dec or 30 Nov, none
- **WHEN** the next slot is asked from 18 Oct, 25 Nov 10:00, 30 Nov 23:00, 1 Dec 10:00, 17 Dec 18:00, 18 Dec 10:00, 24 Dec 18:00 and 25 Dec 10:00 **THEN** the answers are 25 Nov morning, 1 Dec morning, 1 Dec morning, 2 Dec evening, 18 Dec morning, 18 Dec evening, 25 Dec morning and 25 Nov 2027 morning
- *Verifies:* `milestonesAndBigDaysInTheMorning`, `doorReminderOnlyWhenItIsStillClosed`,
  `nextSlotWalksTheSeasonAndWrapsToNextYear`
