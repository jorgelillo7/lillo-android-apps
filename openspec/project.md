# Project — lillo-android-apps

> The map of this repository's *behaviour*, for any reader (human or AI agent)
> landing here cold. Structure, build and commands live in `CLAUDE.md`; this
> describes **what the apps do and must keep doing**. Specs live in
> `openspec/specs/{app}/{capability}/spec.md`, grouped by the app they belong
> to, plus `openspec/specs/core/` for what the shared modules guarantee.

## What this is

A Gradle multi-project of small Android apps published on Google Play, with
shared Compose code in `core/`. No accounts, no ads, no server — except
`group-polls`, whose Firestore backend lives in lillorepo
(`packages/group_polls`). `stadium-events` downloads one static file
(`jorgelillo7.github.io/stadium/events.json`) and needs no server of its own.

## Apps and their capabilities

| App | Package | Capability specs (`specs/{app}/…`) |
|---|---|---|
| `christmas-countdown` | `com.jorgelillo.christmascountdown` | [`countdown`](specs/christmas-countdown/countdown/spec.md), [`advent-calendar`](specs/christmas-countdown/advent-calendar/spec.md), [`carols-and-reminders`](specs/christmas-countdown/carols-and-reminders/spec.md) |
| `decision-wheel` | `com.jorgelillo.decisionwheel` | [`wheel`](specs/decision-wheel/wheel/spec.md) |
| `whos-lying` | `com.jorgelillo.whoslying` | [`game`](specs/whos-lying/game/spec.md), [`packs`](specs/whos-lying/packs/spec.md) |
| `tournaments` | `com.jorgelillo.tournaments` | [`bracket`](specs/tournaments/bracket/spec.md), [`swiss`](specs/tournaments/swiss/spec.md), [`sharing-and-stats`](specs/tournaments/sharing-and-stats/spec.md) |
| `league-cards` | `com.jorgelillo.leaguecards` | [`season`](specs/league-cards/season/spec.md), [`trophies`](specs/league-cards/trophies/spec.md) |
| `stadium-events` | `com.jorgelillo.stadiumevents` | [`answer`](specs/stadium-events/answer/spec.md), [`feed-and-reminders`](specs/stadium-events/feed-and-reminders/spec.md) |
| `slipper` | `com.jorgelillo.slipper` | [`run`](specs/slipper/run/spec.md), [`challenges-and-progress`](specs/slipper/challenges-and-progress/spec.md) |
| `group-polls` | `com.jorgelillo.grouppolls` | [`polls`](specs/group-polls/polls/spec.md), [`moderation-and-privacy`](specs/group-polls/moderation-and-privacy/spec.md) (its Firestore rules and their spec: lillorepo `packages/group_polls`) |
| `core` | — | [`platform`](specs/core/platform/spec.md), [`sharelink`](specs/core/sharelink/spec.md), [`designsystem`](specs/core/designsystem/spec.md), [`reminders`](specs/core/reminders/spec.md) |

Screenshot tests freeze what users already see and are not cited by scenarios (see
`CLAUDE.md` § "Working on a change"). `python3 scripts/check_specs.py` fails when a
scenario names a test that does not exist, and lists scenarios with no test named; it
runs in CI's Lint job.

## How behaviour is pinned

- **Specs** (`openspec/specs/{app}/{capability}/spec.md`) state the contract
  as `Requirement` (SHALL) + `Scenario` (WHEN/THEN) blocks, each with the
  motive that makes it worth keeping.
- **Tests** prove it holds. Each scenario names the test that verifies it, by
  its Kotlin name — a sentence in backticks, as the tests are already written
  (`` `a short season ends after half the matchdays and still reaches the late cards` ``).
  A scenario without a test is a gap; a test without a scenario is
  undocumented behaviour.
- **Decisions** that shape the repo rather than one app's behaviour (module
  layout, reuse rules, publishing) stay in [`docs/decisions.md`](../docs/decisions.md).

## Changes

A feature that does not exist yet is agreed in `openspec/changes/{name}/`
before it is written; see [`changes/README.md`](changes/README.md).
