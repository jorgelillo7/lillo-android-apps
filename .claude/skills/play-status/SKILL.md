---
name: play-status
description: Show which version of every app is in this repo and on each Google Play track (production, internal testing…), with each release's status (live, draft, staged rollout), as one summary table. Use when the user asks what version is on Play, what is live, what is pending, in review, in internal testing, or whether the repo is ahead of the store.
---

# Play status: repo versions vs Google Play

The data comes from `scripts/play_status.py`, run by the **Play status** workflow
(`.github/workflows/play-status.yml`) with the Play service account key that only GitHub holds.
Read only: it opens an edit, reads the tracks and deletes the edit.

## 1. Run it

```bash
gh workflow run play-status.yml --ref master
sleep 5
id=$(gh run list --workflow play-status.yml --limit 1 --json databaseId -q '.[0].databaseId')
gh run watch "$id" --exit-status >/dev/null
```

If this Mac has the key (`~/.config/lillo-android-apps/play-service-account.json`), the script
also runs locally: `pip3 install google-auth requests && python3 scripts/play_status.py`.

## 2. Read the table

```bash
gh run view "$id" --log | sed -n 's/^.*Z //p' | sed -n '/^| App |/,/In review/p'
```

(`rtk proxy gh …` if the output comes back truncated.) Each row: the repo's
`versionCode (versionName)`, then per track the releases as `versionCodes (name) · status`; ⬆️
marks apps whose repo version is ahead of what production serves.

## 3. Answer the user

Give one table in the user's language, one row per app: repo version, production, internal
testing, and a short **"what it means"** column:

| Status from the API | Say |
|---|---|
| `completed` on production, same versionCode as the repo | Live and up to date |
| `completed` with a versionCode just uploaded | Live, or **in review** if sent recently: the API can't tell (Play Console → Publishing overview shows it) |
| `draft` | Not sent yet: Play Console → Publishing overview → Send changes for review |
| `inProgress` (with a fraction) | Staged rollout |
| `halted` | Rollout stopped |
| "not on Play" | The app doesn't exist in Play Console yet |
| ⬆️ | The repo has a newer version than production: a release is pending |

Cross-check recent uploads with `gh run list --workflow release.yml --limit 10` (run names say
`<app> · <action> → <track>`) and `PENDING.md`, and say what the user should do next, if anything.
