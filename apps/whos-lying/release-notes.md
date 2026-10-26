# Who's lying? — Release notes

Newest first. User-facing notes per language are in
`app/src/main/play/release-notes/<locale>/default.txt` (the current version's).

| versionCode | Version | Date | Notes |
|---|---|---|---|
| 7 | 1.3 | 2026-10-26 (production) | Pack picker: "clear all" and long-press for only one pack (tester request); word count readable on ticked packs; dependencies 2026-10, cutout-safe pages, predictive back |
| 6 | 1.2 | 2026-10-24 (production) | Tester feedback: result rows keep the name whole (word under it), "remove someone" notice at the 24-player cap, drawing colour wheel, Blind never shows the category hint, primary violet darkened for WCAG AA |
| 5 | 1.1 | 2026-10-22 (internal testing, then production: the first release from CI) | Includes 4, never uploaded. Since then: text contrast at WCAG AA and bigger touch targets (accessibility checks), landscape fixes (podium, results, Drifter screens), the immutable game state behind every screen. Testers' feedback: edit an included pack by making your own copy; first-launch welcome note; an all-impostor chaos game ends when one player is left (it could go on with nobody to vote); a wrong Drifter guess no longer leaves the game stuck with no "next vote" button; landscape / short screens: result, ranking and podium put their buttons beside the content (opt-in `LilloPage(sideActionsWhenShort)`), Drifter guess field beside the text, compact podium and time-up |
| 4 | 1.0 | — (never uploaded: shipped in 5) | New icon (line-up with the liar in a fedora), share custom packs by link / QR (import by link, share-to-app or paste), clearer round/score texts, shared core code |
| 1 | 1.0 | — (not released) | First version: Classic, Blind and Drifter modes, Chaos, 6 packs per language, custom packs |
