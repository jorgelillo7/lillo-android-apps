<div align="center">

<img src="store/play-icon-512.png" width="112" alt="Busy day? icon" />

# 🏟️ Busy day? Metropolitano · ¿Hay jaleo? Metropolitano

**Is there a match, a concert or a final at the Riyadh Air Metropolitano tomorrow? For the
neighbours around it, with nothing to set up.**

</div>

## ✨ Features

- 🔔 One answer, in big letters: **"SÍ, hay jaleo"** (what, when, from when it gets busy) or
  **"NO, día tranquilo"**; amber for a match, coral for the biggest days (concerts, finals, Champions League, Real Madrid and Barcelona), green for a quiet day
- 🗓️ The next three busy days below it; LaLiga matches without a fixed day say "sábado o domingo,
  día por confirmar"; concert tours count their nights
- 🌙 A reminder **the evening before** every busy day (19:00), on from the first launch
- 🏠 Home-screen widget with tomorrow's answer
- 📰 Where the data comes from, on screen: the stadium's official agenda and Atlético de Madrid's
  fixtures, merged daily into `https://jorgelillo7.github.io/stadium/events.json`
- 👵 Made for 50–70 year olds: light theme, large text, one screen; dark only if the phone is
- 🚫 No ads, no account, no location, no analytics: the app only downloads that public file

## 🧱 How it is built

- `domain/` (pure Kotlin): the feed model (`Feed.kt`) and the rules (`Jaleo.kt`: tomorrow's answer,
  the next busy days, "about 2 h before", short titles)
- `app/`: one Compose screen and its ViewModel; `data/FeedRepository` (plain HTTP GET, the last copy
  kept in a `JsonStore` for offline use); `evening/` (a WorkManager job every evening at 19:00:
  download, reminder, widget, next evening); `widget/` (Glance)
- The feed is built by `tools/stadium_events.py` in the `jorgelillo7.github.io` repo (daily GitHub
  Actions job). Plan and decisions: [docs/plans/stadium-events.md](../../docs/plans/stadium-events.md)

Commands: [OPERATIONS.md](OPERATIONS.md) · Colours and components: [DESIGN.md](DESIGN.md) ·
History: [release-notes.md](release-notes.md)
