# Summandos: operations

Shared engine commands and setup: [engine/README.md](../README.md). Run from the repo root.

```bash
scripts/engine-assets.sh engine/summandos                   # fetch the 3D models (once; before export)
scripts/engine-test.sh engine/summandos                     # tests
engine/summandos/tools/smoke_test.sh                        # release APK played on the emulator (before an upload)
scripts/godot.sh --path engine/summandos                    # open in the editor
scripts/godot.sh --path engine/summandos -- --mode=stress   # performance test (not in the menu)
scripts/godot.sh --headless --path engine/summandos --script tools/balance.gd   # difficulty curve
.venv/bin/python engine/summandos/tools/generate_icon.py    # regenerate the icons
.venv/bin/python engine/summandos/tools/generate_sounds.py  # regenerate effects and music
.venv/bin/python scripts/engine_icon_preview.py engine/summandos /tmp/icon.png  # check them
scripts/engine-template.sh engine/summandos                 # slim engine (5 min, once per profile change)
scripts/engine-export.sh --arm64 engine/summandos           # APK for a phone (export/, debug key)
```

The Golem and Necromancer come from KayKit Skeletons EXTRA, a bought pack that never goes in this
repo. Its zip is unpacked in `~/Projects/documentation/assets/kaykit-skeletons-extra/`; the 1.1
characters ship without animations, so each gets its rig's clips added once, and models.txt pins
the result by checksum (engine-assets.sh copies it in; a Mac without it skips it and the game
draws the free stand-ins):

```bash
cd ~/Projects/documentation/assets/kaykit-skeletons-extra
E=KayKit_Skeletons_1.1_EXTRA; A=$E/Animations/gltf; T=~/Projects/lillo-android-apps/scripts/glb_add_animations.py
python3 $T $E/characters/gltf/Skeleton_Golem.glb Skeleton_Golem.glb \
  $A/Rig_Large/Rig_Large_MovementBasic.glb:Walking_A $A/Rig_Large/Rig_Large_General.glb:Idle_A
python3 $T $E/characters/gltf/Necromancer.glb Necromancer.glb \
  $A/Rig_Medium/Rig_Medium_MovementBasic.glb:Walking_B $A/Rig_Medium/Rig_Medium_General.glb:Idle_A
```

The weapons (Kenney Blaster Kit, CC0 and free) are committed in `assets/weapons/`: `blaster-a`
(pistol) and `blaster-h` (bazooka) with the kit's shared `Textures/colormap.png`.

Store listing graphics (7 phone and 7 tablet screenshots and the feature graphic, Spanish and
English):

```bash
scripts/engine-assets.sh engine/summandos                   # models, if not fetched yet
engine/summandos/tools/capture_store_screenshots.sh         # store/raw[-tablet]/<locale>/
.venv/bin/python engine/summandos/tools/generate_store_assets.py   # play/src/main/play/listings/
```

Screenshots (`scripts/engine-shot.sh`, add `--landscape` for sideways, `--store` for 1080x2424):

```bash
scripts/engine-shot.sh engine/summandos menu.png 2
scripts/engine-shot.sh --progress engine/summandos/tools/samples/progress-easy.cfg \
  engine/summandos levels.png 2 -- --levels
scripts/engine-shot.sh engine/summandos run.png 2.5 -- --mode=play --autosteer
scripts/engine-shot.sh engine/summandos adult.png 2.5 -- --mode=play --difficulty=adult --level=12
```

Game options after `--`: `--mode=play|stress` starts a run at once, `--difficulty=easy|normal|adult`
and `--level=<n>` pick it, `--autosteer` takes the better gate, `--levels` opens the level map,
`--palette=sunset|night` overrides the saved choice, `--shot=<png> --after=<s>` saves a
screenshot and quits.

## Play Console answers

