# Capability: polls

Red-against-blue polls between friends (private, by link, results with names) and for
everyone (public feed, percentages only): creating, voting once, predicting, closing and
the feed. The security rules that enforce the same invariants on the server live in
lillorepo (`openspec/specs/group_polls/poll-rules`).

- **Source:** `apps/group-polls/domain/` (the rules of a poll),
  `apps/group-polls/app/` (`PollsViewModel`)
- **Verified by:** `apps/group-polls/domain/src/test/` (`DomainTest`),
  `apps/group-polls/app/src/test/` (`PollsViewModelTest`, `PollFlowTest`)

---

### Requirement: A poll opens for its duration and drafts are validated

A poll's duration SHALL set its closing time: one day closes 24 hours after creation and
no limit has none. A poll SHALL be open strictly before its closing time and always open
without one. A draft SHALL be reported with the problems it has: a question that is too
short, answers that are the same ignoring case and accents, an empty answer, an answer
longer than 30 characters; a valid draft has none. A created poll SHALL carry the
creator's trimmed name and language and be remembered locally, and a failed creation
SHALL show the backend's message while the creating flag returns to false. Vote shares
SHALL be integers that add up to 100 (67/33 for 2 to 1, 50/50 for a tie, 0/0 for no votes).

The same limits are enforced by the server's rules; checking them here is what lets the
user be told why, instead of getting a refused write.

#### Scenario: creating
- **WHEN** a one-day and a no-limit duration close at time 0 **THEN** the first closes at 24 h and the second never; a poll closing at 10 is open at 9 and not at 10, and one with no closing is open at any time
- **WHEN** drafts are validated **THEN** "¿Playa o montaña?" with "Playa" and "Montaña" has no problems, "¿Sí?" is too short, "Playa" and " PLAYÁ " are the same, a blank answer is empty, and a 31-character answer is too long
- **WHEN** "  Ana  " is the name and a poll is created **THEN** the stored poll has creator name "Ana", language "en" and no closing time, and the code is in the user's polls
- **WHEN** creation throws "offline" **THEN** the creating flag goes true then false, nothing is created and the error "offline" is shown until cleared
- **WHEN** votes are 0–0, 2–1, 1–2 and 5–5 **THEN** the shares are 0/0, 67/33, a pair adding to 100, and 50/50
- *Verifies:* `durationSetsTheClosingTime`, `draftsAreValidated`,
  `aCreatedPollCarriesTheNameAndIsRemembered`, `aFailedCreateShowsTheError`,
  `splitAlwaysAddsUpTo100`

### Requirement: A vote is final, remembered, and a prediction counts only once the poll is decided

A vote SHALL be remembered with its prediction and counted once on the backend; voting
again SHALL fail with "already voted". A prediction SHALL be resolved only on a poll that
is closed and has a winner: not while open, not when it never closes, not on a tie; then
it counts once. An opened link SHALL be ignored when it holds no code, and otherwise its
code SHALL be shown once (until consumed) and remembered; invite codes SHALL be distinct
and valid (200 generated, all different), and be read from a message with a link or from
the bare code with spaces, and a too-short code SHALL read as nothing.

#### Scenario: voting and predicting
- **WHEN** a vote of red with a blue prediction is cast **THEN** it is remembered as that vote and the backend has one red vote; voting blue afterwards fails with "already voted"
- **WHEN** a prediction is resolved on a still-open poll, then the poll is closed and resolved again **THEN** only the second counts, as one resolved prediction
- **WHEN** predictions meet an open poll, a never-closing one or a tie **THEN** nothing changes; on a closed poll with a winner they are added to the record
- **WHEN** the text "see you there" and then a q-page link with code `abcdefghijkm` are opened **THEN** only the code is shown and remembered, and it clears once consumed
- **WHEN** 200 codes are generated **THEN** all are distinct and valid; a message with a link and a code with spaces both give the code, and `?c=short` gives nothing
- *Verifies:* `aVoteIsRememberedAndCannotBeCastTwice`,
  `predictionsResolveOnlyOnClosedPollsWithAWinner`,
  `aPredictionCountsOnceTheClosedPollHasAWinner`, `anOpenedLinkIsShownAndRemembered`,
  `inviteCodesAreLongRandomAndParsedFromLinks`

### Requirement: The feed shows last week's public, unhidden polls

A poll SHALL be in the feed when it is public, not hidden and less than seven days old:
at six days yes, at seven no, a hidden one no, a private one no.

#### Scenario: the feed
- **WHEN** a public poll is checked at 6 and 7 days **THEN** it is in the feed at 6 and not at 7
- **WHEN** a hidden poll, or a private one, is checked at time 0 **THEN** neither is in the feed
- *Verifies:* `feedShowsLastWeekPublicUnhiddenPolls`

### Requirement: A user can vote, create, vote on their own poll and close it

#### Scenario: the flow
- **WHEN** a user named Alex votes red with a blue prediction on a public poll **THEN** the prediction blue is stored
- **WHEN** the user creates a private poll "Friday dinner?" with Pizza and Sushi **THEN** it is created under Alex's name, then voted blue with a blue prediction, and closing it (with confirmation) gives it a closing time
- *Verifies:* `voteOnAPublicPollThenCreateAndCloseAPrivateOne`
