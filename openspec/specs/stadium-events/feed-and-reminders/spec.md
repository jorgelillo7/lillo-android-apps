# Capability: feed-and-reminders

The app downloads one public file of events, keeps the last copy if the download fails,
reads it tolerantly, asks about notifications once, and reminds the evening before a busy
day, or that morning when the evening's did not arrive.

- **Source:** `apps/stadium-events/domain/` (`Feed`, `Jaleo`),
  `apps/stadium-events/app/` (`JaleoViewModel`, `evening/`); the feed itself is built daily by the
  `jorgelillo7.github.io` repository
- **Verified by:** `apps/stadium-events/domain/src/test/` (`JaleoTest`),
  `apps/stadium-events/app/src/test/` (`JaleoViewModelTest`, `EveningSchedulerTest`,
  `EveningChainTest`, `EveningWorkerBusyTest`, `MorningWorkerTest`, `NoPermissionWorkerTest`,
  `EveningWorkerCalmTest`, `StoredStateTest`)

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

### Requirement: One reminder job, two slots

The app SHALL keep one job, at the next 09:00 or 19:00 local time (`Jaleo.nextSlot`), planned
from the first launch, kept by the app's start and moved on by the job itself. Each run SHALL
decide from the clock and the day last reminded (`remindedFor`, stored with the events), never
from when the job was planned, so a late run still does the right thing:

- from 19:00, it SHALL remind about tomorrow when tomorrow is busy ("Mañana, domingo 15, hay
  jaleo en el Metropolitano");
- from 09:00 to 18:59, it SHALL remind about today when today is busy and no reminder was shown
  for today, unless the event has already started ("Hoy hay jaleo en el Metropolitano"): the
  catch-up when the evening's did not arrive (phone off, app installed that morning);
- between 00:00 and 08:59 it SHALL never sound;
- a day whose event is not fixed (`either`) SHALL keep the "puede haber jaleo" wording in both;
- a reminder SHALL count as given (`remindedFor`) only when it was shown, and both go on the
  same channel with the same id, so the morning's replaces a stale "Mañana".

An elderly neighbour whose phone was off the evening before still needs to know before leaving
home; a reminder at night, or twice for one day, would teach them to silence the app.

#### Scenario: The job runs at nine and at seven

- **WHEN** it is before 09:00, before 19:00 or later **THEN** the next run is 09:00 today, 19:00 today or 09:00 tomorrow; **WHEN** it is planned again **THEN** it replaces the pending one
- *Verifies:* `JaleoTest.theJobRunsAtNineAndAtSeven`, `EveningSchedulerTest.theJobRunsAtTheNextNineOrSeven`, `EveningSchedulerTest.theJobWaitsUntilSevenInTheEveningAndNoLonger`, `EveningSchedulerTest.anEveningAlreadyPastPlansTomorrowMorningInstead`, `EveningSchedulerTest.planningAgainReplacesTheJobInsteadOfStackingAnother`

#### Scenario: The reminder fires with the app closed

- **WHEN** the job is due and the app starts a minute later (WorkManager started its process to run the job) **THEN** that job is kept; **WHEN** the job planned at start runs through WorkManager **THEN** it plans the next one
- *Verifies:* `EveningSchedulerTest.theAppsStartKeepsTheEveningJobThatIsDue`, `EveningChainTest.theJobPlannedAtStartRunsAndPlansTheNextOne`

#### Scenario: The evening before a busy day

- **WHEN** the job runs at 19:00 or later the evening before **THEN** it reminds about tomorrow once, records the day, and a second evening run, the night and the next morning stay silent; **WHEN** tomorrow is calm **THEN** nothing is shown
- *Verifies:* `JaleoTest.theEveningBeforeABusyDayRemindsTomorrowOnce`, `EveningWorkerBusyTest.theEveningBeforeABusyDayRemindsOnceAndTheMorningAfterStaysSilent`

#### Scenario: The morning of a busy day catches up

- **WHEN** the job runs from 09:00 on a busy day with nothing shown for it **THEN** it reminds about today once; **WHEN** the evening already did, or the day is calm **THEN** nothing is shown
- *Verifies:* `JaleoTest.theMorningOfABusyDayCatchesUpOnlyWhenTheEveningSaidNothing`, `MorningWorkerTest.theMorningOfABusyDayRemindsWhenTheEveningSaidNothingAndOnlyOnce`

#### Scenario: Never at night, never after the start

- **WHEN** the job runs between 00:00 and 08:59 **THEN** nothing is shown; **WHEN** it runs after the event's start **THEN** nothing is shown; **WHEN** the event has no time **THEN** the morning may remind until 19:00
- *Verifies:* `JaleoTest.nothingSoundsAtNightOrAfterTheStart`

#### Scenario: An unfixed day stays a maybe

- **WHEN** the day is one of an unfixed match's days **THEN** both reminders are maybes and their titles say "puede haber jaleo"
- *Verifies:* `JaleoTest.anUnfixedDayRemindsAsAMaybeInTheMorningToo`, `JaleoTextsTest.anUnfixedDayKeepsTheMaybeInBothTitles`

#### Scenario: Only a shown reminder counts

- **WHEN** the permission is missing **THEN** nothing is recorded as reminded
- *Verifies:* `NoPermissionWorkerTest.aReminderThatCouldNotBeShownIsNotMarkedAsGiven`

#### Scenario: A quiet tomorrow

- **WHEN** the job runs and today and tomorrow are quiet **THEN** nothing is shown and the next run is planned
- *Verifies:* `EveningWorkerCalmTest.aQuietTomorrowShowsNothingButTheChainGoesOn`

### Requirement: What the released app stored still reads

The state a released version wrote (the feed, the fetch time, the permission question) SHALL
keep reading, with no day reminded yet, and the day reminded SHALL be stored as an ISO date.

#### Scenario: stored state
- **WHEN** 1.1's state is read **THEN** it decodes unchanged; **WHEN** a reminded day is stored **THEN** it reads back as that date
- *Verifies:* `StoredStateTest.theStateTheReleasedAppWritesStillReads`, `StoredStateTest.theDayLastRemindedIsStoredAsADate`
