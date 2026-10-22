# docs/ — index

One entry per document. Runbooks first, then setup, then reference.

## Operations

| Doc | What's in it |
|---|---|
| [`operations.md`](operations.md) | Repo-wide runbook: prerequisites, build/test, shared scripts, release steps. Per-app commands live in `apps/<app>/OPERATIONS.md` |
| [`publishing-on-google-play.md`](publishing-on-google-play.md) | End-to-end Play checklist: account, signing, App content declarations, store listing, release tracks, review |
| [`adding-a-new-app.md`](adding-a-new-app.md) | Folder layout, Gradle setup, icon, privacy policy and store assets for a new app |
| [`signing.md`](signing.md) | Upload keys: create one per app (`scripts/new-upload-key.sh`), where it lives, backup, lost key |

## Setup

| Doc | What's in it |
|---|---|
| [`setup/toolchain.md`](setup/toolchain.md) | Versions in use, how to upgrade them, and the gotchas we hit (AGP 9, R8, JDK) |

## Reference

| Doc | What's in it |
|---|---|
| [`decisions.md`](decisions.md) | The choices that shape the repo, one line each with its reason and PR |
| [`lessons-learned.md`](lessons-learned.md) | Things that went wrong once and how to avoid them |

## Per app

| App | Operations | Release notes |
|---|---|---|
| Is it Christmas yet? (Christmas countdown) | [`OPERATIONS.md`](../apps/christmas-countdown/OPERATIONS.md) | [`release-notes.md`](../apps/christmas-countdown/release-notes.md) |
| What next? (decision wheel) | [`OPERATIONS.md`](../apps/decision-wheel/OPERATIONS.md) | [`release-notes.md`](../apps/decision-wheel/release-notes.md) |
| Who's lying? (impostor game) | [`OPERATIONS.md`](../apps/whos-lying/OPERATIONS.md) | [`release-notes.md`](../apps/whos-lying/release-notes.md) |
| What do you vote? (group polls) | [`OPERATIONS.md`](../apps/group-polls/OPERATIONS.md) | [`release-notes.md`](../apps/group-polls/release-notes.md) |
| Last one standing? (tournaments) | [`OPERATIONS.md`](../apps/tournaments/OPERATIONS.md) | [`release-notes.md`](../apps/tournaments/release-notes.md) |
| ¿Quién llora esta jornada? (league cards) | [`OPERATIONS.md`](../apps/league-cards/OPERATIONS.md) | [`release-notes.md`](../apps/league-cards/release-notes.md) |
| ¿Te pilla la chancla? (slipper runner) | [`OPERATIONS.md`](../apps/slipper/OPERATIONS.md) | [`release-notes.md`](../apps/slipper/release-notes.md) |

## Plans

Design decisions of apps that needed a plan before coding:
[`plans/group-polls.md`](plans/group-polls.md), [`plans/tournaments.md`](plans/tournaments.md).

Shared scripts: [`scripts/`](../scripts). Branding: [`branding/`](../branding).
Claude Code skills: [`.claude/skills/`](../.claude/skills) — `new-android-app`, `upgrade-android-deps`, `release-android-app`.
