<div align="center">

<img src="store/play-icon-512.png" width="112" alt="What do you vote? icon" />

# 🔴🔵 What do you vote? · ¿Qué votáis?

**Red or blue polls: ask your friends with a link, or everyone in the public feed. Free, no ads.**

</div>

## ✨ Features

- 🔗 **Private polls**: only people with the link can vote; results show **who voted what**
- 🌍 **Public polls**: everyone sees them in the feed for a week, one section per language; only
  percentages, never voters' names; the creator's name is shown
- 🔴🔵 Two answers, red against blue, voted by tapping one half of the screen; the vote is final
- 🔮 **Prediction** (Wii homage): guess which side wins; your accuracy shows in Settings once polls close
- ⏳ Open for 1 day, 7 days or with no limit; the creator can close a poll, never delete it
- ↗ Share any poll: `https://jorgelillo7.github.io/q/#CODE` opens it in the app (App Links), or
  a page with Google Play otherwise
- 🛡️ Moderation: word filter on public polls, report + hide a creator, auto-hide after 3 reports,
  manual review; terms accepted before the first public poll
- 🗑️ Delete my data: name removed from your polls and votes (votes still count), phone forgets all
- 🚫 No ads, no accounts (anonymous sign-in), no analytics

## 🏗️ Architecture

| Piece | Where |
|---|---|
| `domain` | Pure Kotlin: poll model, draft validation, blocklist, invite codes, results, predictions. Unit tested. |
| `app` | Compose UI, `PollsViewModel`, DataStore (name, known polls, votes, predictions), navigation-compose. `PollRepository` with an in-memory `FakePollRepository` and the real `FirestorePollRepository` |
| Backend | No server: Firestore + anonymous Auth + App Check in its own Firebase project. Security rules, their emulator tests, moderation CLI and Terraform live in **lillorepo** (`packages/group_polls`, `infra/group_polls.tf`) |
| Links and privacy | `jorgelillo7.github.io`: `/q/`, `/.well-known/assetlinks.json`, `/privacy/group-polls/` |

Without `firebase.properties` the app builds and runs on the in-memory backend (sample polls), which
is what the smoke test uses.

## 📦 Commands

See [OPERATIONS.md](OPERATIONS.md). Version history: [release-notes.md](release-notes.md).
Plan and decisions: [docs/plans/group-polls.md](../../docs/plans/group-polls.md).
