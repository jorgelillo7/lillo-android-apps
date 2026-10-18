<div align="center">

<img src="store/play-icon-512.png" width="112" alt="Christmas Countdown icon" />

# 🎄 Christmas Countdown

**Days, hours, minutes and seconds until Christmas — with an advent calendar, a home-screen widget and carols.**

[![Google Play](https://img.shields.io/badge/Google%20Play-Get%20it-414141?logo=googleplay&logoColor=white)](https://play.google.com/store/apps/details?id=com.jorgelillo.christmascountdown)

</div>

## ✨ Features

- ⏱️ **Live countdown** to the next Christmas, with real time left (daylight saving included)
- 🌙 **Sleeps mode** — count the nights left, the way kids do
- 🎁 **Advent calendar** — 24 doors, one unlocks each day of December; the layout is reshuffled every year (same for everyone within a year). Behind them, activities from a pool of 35 in three blocks (days 1–7 get the house ready, 8–16 at home, 17–23 get together; Christmas Eve on the 24th), picked each year by least use so everything comes round, plus 2–3 music days that open the song on YouTube. Each opened door can be shared
- 🏠 **Home-screen widget** built with Jetpack Glance
- 🎶 **Carols** played by an on-device music-box synthesiser (no audio files shipped)
- ❄️ **Falling snow** drawn with Compose Canvas
- 📤 **Share** the countdown with friends
- 🌍 English and Spanish · 🚫 no ads, no tracking, no data collected
- 🔔 **Reminders**: one month and one week before (25 Nov, 18 Dec at 10:00), the day the Advent calendar opens (1 Dec, 10:00), the door of the day if still closed (2–24 Dec, 18:00) and Christmas Day (25 Dec, 10:00). Local only (WorkManager), one channel per kind; asked for in context with a card, never at install

## 🏗️ Architecture

| Module | What lives there |
|---|---|
| `domain` | Pure Kotlin: countdown and advent rules, carol scores, music-box synthesiser. Unit tested. |
| `app` | Compose UI (MVVM: `ViewModel` + `StateFlow`), DataStore settings, Glance widget, audio playback. |
| `:core:designsystem` | Shared theme (`LilloTheme`) and components (`GlassCard`, `GradientBackground`). |

```
app/src/main/kotlin/com/jorgelillo/christmascountdown/
├── MainActivity.kt                 Splash screen, edge-to-edge, theme
├── ChristmasCountdownApplication   App-wide singletons (manual DI)
├── data/SettingsRepository         DataStore: sleeps mode, music, opened doors per year
├── music/MusicBoxPlayer            Streams synthesised carols through AudioTrack
├── ui/                             ChristmasApp shell, countdown, advent and about screens
└── widget/CountdownWidget          Glance home-screen widget
```

## 🎨 Icon

All launcher and store icons are generated from a single geometry definition:

```bash
# from the repo root
scripts/setup-python.sh   # once
.venv/bin/python apps/christmas-countdown/tools/generate_icon.py
```

## 📦 Publishing

Everything uploaded to the Play listing is tracked in [`store/`](store), in the
[fastlane supply](https://docs.fastlane.tools/actions/supply/) layout:

```
store/metadata/android/<en-US|es-ES>/
├── title.txt, short_description.txt, full_description.txt
├── changelogs/<versionCode>.txt        release notes
└── images/                             icon, featureGraphic, phoneScreenshots/
store/raw/<locale>/                     raw emulator screenshots used for the framed images
```

Regenerate the framed screenshots and feature graphic after new raw captures:

```bash
# from the repo root
.venv/bin/python apps/christmas-countdown/tools/generate_store_assets.py
```

See [OPERATIONS.md](OPERATIONS.md) for every command and signing details, [release-notes.md](release-notes.md) for the version history, and the
[publishing guide](../../docs/publishing-on-google-play.md).

Privacy policy: https://jorgelillo7.github.io/privacy/christmas-countdown/
