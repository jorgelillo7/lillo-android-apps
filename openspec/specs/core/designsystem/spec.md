# Capability: designsystem

The shared Compose pieces. Only the logic that is not drawing is specified; the look is
frozen by screenshot tests.

- **Source:** `core/designsystem/` (`Avatar`)
- **Verified by:** `core/designsystem/src/test/` (`AvatarInitialTest`,
  `CatalogueScreenshotTest`)

---

### Requirement: An avatar shows one whole character

The initial SHALL be the first character of the trimmed name upper-cased (including
accented letters and ñ), a question mark for an empty or blank name, and a whole emoji
(a ZWJ sequence or a flag included), never half of one.

#### Scenario: initials
- **WHEN** "ana", "  álvaro" and "ñoño" are given **THEN** the initials are "A", "Á" and "Ñ"
- **WHEN** the name is empty or spaces **THEN** the initial is "?"
- **WHEN** the names start with 🦁, the cook emoji sequence or the Spanish flag **THEN** the whole emoji is returned
- *Verifies:* `aLetterUpperCased`, `aBlankNameIsAQuestionMark`,
  `aWholeEmojiNeverHalfOfOne`

> **GAP — unverified.** Motion, confetti, the About sheet and the language helper have
> no behaviour tests, only the catalogue screenshots.
