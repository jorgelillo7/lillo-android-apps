<div align="center">

<img src="store/play-icon-512.png" width="112" alt="¿Quién llora esta jornada? icon" />

# 😭 ¿Quién llora esta jornada?

**A *Reigns*-style card game about life in a fantasy football league among friends.**

</div>

## ✨ Features

- 🃏 Each matchday the league's members write to you: swipe the card left or right to decide
  (or tap the buttons); some choices are gambles, some go on as a story — the next card straight
  away ("choose your own adventure") or a follow-up next matchday
- 📊 Four meters — 💰 money, 💪 squad, 🙄 the league's patience, 😭 tears. Any of them at 0 or 100
  ends the season with its own silly ending; they drift back towards the middle every matchday
- 💬 After every matchday, the league's group chat: your points, the day's events (recálculo,
  corralito, invalid line-up…) and the members' messages, names in their chat colours
- 🏆 A table (by team name) against computer rivals with personalities (the eternal competitor, the
  all-or-nothing one who abandons half the seasons…), a 19-matchday season (~5 minutes), a joker
  once a season
- 🏅 Trophies: collect the ten endings (locked until seen), achievements tied to cards (“the
  fastest moan in the West”, “three goalkeepers”…) or to how the season ends, the last 30 seasons;
  sharing the result lists the achievements of that season
- 🚫 No permissions, no account, no ads: the season is saved on the phone

## 🔒 The deck: sample in the repo, the league's own outside it

Everything funny is data: `League` (characters, rivals, cards, events, chat lines, joker,
endings) read from JSON. This **public** repo only ships a generic sample with archetypes
(`app/src/main/assets/league.json`). A real league's deck — real people, their nicknames and
in-jokes — must never be committed here: it lives in a private folder, by default
`../lloros-league/content/league-private.json` next to this repo (or the folder in the Gradle
property `leagueCards.privateDir`). When that file exists, the build packs it and the app plays
it instead of the sample. Anyone else, and CI's pull request checks, build the sample; the Release
workflow gets the private deck from a GitHub secret (`scripts/set-release-secrets.sh league-cards`)
and refuses to publish without it.

Store screenshots, if ever needed, must come from a build without the private deck.

## 🏗️ Architecture

| Piece | Where |
|---|---|
| Deck model, validation (`League.problems()`) | `domain/League.kt` |
| Season engine: deal, choose, gamble, matchday, rivals, endings | `domain/Season.kt` (`Game`) |
| Saved state, record, trophies, history | `domain/Saved.kt` |
| Screens: home, card, group chat, end, table | `app/…/ui/` |

All randomness comes from the season's seed plus where it happens (matchday, card), so a saved
season resumes exactly and tests are reproducible. Tests also play 200 seasons with a careful
and a random player to keep the balance honest (`GameTest`), on the sample and, when present on
the machine, on the private deck.

Operations: [OPERATIONS.md](OPERATIONS.md).