| Section | Answer |
|---|---|
| Package name | `com.jorgelillo.summandos` (versionCode 1, version 1.0) |
| App or game | Game · category **Casual** · Free · default language Spanish (Spain) |
| Privacy policy | `https://jorgelillo7.github.io/privacy/summandos/` (also linked from the game's About card) |
| App access | All functionality is available without special access |
| Ads | No |
| Content rating | IARC questionnaire, category **Game**. Violence: **yes**, fantasy and cartoon (crossbowmen shoot low-poly skeletons, which fall apart; no blood, no gore, no realistic people); no fear content, sex, nudity, language, drugs, alcohol, tobacco or gambling; no purchases; users can't interact, chat or share content; no location shared. Expected: PEGI 7 / ESRB Everyone 10+ |
| Target audience | 13+ (13–15, 16–17, 18+), as slipper: under-13s bring in the Families policy (more review, its own rules). Appeal to children: say yes only if Play asks about the listing; nothing in it is aimed at children |
| Data safety | **No data collected, no data shared.** No internet permission; the only permission is vibration. Data encrypted in transit: not applicable. Deletion request: not applicable (nothing leaves the phone) |
| Advertising ID | No |
| Government, financial, health, news | No / no financial features / no health features / not a news app |
| Countries | All, like the other apps |
| Store settings | App category **Casual**; tags: up to 5 from Play's list, the closest to casual, arcade, maths and shooting; contact e-mail the developer account's, website `https://jorgelillo7.github.io/` |
| Store listing | `play/src/main/play/listings/<locale>/`: title, short and full description, icon, feature graphic, phone, 7" and 10" tablet screenshots (Spanish and English) |
| Release notes | `play/src/main/play/release-notes/<locale>/default.txt` |

## Release

**First release, straight to production as a draft** (Play only takes a draft until the app has
been published once; the API cannot create the app, so it is all by hand):

1. Upload key, in your own terminal: `scripts/new-upload-key.sh summandos` (asks for a password,
   writes `engine/summandos/keystore.properties`, git-ignored, and backs the key up to Google
   Drive). Copy the serial and fingerprints into the table below.
2. The bought models must be on this Mac (`scripts/engine-assets.sh engine/summandos` copies the
   Golem and Necromancer), or the bundle ships the stand-ins.
3. Signed bundle: `scripts/engine-export.sh --aab engine/summandos`, then
   `scripts/verify-bundle.sh engine/summandos/export/summandos.aab`: the owner `CN=Jorge Lillo`,
   versionCode 1, `android.permission.VIBRATE` only. Smoke test the same build first
   (`engine/summandos/tools/smoke_test.sh`).
4. Play Console → Create app: "Sumandos: suma y dispara", package name
   `com.jorgelillo.summandos` (it can never change), Spanish (Spain), Game, Free. Answer the
   App content forms (table above), pick the countries. The store listing goes from here:
   `./gradlew :engine:summandos:play:publishReleaseListing` (the `play/` module, Gradle Play
   Publisher), the first time with `listings/*/graphics` moved aside, then again with them: Play
   rejects a language's graphics until its listing exists.
5. Production → Create release → upload `summandos.aab` (Play App Signing on), release notes from
   `play/release-notes/` → save as draft → Publishing overview → Send changes for review.
   The review warns of native code without debug symbols (Godot's `.so`): accept it.
6. Once the draft is saved, Play has made the app signing key: Android developer verification →
   register the package name `com.jorgelillo.summandos` with the **app signing key** fingerprint
   (Integrity → App signing), as for the Kotlin apps (docs/publishing-on-google-play.md §0).
7. Users and permissions: give the publishing service account this app (release to testing and
   production, manage store presence), so later releases can go from CI.
8. Later releases: the Release workflow, once S4 wires the game into it (`play/` module, the
   game's GitHub environment with `scripts/set-release-secrets.sh summandos`).

Upload key (created 24 Nov 2026 with `scripts/new-upload-key.sh`, see [docs/signing.md](../../docs/signing.md)):

| | |
|---|---|
| File | `~/Projects/documentation/keys/summandos/upload-summandos.jks` (+ Google Drive backup) |
| Alias | `upload` |
| Serial | `3ee65971bab61913` (valid until Apr 2054) |
| SHA-1 | `03:50:CA:63:09:02:76:59:92:D1:BA:B7:14:13:25:AB:9D:73:14:21` |
| SHA-256 | `B2:9B:D4:00:3E:72:A5:B0:7C:68:3C:1C:43:77:43:49:0F:75:1B:F9:84:B3:28:55:11:14:D6:C2:C7:A1:BD:0A` |

Version history: [release-notes.md](release-notes.md).
