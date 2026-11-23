# lillo-android-apps — Claude Notes

Claude-specific notes for this repository. Read `/CLAUDE.md` first: it is the
canonical guide (structure, commands, conventions). Behaviour lives in
`openspec/` (start at `openspec/project.md`); open work in `PENDING.md`.

## Skills

| Skill | When |
|---|---|
| [`new-android-app`](skills/new-android-app/SKILL.md) | A new app, from idea to first Play-ready build: competitor study, design, scaffold. |
| [`release-android-app`](skills/release-android-app/SKILL.md) | Ship a new version of an app to Google Play. |
| [`upgrade-android-deps`](skills/upgrade-android-deps/SKILL.md) | Upgrade the toolchain and dependencies (AGP, Gradle, Kotlin, SDK levels, libraries). |
| [`check-android-env`](skills/check-android-env/SKILL.md) | Health of the local Android setup, API levels across the repo, Play's target-SDK deadline, Renovate. |
| [`engine-game`](skills/engine-game/SKILL.md) | A game in `engine/` (Godot): reuse the core, test, screenshot both ways round, check the icon, export an APK for the phone, start a new game. |
| [`slim-engine`](skills/slim-engine/SKILL.md) | A lighter APK for an engine game: its engine profile, the profile check, the slim Godot template, export and verify; bring a module back; Godot upgrades. |
| [`play-status`](skills/play-status/SKILL.md) | Every app's version in the repo against every Play track, as one table. |

Machine-level skills (the Mac's toolchain, disk, contacts) are not here: they
are versioned in lillorepo `system/skills/` and installed into
`~/.claude/skills/`, so they load in every project.

## Hooks

`settings.json` runs `scripts/check_paid_assets.py --hook` before Bash calls that add, commit,
push or open a PR, and blocks one that would carry a file from a bought asset pack.

## Plans

Product plans that outlive a session (an app's design, game ideas) live in
`docs/plans/`. A session's implementation plan goes in `.claude/plans/`
(git-ignored) and is deleted once its work is merged.
