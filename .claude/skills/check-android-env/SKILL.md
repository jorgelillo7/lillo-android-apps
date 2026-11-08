---
name: check-android-env
description: Check the health of the local Android setup and what the repo states about it (Command-line Tools, SDK packages, AVDs, Android Studio, JDK, API levels across the repo, Google Play's targetSdk deadline, free disk, Renovate) as one prioritised table. Read only. Use when the user asks whether the Android environment is fine or up to date, when the emulator misbehaves, or before upgrading the toolchain.
---

# Check the Android environment

Covers what Renovate can't see. Library versions are Renovate's and `upgrade-android-deps`' job.

## 1. Run it

```bash
source scripts/env.sh && python3 scripts/check_android_env.py
```

(`rtk proxy python3 …` if the output comes back truncated.) It prints a Markdown table, red first,
and exits with 1 if anything is red. It changes nothing.

## 2. Answer the user

Show the table in the user's language, then say what to do first:

| Status | Meaning |
|---|---|
| 🔴 | Something is broken or will break: fix it before any emulator or upgrade work |
| 🟡 | Behind or inconsistent, nothing is failing: fix it when convenient |
| 🟢 | Fine: one line is enough |

A row that says it couldn't check something is unknown, not healthy: say so.

## 3. Fixing

Don't fix anything until the user asks. Then use each row's Fix column. Android Studio is updated
by the user. Details of the broken-AVD case: `docs/setup/toolchain.md` → Gotchas.

The Play deadlines are constants at the top of the script (`PLAY_DEADLINES`): when Google announces
a new one, update them and `docs/publishing-on-google-play.md`.
