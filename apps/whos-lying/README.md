<div align="center">

<img src="store/play-icon-512.png" width="112" alt="Who's lying? icon" />

# 🕵️ Who's lying? · ¿Quién miente?

**The impostor word game for groups, on one phone. Free forever: every pack, no ads, no subscriptions.**

</div>

## ✨ Features

- 🎭 **Classic:** the impostor gets a *similar* word and doesn't know they're the impostor
- 🙈 **Blind:** the impostor gets no word (optionally the category) and must bluff
- 🤷 **The Drifter:** classic plus a player with no word who wins by guessing it when voted out
- 🌀 **Chaos:** some games get a random number of impostors… sometimes everyone
- 📦 21 built-in packs, 600+ words per language (Spanish, English), each with two similar words, written for this app; multi-select grid
- 📝 **Your own packs:** one word per line, `word / similar / similar…` (similar words are optional; the impostor gets one at random)
- 🔁 No word repeats until ~90% of the chosen packs were played, then the oldest come back first; setup shows how many are left
- ⏱️ Optional discussion timer (1–24 min, recommended ~1 min per player): tap to pause; at zero it beeps, shows "Time's up!" and forces the vote
- 🎨 **Drawing mode** (with any mode): clues are drawn together on one canvas (7 inks, undo, clear) instead of said; the drawing is shown and shareable at the end
- 🏆 **Matches**: 3/5/7/10 rounds or no limit (recommended by group size); winners score (civilians 5, impostors/drifter 15), ranking with rounds left, final podium with confetti
- 📘 In-app guide to every mode (home → Modes, or "Which mode?" in setup)
- ⚙️ Settings: in-app language (Android 13+), share with friends, rate, privacy
- 🚫 No ads, no accounts, no network, no data collected
- 🔗 Share your own word packs by link or QR code (`jorgelillo7.github.io/p/#…`, the pack travels in the link): open it, share it to the app from a chat, or paste it in Packs → Import
- ✏️ The included packs can be copied into your own and edited (they ship with the app, so the originals stay as they are)

## 🏗️ Architecture

| Module | What lives there |
|---|---|
| `domain` | Pure Kotlin: roles and dealing (`Game.deal`), votes, drifter guess (accent/case-insensitive), outcomes, chaos, word picker, built-in packs. Unit tested. |
| `app` | Compose UI, one `GameViewModel`, DataStore JSON (players, settings, custom packs, recent words), navigation-compose. |

In CLASSIC/DRIFTER modes impostors see exactly the same screen as civilians (just a different word),
so nothing gives them away. Only players without a word are told their role.

## 📦 Commands

See [OPERATIONS.md](OPERATIONS.md). Version history: [release-notes.md](release-notes.md).
