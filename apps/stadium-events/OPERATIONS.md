# Busy day? Metropolitano — Operations

Run from the **repo root** after `source scripts/env.sh`. Repo-wide commands and scripts:
[docs/operations.md](../../docs/operations.md).

Module paths: app `:apps:stadium-events:app`, logic `:apps:stadium-events:domain`.
Package: `com.jorgelillo.stadiumevents`.

## Develop

| Task | Command |
|---|---|
| Unit tests | `./gradlew :apps:stadium-events:domain:test :apps:stadium-events:app:testDebugUnitTest` |
| Screenshot tests | `./gradlew :apps:stadium-events:app:verifyRoborazziDebug` (re-record: `recordRoborazziDebug`) |
| Lint | `./gradlew :apps:stadium-events:app:lintDebug` |
| Install debug | `./gradlew :apps:stadium-events:app:installDebug` |
| Smoke test (release) | `apps/stadium-events/tools/smoke_test.sh` (needs internet on the emulator) |
| Regenerate icons | `.venv/bin/python apps/stadium-events/tools/generate_icon.py` |
| Store screenshots | emulator running, with internet: `apps/stadium-events/tools/capture_store_screenshots.sh` (and `CAPTURE_DEVICE=tablet …`), check `store/raw*/`, then `.venv/bin/python apps/stadium-events/tools/generate_store_assets.py`. The days it moves the clock to follow the agenda of 2026-11: pick new ones when it changes |
| Baseline profile | emulator running: `scripts/generate-baseline-profiles.sh stadium-events` |
| Try the evening reminder | the day before an event in the feed, set the emulator's clock to 18:59 (`scripts/emulator.sh date MMDD1859YYYY`) and open the app once |

UI test tags (`scripts/tap-text.sh`): `answer`, `loading`, `upcoming_<event id>`, `retry`, `about`.

## The feed

`https://jorgelillo7.github.io/stadium/events.json`, rebuilt every day at 05:00 UTC by the
**Stadium events** workflow of the `jorgelillo7.github.io` repo (`tools/stadium_events.py`, tests in
`tools/test_stadium_events.py`). It reads:

| Source | Gives |
|---|---|
| `https://www.riyadhairmetropolitano.com/` | every event at the stadium: title, days ("5 O 6" = one of them, "a \| b \| c" = all), category |
| `https://www.atleticodemadrid.com/calendario-completo-primer-equipo/` | home matches: competition, kick-off, and the day once LaLiga fixes it |

Format (version 1): `{"version": 1, "sources": [...], "events": [{"id", "title", "kind":
"match|final|concert", "level": "busy|max", "days": ["2026-12-05", "2026-12-06"], "either": true,
"time": "21:00" | null, "competition", "url"}]}`. The app ignores unknown fields and falls back to
defaults for unknown values, so the feed can grow; renaming or removing a field needs a new
`version`.

If the workflow fails, a source page changed: fix the reader there, run its tests, and the next run
publishes again. Meanwhile the last good file stays published and the apps keep their last copy.

## Release

Not on Play yet: see `PENDING.md`. The first bundle and the Play Console setup are by hand
(`release-android-app` skill and [docs/publishing-on-google-play.md](../../docs/publishing-on-google-play.md));
after that, the Release workflow (`-f app=stadium-events`).

Play Console answers:

| Question | Answer |
|---|---|
| Category | App · Maps & navigation (or Lifestyle) |
| Data safety | **No data collected, no data shared.** The app downloads a public file; no account, no location, no analytics |
| Permissions | INTERNET (the events file), POST_NOTIFICATIONS (the evening reminder); WorkManager adds ACCESS_NETWORK_STATE, RECEIVE_BOOT_COMPLETED, WAKE_LOCK and FOREGROUND_SERVICE (its own service, not used to show anything), like Christmas Countdown |
| Ads | No |
| Target audience | 18+ |
| Privacy policy | `https://jorgelillo7.github.io/privacy/stadium-events/` |
