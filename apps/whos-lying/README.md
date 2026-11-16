<div align="center">

<img src="store/play-icon-512.png" width="112" alt="Who's lying? icon" />

# 🕵️ Who's lying? · ¿Quién miente?

**The impostor word game for groups, on one phone. Free forever: every pack, no ads, no subscriptions.**

</div>

## ✨ Features

- 🕵️ **Original:** everyone is told their role; impostors get no word, or a *similar* one as a hint
- 🎭 **Mysterious:** nobody is told their role; impostors get a similar word without knowing it; optional **Journalists** with no word, who win by guessing it when voted out
- 🌀 **Chaos:** Original with hints and a random number of impostors up to half the group; one game in five goes past it, one in five everyone is an impostor (no hints); never scored
- 👥 Impostors and journalists together are at most half the group; setup warns at the cap
- 📦 21 built-in packs, 600+ words per language (Spanish, English), each with two similar words, written for this app; multi-select grid
- 📝 **Your own packs:** one word per line, `word / similar / similar…` (similar words are optional; the impostor gets one at random)
- 🔁 No word repeats until ~90% of the chosen packs were played, then the oldest come back first; setup shows how many are left
- ⏱️ Optional discussion timer (1–24 min, recommended ~1 min per player): tap to pause; at zero it beeps, shows "Time's up!" and forces the vote
- 🎨 **Drawing mode** (with any mode): clues are drawn together on one canvas (7 inks, undo, clear) instead of said; the drawing is shown and shareable at the end
- 🏆 **Matches**: 3/5/7/10 rounds or no limit (rounds and time follow the group size); winners score (civilians 5, impostors/journalists 15), ranking with rounds left, final podium with confetti; the home continues a match half played or starts a new one
- 📘 In-app guide to every mode (home → Modes, or "Which mode?" in setup)
- ⚙️ Settings: in-app language (Android 13+), share with friends, rate, privacy
- 🚫 No ads, no accounts, no network, no data collected
- 🔗 Share your own word packs by link or QR code (`jorgelillo7.github.io/p/#…`, the pack travels in the link): open it, share it to the app from a chat, or paste it in Packs → Import
- ✏️ The included packs can be copied into your own and edited (they ship with the app, so the originals stay as they are)
- 🌗 Two looks, day and night: Auto follows the phone, or pick Light or Dark in Settings

## 🏗️ Architecture

| Module | What lives there |
|---|---|
| `domain` | Pure Kotlin: roles and dealing (`Game.deal`), votes, journalist guess (accent/case-insensitive), outcomes, Chaos, word picker, built-in packs. Unit tested. |
| `app` | Compose UI, one `GameViewModel`, DataStore JSON (players, settings, custom packs, recent words), navigation-compose. |

In Mysterious impostors see exactly the same screen as civilians (just a different word), so nothing
gives them away; only journalists are told their role. Original and Chaos tell everyone theirs.

## 📦 Commands

See [OPERATIONS.md](OPERATIONS.md). Version history: [release-notes.md](release-notes.md).
