# Capability: reminders

Reminder chains and notifications every app shares: one pending job per name, at a given
time, that does its work and plans the next one.

- **Source:** `core/reminders/` (`LilloSchedule`, `LilloNotifications`)
- **Verified by:** `LilloScheduleTest`, `LilloNotificationsTest`

---

### Requirement: Scheduling again replaces the pending job

Scheduling a name SHALL replace its pending job, so apps re-plan on every start and an
update, a time change and the first run all converge on the same state. Each name SHALL
keep its own job.

#### Scenario: A chain planned twice has one pending job

- **WHEN** a name is scheduled and then scheduled again **THEN** exactly one job is pending and the earlier one is cancelled
- *Verifies:* `LilloScheduleTest.schedulingAChainAgainReplacesItsPendingJobAndNeverStacksThem`

#### Scenario: Chains do not disturb each other

- **WHEN** two names are scheduled and one is scheduled again **THEN** the other keeps its job
- *Verifies:* `LilloScheduleTest.eachChainKeepsItsOwnJob`

#### Scenario: The job waits for its time, and a past time runs at once

- **WHEN** a job is scheduled for a later time **THEN** it waits that long; **WHEN** the time has passed **THEN** it runs right away
- *Verifies:* `LilloScheduleTest.aJobWaitsForTheGivenTime`, `LilloScheduleTest.aTimeInThePastRunsRightAway`

### Requirement: Notifications follow the permission and their channel

From Android 13 a notification SHALL need the permission; earlier versions allow it
without asking. Nothing SHALL be shown without it. A notification SHALL go on its channel
and replace the one with the same id. Channels SHALL be created once.

#### Scenario: The permission by Android version

- **WHEN** the permission is denied on Android 13 or later **THEN** the app cannot notify, and can once it is granted; **WHEN** the version is earlier **THEN** it can without asking
- *Verifies:* `LilloNotificationsTest.fromAndroid13NotificationsNeedThePermission`, `LilloNotificationsTest.beforeAndroid13NotificationsAreAllowedWithoutAsking`

#### Scenario: Nothing without the permission

- **WHEN** a notification is shown without the permission **THEN** none appears
- *Verifies:* `LilloNotificationsTest.nothingIsShownWithoutThePermission`

#### Scenario: Channel and id

- **WHEN** a notification is shown and another with the same id follows **THEN** it is on its channel and the second replaces the first
- *Verifies:* `LilloNotificationsTest.aNotificationIsShownOnItsChannelAndANewOneWithTheSameIdReplacesIt`

#### Scenario: Channels are created once

- **WHEN** the app starts several times **THEN** each channel exists once with its name, description and importance; before Android 8 nothing is created and nothing breaks
- *Verifies:* `LilloNotificationsTest.channelsAreCreatedOnceWhateverTheNumberOfStarts`, `LilloNotificationsTest.beforeAndroid8ThereAreNoChannelsToCreateAndNothingBreaks`

> **GAP — unverified.** That a job survives a reboot, and how far Android delays it to
> save battery, are WorkManager's behaviour on a device and have no test here.
