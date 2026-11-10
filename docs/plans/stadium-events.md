# Stadium events app: ¿Hay jaleo? · Busy day?

Tells the neighbours around the Riyadh Air Metropolitano (San Blas-Canillejas, Las Rosas, Coslada,
San Fernando, the A-2 and M-40) whether tomorrow there is a match, a concert or another big event
at the stadium, with a reminder the evening before. Made for people who never check event
calendars, such as the user's parents: **as simple as possible, nothing to set up**.

Package `com.jorgelillo.stadiumevents`, folder `apps/stadium-events`. Play title "¿Hay jaleo?
Metropolitano" / "Busy day? Metropolitano" (the stadium is what people search for). An
independent app, not related to the stadium, the club or the organisers: the listing and the app
say so.

## Decisions

| Topic | Decision |
|---|---|
| Audience | Neighbours and drivers around the stadium, aged 50–70: big text, one question answered. |
| Main screen | One card answering tomorrow: "SÍ, hay jaleo" (amber; coral for the biggest days) with what, time and "more movement about 2 h before", or "NO, día tranquilo" (green). Below it, only the next three busy days. |
| Settings | None in v1. The reminder is on from the start: the evening before, at a fixed hour. |
| Widget | Home-screen widget with the same answer as the main card ("Mañana · Partido 21:00"), built on Christmas Countdown's Glance widget. |
| Busy levels | Atlético home matches, Spain matches and other events: "Mucho jaleo". Concerts and finals (e.g. the 2027 Champions League final): "Jaleo máximo". |
| Unconfirmed dates | LaLiga fixes the day a few weeks ahead ("sábado 5 o domingo 6"): the card and the list show both days and "día por confirmar"; the reminder goes out once the day is fixed. |
| Traffic | No real-time traffic. "About 2 h before" is a rule of thumb and is worded as one. |
| Where the data comes from | Shown in the app (footer: "Datos: agenda oficial del estadio (riyadhairmetropolitano.com) y calendario del Atlético de Madrid. Revisado hoy a las 8:00") and in the store description. |
| Palette | Light: cream background, dark text, amber / coral / green cards. The dark variant only when the phone is in dark mode. |
| Icon | The stadium seen from above (green pitch, cream stands) with two floodlights and a small amber bell between them, on the house's dark-blue gradient. |
| Permissions | INTERNET (only to download the events file) and POST_NOTIFICATIONS. No location, no account, no analytics, no ads. |

## Data

There is no single official feed (LaLiga's "sync your calendar" button is disabled on its page),
but two official pages cover everything, in plain HTML:

| Source | What it gives |
|---|---|
| [riyadhairmetropolitano.com](https://www.riyadhairmetropolitano.com/) (the stadium's agenda) | Every event at the stadium: Atlético home matches, Spain, finals, concerts. Date and type, no time. |
| [atleticodemadrid.com/calendario-completo-primer-equipo](https://www.atleticodemadrid.com/calendario-completo-primer-equipo/) | Every first-team match with date, kick-off time (or "Sin confirmar") and venue. |

A scheduled GitHub Actions job in the `jorgelillo7.github.io` repo reads both pages once a day,
merges them (the stadium's list, with the club's kick-off times), and publishes a small JSON file
on GitHub Pages. If it finds no events or cannot parse a page, it keeps the last good file and
the job fails, so it shows up. The app downloads that file once a day and when opened, keeps the
last copy for offline use, and schedules its own reminder: no push server.

When a source page changes its layout, the reader is fixed once and every phone picks it up the
next day without an app update.

## What competitors taught us

| App | Downloads (2026-11) | What it does |
|---|---|---|
| EvityGo: Eventos Metropolitano | 10+ | Same audience: reminders the day before, the morning of, or 1–2 h before; impact level and busiest window; no GPS or account. Asks you to pick weekdays and timing first; keeps an install id and push token on its server; mentions local sponsors in the future. |
| Eventos Metropolitano Madrid (LCApps) | 1,000+ | For people going to the events: reminders and buying tickets. Contains ads. |
| Atlético de Madrid's official app | Large | For fans: matches and tickets. Nothing for neighbours. |

Our difference: nothing to set up, a widget you see without opening the app, big text, and no
server or data at all.

## Known gaps (fine for v1)

- Concerts and Spain matches have no time on the stadium's page: the card shows the day only.
- Only this stadium. The package and folder are generic in case another one is ever added.
