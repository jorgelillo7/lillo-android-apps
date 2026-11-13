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

### Requirement: Motion honours "Remove animations"; About and language do what they say

`animationsEnabled()` SHALL be false when the phone's animator duration scale is 0 and true
otherwise, and `ConfettiBurst` and `ConfettiRain` SHALL draw nothing while it is false. The
About sheet SHALL open the privacy policy, SHALL offer "Rate" only when the app has a Play
Store link, and SHALL offer the language choice; the language dialog SHALL set the app's
language, or clear it back to the phone's, and close.

#### Scenario: behaviour
- **WHEN** "Remove animations" is on, or off **THEN** `animationsEnabled()` is false, or true
- **WHEN** a burst and a rain of confetti start with "Remove animations" on **THEN** neither is drawn; with it off both are
- **WHEN** the About sheet of an unpublished app is shown and "Privacy" is tapped **THEN** there is no "Rate", the language button is there, and the privacy URL is opened; a published app's "Rate" opens its Play Store link
- **WHEN** "Español" and then "System" are picked in the language dialog **THEN** the app's locales are `es` and then empty, and the dialog closes
- *Verifies:* `animationsFollowThePhonesSetting`, `animationsAreOnByDefault`,
  `noConfettiWithAnimationsRemoved`, `confettiWithAnimationsOn`,
  `theAboutSheetOpensThePrivacyPolicyAndRatesOnlyOncePublished`, `aPublishedAppOffersToRateIt`,
  `theLanguageDialogSetsTheAppsLanguageOrGoesBackToThePhones`
