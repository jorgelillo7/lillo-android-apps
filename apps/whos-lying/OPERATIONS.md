# Who's lying? — Operations

Run from the **repo root** after `source scripts/env.sh`. Repo-wide commands and scripts:
[docs/operations.md](../../docs/operations.md).

Module paths: app `:apps:whos-lying:app`, logic `:apps:whos-lying:domain`.
Package: `com.jorgelillo.whoslying` (minSdk 24: navigation-compose needs it).

## Develop

| Task | Command |
|---|---|
| Unit tests | `./gradlew :apps:whos-lying:domain:test` |
| Lint | `./gradlew :apps:whos-lying:app:lintDebug` |
| Install debug | `./gradlew :apps:whos-lying:app:installDebug` |
| Reset (players, packs, settings) | `$ADB shell pm clear com.jorgelillo.whoslying` |
| Run in Spanish | `$ADB shell cmd locale set-app-locales com.jorgelillo.whoslying --locales es-ES` |
| Smoke test (release build, emulator running) | `apps/whos-lying/tools/smoke_test.sh` |
| Full battery: every mode and ending, match, packs, Spanish, landscape (release APK built, emulator running headless) | `python3 apps/whos-lying/tools/battery_test.py [scenario …]` |
| Regenerate icons | `.venv/bin/python apps/whos-lying/tools/generate_icon.py` |
| Store screenshots | emulator running + app installed: `apps/whos-lying/tools/capture_store_screenshots.sh`, check `store/raw/`, then `.venv/bin/python apps/whos-lying/tools/generate_store_assets.py` |

UI test tags (usable with `scripts/tap-text.sh`, dialogs included): `play`, `how_to`, `modes`,
`packs`, `about`, `modes_info`, `modes_sheet`, `timer_plus|minus`, `timer`, `unplayed`,
`choose_packs`, `pack_<id>`, `select_all_packs`, `packs_done`, `picker_new_pack`, `settings_sheet`, `language`,
`language_system|es|en`, `share`, `rate`, `privacy`, `player_name`, `add_player`, `continue`, `mode_classic|blind|drifter`, `impostors_plus|minus`,
`hint`, `chaos`, `start`, `tap_to_reveal` (the curtain: swipe it up, e.g. `adb shell input swipe 540 1700 540 700 800`), `secret_word`, `hide`, `go_vote`, `timer`, `debate_out`, `vote_<player>`,
`confirm_vote`, `back_to_debate`, `exposing`, `eliminated`, `keep_playing`, `result_title`,
`rounds_plus|minus`, `rounds`, `timer_value`, `time_up`, `rounds_left`, `see_winner`, `podium_title`, `new_match`,
`drawing`, `canvas`, `ink_0..6`, `undo`, `clear`, `share_drawing`, `play_again` (result → ranking), `next_round`, `rank_<player>`, `reset_scores`,
`guess_text`, `guess`, `reveal_all`, `play_again`, `home`, `new_pack`, `pack_name`, `pack_words`,
`save_pack`, `back`.

## Release

Releases follow the `release-android-app` skill.

| versionCode | versionName | Date | Track | Notes |
|---|---|---|---|---|
| 1 | 1.0 | 2026-10-07 | — | Uploaded to the bundle library by mistake, never released. **Code 1 is burnt.** |
| 2 | 1.0 | 2026-10-07 | Internal testing | First test with friends. Word packs without alcohol / drinking games, for a PEGI 3 rating |
| 3 | 1.0 | 2026-10-08 | Internal testing | Landscape / short screens (AYN Thor): compact reveal curtain and secret card, drawing canvas beside the tools, voting grid without the header, start button inside the setup list |
| 4 | 1.0 | 2026-10-12 | Never uploaded (shipped in 5) | New icon, share packs by link / QR, lint fixes, shared core code |
| 5 | 1.1 | 2026-10-22 | Internal testing → production (first release from CI) | Everything since 3: v4's icon and pack sharing, editable copies of included packs, contrast and touch targets, landscape fixes |

Upload key (created 5 Oct 2026 with `scripts/new-upload-key.sh`, see [docs/signing.md](../../docs/signing.md)):

| | |
|---|---|
| File | `~/Projects/documentation/keys/whos-lying/upload-whos-lying.jks` (+ off-Mac backup) |
| Alias | `upload` |
| Serial | `bd4c0d6a8362aa0` (valid until Feb 2054) |
| SHA-1 | `A9:9E:E3:39:4B:17:81:7F:0E:21:D5:C7:FC:BA:8C:35:33:02:73:A3` |
| SHA-256 | `85:7F:D2:53:4D:0A:5A:BE:FF:67:B8:8E:06:4B:B0:FB:9C:5B:BE:CF:DE:85:4B:08:6C:14:37:B7:34:B8:1A:3D` |

```bash
./gradlew :apps:whos-lying:app:bundleRelease
scripts/verify-bundle.sh apps/whos-lying/app/build/outputs/bundle/release/app-release.aab
```

## Report-a-word form

"Report this word" (result screen) opens a Google Form **in the browser**, prefilled with the word,
the impostor's word, the pack, the language, the reasons ticked in the app and the comment. The
player only taps *Enviar*. The app itself never goes online.

| What | Where |
|---|---|
| Public form (what players see) | https://docs.google.com/forms/d/e/1FAIpQLSf36XFjNcuWZLrEj4Msc01SdGrY8a-2GlxUy_DqQ-quov1dAQ/viewform |
| Apps Script project | "Script - ¿Quién miente? · Reportar palabra": https://script.google.com/home/projects/1FNOkHu9I9jCSLiGsM6TFvQ6dToGux63-maHfHmXYkFcYOqSnybrjeCzC/edit (only Jorge's account can open it) |
| Form and responses sheet | Jorge's Google Drive: "¿Quién miente? · Reportar palabra" and "¿Quién miente? · Reportes" |
| Script source | `tools/create_report_form.gs` (the copy in Apps Script is the same code) |
| Form URL and field ids used by the app | `app/src/main/kotlin/com/jorgelillo/whoslying/ui/Report.kt` (`WordReport`) |

**Changing texts** (title, description, thank-you message, question titles): edit the form directly
in Google Forms. Field ids don't change, so the app keeps working.

**Adding or replacing fields, or recreating the form** (the script always creates a *new* form):

1. Edit `tools/create_report_form.gs` here, paste it into the Apps Script project above, save and **Run** `createReportForm`.
2. From the execution log, copy the `Prefilled:` link. It holds the new form URL and one
   `entry.<id>=<PLACEHOLDER>` per field (placeholders are just markers; players never see them).
3. Update `FORM_URL` and the `entry.*` constants in `WordReport`, ship a new version.
4. Keep the old form open until most players have updated (old versions still point to it), then
   close it in Forms (*Responses → Accepting responses: off*).

Opening the `Prefilled:` link yourself shows the placeholders (PALABRA, PARECIDA…); that's
expected. The app replaces them with the real values.

## Decided against

- **Play Games Services / achievements** (suggested by Play Console because the app is a game):
  it needs the internet permission and identifies players by their Google account, which breaks
  the "no data, no accounts" promise (privacy policy, Data safety, welcome note), and achievements
  fit badly a one-phone party game where the phone owner isn't the only player. Dismiss the
  suggestion in the console.
