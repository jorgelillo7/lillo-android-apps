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
| `christmas-countdown` | `com.jorgelillo.christmascountdown` | not written yet |
| `decision-wheel` | `com.jorgelillo.decisionwheel` | not written yet |
| `whos-lying` | `com.jorgelillo.whoslying` | not written yet |
| `tournaments` | `com.jorgelillo.tournaments` | not written yet |
| `league-cards` | `com.jorgelillo.leaguecards` | not written yet |
| `slipper` | `com.jorgelillo.slipper` | not written yet |
| `stadium-events` | `com.jorgelillo.stadiumevents` | not written yet |
| `group-polls` | `com.jorgelillo.grouppolls` | not written yet (its Firestore rules and tests: lillorepo `packages/group_polls`) |
| `core` | — | not written yet |

Until an app has its specs, its behaviour is pinned by its tests alone: the
domain tests, the ViewModel and `*FlowTest` tests, and the snapshot tests that
freeze what users already see (see `CLAUDE.md` § "Working on a change").

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
