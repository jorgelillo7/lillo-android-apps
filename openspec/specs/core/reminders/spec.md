# Capability: reminders

Reminder chains and notifications every app shares: one pending job per name, at a given
time, that does its work and plans the next one.

- **Source:** `core/reminders/` (`LilloSchedule`, `LilloNotifications`)
- **Verified by:** nothing in this module; the apps' own rules are tested where they live
  (`christmas-countdown/carols-and-reminders`)

---

### Requirement: Scheduling again replaces the pending job

Scheduling a name SHALL replace its pending job, so apps re-plan on every start and an
update, a time change and the first run all converge on the same state. The job SHALL
survive a reboot, and its time is approximate (Android may delay it to save battery).
Each notification channel SHALL be switchable on its own, and Android 13 and later SHALL
ask for the permission while earlier versions allow notifications.

> **GAP — unverified.** This module has no tests: the replace-on-reschedule behaviour,
> the permission check by Android version, and channel creation are untested. A test
> would use WorkManager's test driver to schedule a name twice and assert one pending job.
