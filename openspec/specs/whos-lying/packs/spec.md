# Capability: packs

The word packs the game draws from: 21 built-in packs per language, the user's own, and
sharing a pack by link, with a picker that avoids repeating words.

- **Source:** `apps/whos-lying/domain/` (word picker, pack parsing, `PackShare`),
  `apps/whos-lying/app/` (`GameViewModel` pack handling),
  `core/sharelink` (the link and QR limits)
- **Verified by:** `apps/whos-lying/domain/src/test/` (`GameTest`, `PackShareTest`),
  `apps/whos-lying/app/src/test/` (`GameViewModelTest`)

---

### Requirement: Words do not repeat until most were played

The picker SHALL avoid recently played words: with 100 words in the chosen packs, none
repeats over the first 90 games, leaving 10 unplayed (`FRESH_SHARE` = 10 % of the pool
kept fresh), and afterwards the oldest words come back first, never one of the 89 most
recent. Recent words are remembered up to `RECENT_MEMORY` = 3000. Choosing packs SHALL
keep the subset chosen, ignoring unknown ids, and choosing every built-in pack SHALL be
stored as "all" (an empty set).

A repeated word in the first rounds would be the one complaint players make.

#### Scenario: the picker
- **WHEN** a pack of two words has one of them recent **THEN** the other is picked
- **WHEN** 90 games are played from a pool of 100 **THEN** no word repeats and 10 of 100 are unplayed; the next 50 games never pick one of the 89 most recent
- **WHEN** one pack and an unknown id are chosen **THEN** only the known pack is kept; choosing all built-in packs stores the empty set
- *Verifies:* `pickerAvoidsRecentWords`, `noWordRepeatsUntilAlmostAllWerePlayed`,
  `choosingPacksKeepsASubsetAndEveryPackMeansAll`

### Requirement: A word can have several similar words, and the built-in packs are well formed

A pack line SHALL be `word / similar / similar…`, similar words optional: duplicates and
a repeat of the word itself are dropped, blank lines ignored, and a line can be written
back. The impostor SHALL get one of the similar words, varying with the seed. Each
language SHALL have 21 built-in packs with unique ids, at least 30 words per pack, exactly
two similar words per word (never equal to it once normalised), no repeated words, and the
same packs in the same order in both languages. An imported text with no pack SHALL import
as nothing.

#### Scenario: parsing and built-ins
- **WHEN** `"Playa / Piscina / lago / Playa"`, a blank line, `"  Cine  "` and `"playa / Mar"` are parsed **THEN** the entries are Playa with Piscina and lago, and Cine, and the line writes back as `Playa / Piscina / lago`
- **WHEN** 60 seeded classic games are dealt for a word with three similar words **THEN** the impostor's word takes each of the three at some point
- **WHEN** the built-in packs are checked in Spanish and English **THEN** the rules above hold
- **WHEN** text without a pack is imported **THEN** the result is null
- *Verifies:* `parsesSeveralSimilarWordsPerLine`, `impostorGetsOneOfSeveralSimilarWords`,
  `builtInPacksAreWellFormed`, `importPackIgnoresTextWithoutAPack`

### Requirement: A pack travels in a short link

A pack SHALL be shareable as a link `https://jorgelillo7.github.io/p/#1…` shorter than 300
characters, read back equal even from a chat message that holds it. A link SHALL be
ignored when its pack has fewer than `PackShare.MIN_WORDS` = 3 words, a blank name, when
it belongs to another page (the tournaments page), or when the text is not a link. A pack
SHALL be offered for sending only when this app reads its own link back; otherwise the share
sheet SHALL say it is too big (at most `PackShare.MAX_WORDS` words, short texts) and offer
neither the link nor a QR code.

#### Scenario: sharing
- **WHEN** a pack is shared **THEN** the link starts with the p-page prefix, is under 300 characters and the pack is read back from a message containing it
- **WHEN** a link holds two words or a blank name, points at the tournaments page, or the text is "hola" **THEN** nothing is read
- **WHEN** any built-in pack is shared **THEN** its link fits `PackShare.QR_MAX_CHARS`, so the share sheet shows its QR code
- **WHEN** a pack with the most words a link may carry (500) is shared **THEN** its link is over the QR limit (the sheet offers the link alone) and still reads back whole
- **WHEN** a pack has 501 words, a 41-character name, or so many long words and decoys that its code passes the codec's limit **THEN** no link is offered and the share sheet shows the too-big note, with no send button and no QR; every built-in pack and a 500-word pack can be sent
- *Verifies:* `aPackTravelsInAShortLink`, `unplayableOrForeignLinksAreIgnored`, `builtInPacksFitInAQrCode`,
  `aFullPackTravelsByLinkOnly`, `onlyAPackTheOtherPhoneAcceptsCanBeSent`, `packShareTooBig`,
  `packShareTooBigLandscape`, `aSendablePackOffersItsLinkAndQr`
