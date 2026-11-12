# Capability: sharing-and-stats

A tournament travels to another phone inside a link or QR code with no server, the
library merges what comes back, and the hall of fame and CSV summarise past tournaments.

- **Source:** `apps/tournaments/domain/` (`Share`, `Library`, `Stats`),
  `apps/tournaments/app/` (`TournamentsViewModel`), `core/sharelink`
- **Verified by:** `apps/tournaments/domain/src/test/` (`ShareTest`,
  `LayoutStatsLibraryTest`), `apps/tournaments/app/src/test/` (`TournamentsViewModelTest`)

---

### Requirement: The whole tournament fits a link and a QR code

A tournament SHALL be encodable into a link `https://jorgelillo7.github.io/t/#1…` whose
data is never sent to a server, decodable back to an equal tournament from the link, the
bare code, or the website's `?d=` form. A link for a played tournament SHALL be under 1,000
characters, and a played 64-player knockout SHALL stay under `Share.QR_MAX_CHARS`. Empty
text, text that is not valid base64, a truncated code and a link to another site SHALL
decode to nothing.

#### Scenario: the codec
- **WHEN** a played tournament is encoded and decoded **THEN** it is equal
- **WHEN** its link is made **THEN** it is under 1,000 characters, and a played 64-player knockout stays under `QR_MAX_CHARS`
- **WHEN** the code is taken from the link, from the bare code and from a `?d=` website link **THEN** each decodes to the same tournament
- **WHEN** the text is empty, `1notbase64!!`, a code with its last 20 characters dropped, or an `example.com` link **THEN** the result is null
- *Verifies:* `roundTrip`, `smallEnoughForAQrCode`, `sixtyFourPlayersStillFitAQrCode`,
  `linkAndBareCode`, `garbageIsNull`

### Requirement: Importing keeps the newest copy

A tournament arriving from a link SHALL be added if new, SHALL replace the local copy only
when its `revision` (bumped on every change) is higher, SHALL be reported as already up to
date when equal and as "newer here" when older. Opening the same link twice SHALL
update, not duplicate, and a broken link SHALL produce an event with no result and no id.
New ids SHALL be 10 characters and distinct, a change to a stored tournament SHALL apply
to it, a change to a deleted one SHALL be ignored, and delete SHALL remove it.

#### Scenario: importing
- **WHEN** a message containing a link is imported, then the same link, then the same tournament with a higher revision **THEN** the results are ADDED, ALREADY_UP_TO_DATE and UPDATED, all for the same id
- **WHEN** the link text is not a tournament **THEN** the event has no result and no id
- **WHEN** a tournament is added, updated, imported again older and imported again equal **THEN** the results are ADDED, UPDATED, NEWER_HERE and ALREADY_UP_TO_DATE
- **WHEN** 50 ids are minted **THEN** all are 10 characters and distinct
- **WHEN** a stored tournament is renamed and its game changed **THEN** both changes are stored; changing a missing one does nothing and deleting empties the library
- *Verifies:* `aSharedLinkIsImportedOnceThenUpdated`, `aBrokenLinkSaysSo`,
  `importKeepsTheNewestCopy`, `newIdsAreShortAndDistinct`, `aChangeAppliesToTheStoredCopy`

### Requirement: Stats match players by name, and every match exports as CSV

Across tournaments a player SHALL be matched by name ignoring case and surrounding
spaces (ids are per tournament), and stats SHALL be filterable by game, ignoring case. The
CSV SHALL carry one row per match with a field holding a comma quoted and an inner quote
doubled.

#### Scenario: stats and CSV
- **WHEN** " pepe " in one tournament and "Pepe" in another are combined with three other names **THEN** there are four players, Pepe has two tournaments, the titles add up to two, and filtering by "pokémon" in lower case leaves one title
- **WHEN** a tournament named `Cup, 2026` has a comment `20 vidas a 0, "fácil"` **THEN** the first data row starts with `"Cup, 2026"` and ends with the comment quoted with its inner quotes doubled
- *Verifies:* `statsMatchPlayersByName`, `csvQuotesCommas`

> **GAP — unverified.** The ranking order (titles, then finals, then match win rate) and
> the finals and win-rate columns are not asserted; `statsMatchPlayersByName` only checks
> the first row has a title. A test would build players with equal titles and different
> finals and assert the order.
