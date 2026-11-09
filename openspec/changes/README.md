# changes/

In-flight change proposals. One folder per change, each with:

```
{change-name}/
├── proposal.md   # why, what's changing, at a glance
├── design.md     # the approach and the trade-offs the behaviour depends on
├── tasks.md      # checklist of outcomes, including the test names the specs promise
└── specs/        # spec deltas: ADDED / MODIFIED / REMOVED requirements
```

A change captures the behaviour *before* the code, then updates `../specs/`
when it lands. Between changes this folder is empty (only this README); the
history stays in `git log`.

This mirrors the [OpenSpec](https://github.com/Fission-AI/OpenSpec) convention,
adopted as filesystem structure only — see `../project.md`.
