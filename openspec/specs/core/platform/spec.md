# Capability: platform

What every app shares to keep a small JSON document and to receive a shared link or text.

- **Source:** `core/platform/` (`JsonStore`, `Sharing`, `LilloLinks`)
- **Verified by:** `core/platform/src/test/` (`JsonStoreTest`, `SharedTextTest`)

---

### Requirement: A JSON store starts from its default and reads back what it wrote

`JsonStore` SHALL start from the default value when nothing is stored, SHALL apply
successive updates in order and read them back, and `updateAndGet` SHALL apply the edit and
return the value the edit computed alongside the new state.

#### Scenario: the store
- **WHEN** a store with nothing saved is read **THEN** its state is the default
- **WHEN** "milk" and then "eggs" are appended by two updates **THEN** the state holds milk then eggs
- **WHEN** `updateAndGet` replaces the items with two and returns their count **THEN** it returns 2 and the state holds those two items
- **WHEN** a state is written, its store's DataStore is shut down, and a new one opens the same file **THEN** the new store reads that state
- *Verifies:* `startsFromTheDefault`, `anUpdateIsReadBack`,
  `updateAndGetReturnsWhatTheEditComputed`, `theStateSurvivesARestart`

### Requirement: Shared text and opened links are told apart

`sharedText()` SHALL return the URL of a VIEW intent, the text of a plain-text SEND intent,
and nothing for a MAIN intent or no intent.

#### Scenario: what launched the app
- **WHEN** a VIEW intent for a t-page link arrives **THEN** the link is returned
- **WHEN** a SEND intent of `text/plain` carries "a pack" **THEN** "a pack" is returned
- **WHEN** the intent is MAIN or null **THEN** nothing is returned
- *Verifies:* `anOpenedLink`, `textSharedToTheApp`, `aPlainLaunchHasNothing`
