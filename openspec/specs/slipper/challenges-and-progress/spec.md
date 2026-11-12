# Capability: challenges-and-progress

A run of the day, challenge links that replay a friend's seed and score, croquetas that
unlock skins, and the best score and totals kept on the phone.

- **Source:** `apps/slipper/domain/` (`Challenges`, `Sprites`),
  `apps/slipper/app/` (`SlipperViewModel`, the `JsonStore`), `core/sharelink`
- **Verified by:** `apps/slipper/domain/src/test/` (`EngineTest`),
  `apps/slipper/app/src/test/` (`SlipperViewModelTest`, `SlipperFlowTest`)

---

### Requirement: A challenge travels in a link and carries the game version

A challenge (seed and score) SHALL travel in a link starting `https://jorgelillo7.github.io/c/#`,
read back equal from a message holding it and as nothing from text without one. A link
SHALL carry the game version (`GameConfig.VERSION`): a link written now holds it, one
written for version 1 reads back as version 1 and as not the same run (`sameRun` false),
and a current one as the same run.

#### Scenario: the link
- **WHEN** a challenge of seed 123456789 and score 1240 is linked **THEN** the link has the prefix and reads back equal from "¿Me ganas? <link>"; "no link here" reads as nothing
- **WHEN** a link made for version 1 is read **THEN** its version is 1 and it is not the same run; a current one is
- **WHEN** a link is made now **THEN** its decoded payload contains `"v":2` (the current `VERSION`)
- **WHEN** the app imports a challenge text **THEN** the challenge is set and its seed is the next one until dismissed, after which the previous seed returns
- **WHEN** it imports text without a challenge **THEN** the invalid-link flag is set until dismissed
- **WHEN** a challenge link is made for seed 5 and score 321 **THEN** it reads back as that challenge
- *Verifies:* `aChallengeTravelsInALink`, `aChallengeLinkCarriesTheGameVersion`,
  `aLinkMadeNowWritesItsVersionToo`, `aChallengeLinkSetsTheSeedUntilDismissed`,
  `textWithoutAChallengeIsReported`, `theChallengeLinkCarriesSeedAndScore`

### Requirement: The run of the day is the same all day

The daily seed SHALL be the same for the whole day and today's best SHALL be kept from
the best of the day's runs, starting from zero on a new day.

#### Scenario: the daily run
- **WHEN** two daily runs score 300 and 200 **THEN** today's best is 300, and on the next day it reads 0 and the seed differs from the previous day's
- *Verifies:* `theRunOfTheDayIsTheSameAllDayAndKeepsTodaysBest`

### Requirement: Progress is kept, and croquetas unlock skins

A finished run SHALL keep the best score and add its croquetas to the totals, counting
runs. A locked skin SHALL be ignored when chosen; finishing a run with enough croquetas
(`unlockAt`) SHALL unlock it and then it can be chosen. The sound SHALL toggle to muted.
The pixel-art sprites file SHALL parse and contain the named sprites, a 12 × 16 kid.

#### Scenario: progress
- **WHEN** runs of 500 (3 croquetas) and 200 (2 croquetas) finish **THEN** the saved best is 500, runs 2 and croquetas 5
- **WHEN** a locked skin is chosen **THEN** it is ignored; finishing with its `unlockAt` croquetas reports it unlocked, and then choosing it sticks
- **WHEN** the sound is toggled **THEN** muted is stored
- **WHEN** the sprites file is parsed **THEN** kid, grandma, slipper and croqueta sprites exist and the first kid frame is 12 × 16
- *Verifies:* `aRunKeepsTheBestScoreAndAddsUpTotals`,
  `croquetasUnlockSkinsAndOnlyUnlockedOnesCanBeChosen`, `theSoundToggles`,
  `theSpritesFileParses`
