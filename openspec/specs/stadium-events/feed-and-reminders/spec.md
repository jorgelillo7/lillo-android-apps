# Capability: feed-and-reminders

The app downloads one public file of events, keeps the last copy if the download fails,
reads it tolerantly, and asks about notifications once.

- **Source:** `apps/stadium-events/domain/` (`Feed`),
  `apps/stadium-events/app/` (`JaleoViewModel`); the feed itself is built daily by the
  `jorgelillo7.github.io` repository
- **Verified by:** `apps/stadium-events/domain/src/test/` (`JaleoTest`),
  `apps/stadium-events/app/src/test/` (`JaleoViewModelTest`)

---

### Requirement: The feed is read tolerantly

The feed SHALL ignore unknown fields (at the top level and on events), map an unknown
`kind` to `OTHER` and an unknown `level` to `BUSY`, and read dates, so a feed written for a
newer app does not break an older one.

#### Scenario: reading
- **WHEN** a feed has a future field, an unknown event field, kind "festival" and level "huge" **THEN** it parses, the kind is OTHER, the level BUSY and the date is read
- *Verifies:* `theFeedIgnoresUnknownFieldsAndValues`

### Requirement: A failed download keeps the last copy; a recent copy is not downloaded again

The first open SHALL download the events, answer tomorrow, list the upcoming events and
store the fetch time. A failed download SHALL keep the saved copy and its fetch time and
mark the state offline. Opening SHALL download again only a copy older than three hours
(`STALE_MILLIS`); an explicit refresh SHALL always download.

#### Scenario: downloading
- **WHEN** the app opens with nothing saved **THEN** it answers busy with the Viking match, lists Betis, the Champions League final and Karol G, and stores the fetch time
- **WHEN** the download throws and a copy from five hours ago is saved **THEN** the state is offline, tomorrow is still answered and the fetch time is unchanged
- **WHEN** the copy is an hour old **THEN** opening downloads nothing, and refreshing downloads once and updates the fetch time
- **WHEN** the copy is just over three hours old (`STALE_MILLIS`) **THEN** opening downloads it again once
- *Verifies:* `firstOpenDownloadsTheEventsAndAnswersTomorrow`,
  `aFailedDownloadKeepsTheLastCopyAndSaysSo`, `aRecentCopyIsNotDownloadedAgain`,
  `aCopyOverThreeHoursOldIsDownloadedAgain`

### Requirement: The notifications question is asked once

#### Scenario: permission
- **WHEN** the question has not been asked and the app records that it was **THEN** the state starts false and is stored true
- *Verifies:* `theNotificationsQuestionIsAskedOnce`

### Requirement: One reminder job every evening

The app SHALL keep one job for 19:00 local time every evening, planned from the first
launch. The job SHALL notify only when tomorrow is busy, stay silent when it runs on a
later day than planned, and always plan the next evening.

#### Scenario: The job is planned for seven in the evening

- **WHEN** it is before 19:00 **THEN** the job is planned for today at 19:00; **WHEN** it is 19:00 or later **THEN** for tomorrow; **WHEN** it is planned again **THEN** it replaces the pending one
- *Verifies:* `EveningSchedulerTest.theEveningIsSevenLocalAndNextRunIsTodayOrTomorrowAccordingly`, `EveningSchedulerTest.theJobWaitsUntilSevenInTheEveningAndNoLonger`, `EveningSchedulerTest.anEveningAlreadyPastPlansTomorrowsInstead`, `EveningSchedulerTest.planningAgainReplacesTheJobInsteadOfStackingAnother`

#### Scenario: The evening before a busy day

- **WHEN** the job runs on its planned day and tomorrow is busy **THEN** one notification is shown and the next evening is planned; **WHEN** it runs a day late **THEN** nothing is shown and the next evening is still planned
- *Verifies:* `EveningWorkerBusyTest.theEveningBeforeABusyDayTheReminderIsShownOnceThenTheNextEveningIsPlannedAndALateRunIsSilent`

#### Scenario: A quiet tomorrow

- **WHEN** the job runs and tomorrow is quiet **THEN** nothing is shown and the next evening is planned
- *Verifies:* `EveningWorkerCalmTest.aQuietTomorrowShowsNothingButTheChainGoesOn`
