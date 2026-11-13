# Capability: moderation-and-privacy

Word filter on public polls, reporting and hiding a creator, tidy names, and "delete my
data". The server-side enforcement (three reports hide a poll, names blanked) is in
lillorepo's `poll-rules`.

- **Source:** `apps/group-polls/domain/` (the word filter, names),
  `apps/group-polls/app/` (`PollsViewModel`)
- **Verified by:** `apps/group-polls/domain/src/test/` (`DomainTest`),
  `apps/group-polls/app/src/test/` (`PollsViewModelTest`)

---

### Requirement: The blocklist matches whole words, ignoring case and accents, on public polls

A public poll's text SHALL be checked against a blocklist that matches whole words
ignoring case and accents: "Qué MIERDA de pregunta" and "cocaína" are blocked, while
"Sexto piso o ático" ("sex" inside "sexto") and "computadora" ("puta" inside a word) are
not. The same text SHALL be accepted as a private poll, which the filter does not touch.

#### Scenario: the word filter
- **WHEN** the blocklist is applied to those four texts **THEN** the first two are blocked and the last two are not
- **WHEN** a public draft with a blocked word is validated **THEN** it has the blocked-words problem; the same draft as private has no problems
- *Verifies:* `blocklistMatchesWholeWordsIgnoringCaseAndAccents`, `onlyPublicPollsAreFiltered`

### Requirement: Reporting a creator hides their polls from the feed

Reporting a poll with "hide creator" SHALL remove every poll of that creator from the
feed and remember the poll's code as reported.

#### Scenario: hiding a creator
- **WHEN** the first poll of the feed is reported with the creator hidden **THEN** no poll by that creator remains in the feed and the code is in the reported list
- *Verifies:* `reportingAndHidingACreatorTakesTheirPollsOutOfTheFeed`

### Requirement: Names are tidied, and deleting my data forgets everything here

A name SHALL have its whitespace collapsed and trimmed and be cut to `Limits.NAME_MAX`.
"Delete my data" SHALL reset the local state to its initial value, including the
accepted terms, and call its completion.

#### Scenario: names and deletion
- **WHEN** "  Jorge    Lillo " is tidied **THEN** it is "Jorge Lillo", and 50 characters are cut to `NAME_MAX`
- **WHEN** a user with a name, accepted terms and a poll deletes their data **THEN** the completion runs, the local state equals a fresh one and the terms are no longer accepted
- *Verifies:* `namesAreTidied`, `deletingMyDataForgetsEverythingHere`

### Requirement: The terms are asked before the first public poll

Creating a public poll before the terms are accepted SHALL show them first; accepting SHALL
record it and create that poll in the same step. A private poll SHALL ask nothing.

#### Scenario: terms
- **WHEN** a valid public poll is submitted before the terms are accepted **THEN** nothing is created until "Accept", which records the acceptance and creates the public poll once, with no second dialog
- **WHEN** a valid private poll is submitted **THEN** it is created with no terms asked
- *Verifies:* `aPublicPollAsksForTheTermsOnceThenIsCreated`, `aPrivatePollAsksNothing`

> **Tested elsewhere.** That reporting a poll *three times* hides it for everyone, and the
> blanking of the user's name on the server with their votes still counted, are server
> behaviours tested in lillorepo (`poll-rules`), not here.
