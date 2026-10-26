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
`versionCode (versionName)`; per track the releases as `versionCodes · status [rollout %]`, plus
Play's release name when it doesn't match (a leftover label, harmless); empty drafts are left out;
the app's **last Release workflow run** (`dd/mm (days ago) action → track: result`). ⬆️ marks apps
whose repo version is ahead of what production serves.

## 3. Answer the user

Give one table in the user's language, one row per app: repo version, production, internal
testing, and a short **"what it means"** column:

| Status from the API | Say |
|---|---|
| `completed` on production, same versionCode as the repo | Live and up to date |
| `completed`, last run sent it ≤ 7 days ago | **Probably in review** (usually hours to a few days; new apps longer): the API can't tell, Play Console → Publishing overview shows it |
| `completed`, last run older | Live |
| Repo ⬆️ and the last run failed or is waiting | The upload didn't go through: check the run (approval pending?) |
| `draft` | Not sent yet: Play Console → Publishing overview → Send changes for review |
| `inProgress` (with a fraction) | Staged rollout |
| `halted` | Rollout stopped |
| "not on Play" | The app doesn't exist in Play Console yet |
| ⬆️ | The repo has a newer version than production: a release is pending |

Then say what the user should do next, if anything (approve a waiting run, send a draft for
review, look at a failed run), cross-checking `PENDING.md`. Apps "not on Play" that PENDING lists
as ready (the slipper) are a pending manual first upload.
