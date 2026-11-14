# Capability: reminders

Reminder chains and notifications every app shares: one pending job per name, at a given
time, that does its work and plans the next one; the app's start only makes sure it exists.

- **Source:** `core/reminders/` (`LilloSchedule`, `LilloNotifications`)
- **Verified by:** `LilloScheduleTest`, `LilloNotificationsTest`

---

### Requirement: The job moves the chain on; the app's start never cancels a due job

Planning from the chain's own job (`LilloSchedule.at`) SHALL replace the pending job, so the
chain moves on, and each name SHALL keep its own job. Planning from the app's start
(`LilloSchedule.ensure`) SHALL keep a job of the same worker that is running, due, or planned
no later than the wanted time (within a minute), and SHALL replace a missing one, one planned
later than wanted (an update added an earlier slot, the clock went back) or one of another
worker (a renamed class could not be built). Apps call `ensure` from `Application.onCreate` on
every start, so an update, a time change and the first run converge on one pending job.

When the job's time comes with the app's process dead, WorkManager starts the process to run it
and `Application.onCreate` runs first: replacing there cancelled the very job the process was
started for, and reminders only fired if the app happened to be alive (seen on the emulator:
`Processor cancelling` right after `onStartJob`).

#### Scenario: A chain planned twice by its job has one pending job

- **WHEN** a name is scheduled and then scheduled again **THEN** exactly one job is pending and the earlier one is cancelled
- *Verifies:* `LilloScheduleTest.schedulingAChainAgainReplacesItsPendingJobAndNeverStacksThem`

#### Scenario: Chains do not disturb each other

- **WHEN** two names are scheduled and one is scheduled again **THEN** the other keeps its job
- *Verifies:* `LilloScheduleTest.eachChainKeepsItsOwnJob`

#### Scenario: The job waits for its time, and a past time runs at once

- **WHEN** a job is scheduled for a later time **THEN** it waits that long; **WHEN** the time has passed **THEN** it runs right away
- *Verifies:* `LilloScheduleTest.aJobWaitsForTheGivenTime`, `LilloScheduleTest.aTimeInThePastRunsRightAway`

#### Scenario: The app's start keeps the job that is due

- **WHEN** a job is due and the app starts, its next time already the following day **THEN** replacing would cancel the due job, and `ensure` keeps it as the only job
- *Verifies:* `LilloScheduleTest.replacingAtStartUpCancelsTheJobThatIsDueWhichIsWhyStartUpEnsuresInstead`, `LilloScheduleTest.startUpKeepsAJobThatIsDue`

#### Scenario: The app's start plans what is missing or wrong, and nothing else

- **WHEN** nothing is planned **THEN** `ensure` plans the wanted time; **WHEN** it runs again for the same time **THEN** the job is kept; **WHEN** the pending job is later than wanted or of another worker **THEN** it is replaced
- *Verifies:* `LilloScheduleTest.startUpKeepsTheJobForTheSameTimeAndPlansAMissingOne`, `LilloScheduleTest.startUpReplacesAJobPlannedLaterThanTheWantedTime`, `LilloScheduleTest.startUpReplacesAJobOfAnotherWorker`

### Requirement: Notifications follow the permission and their channel

From Android 13 a notification SHALL need the permission; earlier versions allow it
without asking. Nothing SHALL be shown without it. A notification SHALL go on its channel
and replace the one with the same id. Channels SHALL be created once.

#### Scenario: The permission by Android version

- **WHEN** the permission is denied on Android 13 or later **THEN** the app cannot notify, and can once it is granted; **WHEN** the version is earlier **THEN** it can without asking
- *Verifies:* `LilloNotificationsTest.fromAndroid13NotificationsNeedThePermission`, `LilloNotificationsTest.beforeAndroid13NotificationsAreAllowedWithoutAsking`

#### Scenario: Nothing without the permission

- **WHEN** a notification is shown without the permission **THEN** none appears and `show` says so (false), so an app records a reminder as given only when it was
- *Verifies:* `LilloNotificationsTest.nothingIsShownWithoutThePermission`

#### Scenario: Channel and id

- **WHEN** a notification is shown and another with the same id follows **THEN** it is on its channel and the second replaces the first
- *Verifies:* `LilloNotificationsTest.aNotificationIsShownOnItsChannelAndANewOneWithTheSameIdReplacesIt`

#### Scenario: Channels are created once

- **WHEN** the app starts several times **THEN** each channel exists once with its name, description and importance; before Android 8 nothing is created and nothing breaks
- *Verifies:* `LilloNotificationsTest.channelsAreCreatedOnceWhateverTheNumberOfStarts`, `LilloNotificationsTest.beforeAndroid8ThereAreNoChannelsToCreateAndNothingBreaks`

> **GAP — unverified.** That a job survives a reboot, and how far Android delays it to
> save battery, are WorkManager's behaviour on a device and have no test here.
