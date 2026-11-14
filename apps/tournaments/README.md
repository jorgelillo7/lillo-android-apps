<div align="center">

<img src="store/play-icon-512.png" width="112" alt="Last one standing? icon" />

# 👑 Last one standing? · ¿Solo quedará uno?

**Casual tournaments among friends — cards, ping-pong, FIFA, anything. No account, no server.**

</div>

## ✨ Features

- ⚔️ **Knockout** with any number of players (byes spread with standard seeding) and a
  **two-sided bracket**: left half, right half, final in the middle; pinch or −/+ to zoom
- ♟️ **Swiss**: rounds by ranking without rematches, one bye per player, 3/1/0 points and the
  organised-play tie-breakers (OMW, GW, OGW, floored at 33 %); optional top 2/4/8 knockout
- 🎲 Random draw, re-draw until the first result is in
- 📝 Best of 1, 3 or 5; results like "Pepe beat Luis 2–1" with an optional comment ("20 vidas a 0");
  only valid scores are offered
- 🏆 Victory line: the winner's connector turns gold, the champion's path is thicker, confetti
- 📲 **Another phone without a database**: the whole tournament travels inside a link / QR code
  (`https://jorgelillo7.github.io/t/#…`, data after the `#`, never sent to a server); import by
  opening it, sharing it to the app, or pasting it. Newest copy wins
- 🖼️ Export the bracket or standings as an image, and every match as CSV
- 📊 Hall of fame across tournaments (by player name): titles, finals, record, win-rate bar
- 🚫 No permissions at all, no ads, no analytics
- 🌗 Two looks, day and night: Auto follows the phone, or pick Light or Dark in About → Appearance

## 🏗️ Architecture

| Piece | Where |
|---|---|
| `domain` | Pure Kotlin: model, bracket (seeding, byes, advancing), Swiss pairing and standings, two-sided layout, stats, CSV, share-link codec, library import. Unit tested. |
| `app` | Compose UI, `TournamentsViewModel`, DataStore (one JSON document), navigation-compose, QR with ZXing core (encoding only), PNG export with `android.graphics.Canvas` + FileProvider |
| Links and privacy | `jorgelillo7.github.io`: `/t/`, `/.well-known/assetlinks.json`, `/privacy/tournaments/` |

Decisions and competitor notes: [docs/plans/tournaments.md](../../docs/plans/tournaments.md).

## 📦 Commands

See [OPERATIONS.md](OPERATIONS.md). Version history: [release-notes.md](release-notes.md).
