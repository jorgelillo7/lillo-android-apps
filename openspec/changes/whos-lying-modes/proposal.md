# whos-lying: three modes and a simpler setup

## Why

The modes (Classic, Blind, The Drifter, plus a Chaos switch and a category hint) were hard to
tell apart, and the reveal card never said what a player was. Groups expect the modes of the
impostor games they already know: Original, Mysterious and Chaos.

## What changes

- **Modes** (landed with the domain, see `specs/whos-lying/game/spec.md`): Original (roles told,
  optional similar-word hint), Mysterious (roles hidden, optional journalists, the old drifter
  renamed), Chaos (Original with hints; about one game in three has more impostors, some without
  a hint; never scores). Impostors and journalists are at most half the group. Saved settings of
  earlier versions map onto the new modes.
- **Setup** in cards: mode cards, drawing switch, a players row, impostors and per-mode controls
  (hint in Original, journalists in Mysterious, nothing in Chaos) with a warning at the cap,
  rounds and duration rows that follow the player count.
- **Players**: rename a player (their score follows), remove, close back to setup.
- **Reveal card**: the role with its colour and icon, then the word, the hint or "no word";
  Mysterious shows the word only.
- **Home**: with a match half played, "Continue (round N of M)" and "New match"; "round"
  replaces "game" in the summary.
- **Chaos** shows no points, ranking or podium.
- **Help**: the modes sheet, how-to and welcome rewritten; whos-lying's sheets open fully
  (core `LilloSheet`).
