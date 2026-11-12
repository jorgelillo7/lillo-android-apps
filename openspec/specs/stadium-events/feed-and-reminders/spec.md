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
mark the state offline. A copy fetched an hour ago SHALL NOT be downloaded on opening; an
explicit refresh SHALL download it.

#### Scenario: downloading
- **WHEN** the app opens with nothing saved **THEN** it answers busy with the Viking match, lists Betis, the Champions League final and Karol G, and stores the fetch time
- **WHEN** the download throws and a copy from five hours ago is saved **THEN** the state is offline, tomorrow is still answered and the fetch time is unchanged
- **WHEN** the copy is an hour old **THEN** opening downloads nothing, and refreshing downloads once and updates the fetch time
- *Verifies:* `firstOpenDownloadsTheEventsAndAnswersTomorrow`,
  `aFailedDownloadKeepsTheLastCopyAndSaysSo`, `aRecentCopyIsNotDownloadedAgain`

### Requirement: The notifications question is asked once

#### Scenario: permission
- **WHEN** the question has not been asked and the app records that it was **THEN** the state starts false and is stored true
- *Verifies:* `theNotificationsQuestionIsAskedOnce`

> **GAP — unverified.** The staleness threshold for downloading again (between one hour
> and five hours old) is not pinned: the tests use one hour (not downloaded) and five
> hours (offline copy kept) but no test asserts that a five-hour-old copy *is* downloaded
> when the network works. Likewise the evening-before reminder at 19:00, its scheduling and
> its being on from the first launch have no test.
