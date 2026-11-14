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
- 🌙 A reminder **the evening before** every busy day (19:00), on from the first launch, and
  **that morning** (09:00) if the evening's did not arrive; never at night, never twice for a day
- 🏠 Home-screen widget with tomorrow's answer, today's on an event day (the screen shows both)
- 📰 Where the data comes from, on screen: the stadium's official agenda and Atlético de Madrid's
  fixtures, merged daily into `https://jorgelillo7.github.io/stadium/events.json`
- 👵 Made for 50–70 year olds: light theme, large text, one screen; dark if the phone is, or if
  picked in About → Appearance
- 🚫 No ads, no account, no location, no analytics: the app only downloads that public file

## 🧱 How it is built

- `domain/` (pure Kotlin): the feed model (`Feed.kt`) and the rules (`Jaleo.kt`: the answer for a day,
  the next busy days, "about 2 h before", short titles, which reminder is due)
- `app/`: one Compose screen and its ViewModel; `data/FeedRepository` (plain HTTP GET, the last copy
  kept in a `JsonStore` for offline use); `evening/` (one WorkManager job at 09:00 and 19:00:
  download, the reminder that is due by the clock, widget, next run); `widget/` (Glance)
- The feed is built by `tools/stadium_events.py` in the `jorgelillo7.github.io` repo (daily GitHub
  Actions job). Plan and decisions: [docs/plans/stadium-events.md](../../docs/plans/stadium-events.md)

Commands: [OPERATIONS.md](OPERATIONS.md) · Colours and components: [DESIGN.md](DESIGN.md) ·
History: [release-notes.md](release-notes.md)
