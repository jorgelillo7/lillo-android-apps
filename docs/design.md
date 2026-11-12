---
name: Lillo Android Apps
description: Small, polished Android apps — each with its own colour, one shared design system
format: Google Labs design.md (tokens in YAML, rules in prose); each app's own file is apps/<app>/DESIGN.md
color-recipe:
  scheme: "Material 3 Vibrant, dark, from the app's seed colour: derived once, then hand-typed in the theme object (nothing regenerates it)"
  background: "{scheme.surface} → {scheme.surfaceContainerHigh} at the top (vertical gradient)"
  container: "{scheme.surfaceContainer}"
  containerHigh: "{scheme.surfaceContainerHighest}"
  onBackground: "{scheme.onSurface} (Neutral scheme instead when the seed tints text too much)"
  muted: "{scheme.onSurfaceVariant}"
  accent: "{scheme.primary} — harmonised accent for details, selected tabs, 'in progress'"
  brand: "the app's own vivid colour, for fills only: primary button, champion banner, summary hero"
  accentText: "a softer <Accent>Text tone whenever a saturated accent is used as text"
contrast:
  body-text: "≥ 4.5:1 on every surface it sits on (WCAG AA); body text ~15:1, never pure white"
  checked-by: "every screenshot test (Accessibility Test Framework TextContrastCheck)"
looks:
  pair: "a light and a dark LilloStyle per app (scheme + LilloPalette); follows the phone unless the user picks one in the About sheet (LilloThemeMode, stored per app)"
  palette: "LilloPalette (read through LilloColors): background brush, hero gradient, brand + onBrand, accentText"
  no-dynamic-colour: "Material You is never used: the app's colour is its identity"
typography:
  display-family: "Figtree (OFL, bundled in core: ExtraBold 800 and Black 900, Latin, ~55 KB): titles and big numbers, the store graphics' voice"
  family: "Roboto (system) for body, labels and buttons, weights 400 / 600 / 700 / 900"
  display: "displaySmall–displayLarge, Figtree Black/ExtraBold, tabular figures: big numbers and home titles"
  title: "headline* and titleLarge, Figtree ExtraBold: page headers (LilloPage), LilloScreenHeader"
  body: "bodyLarge / bodyMedium, Regular"
  label: "labelLarge / labelSmall, SemiBold: chips, metadata"
rounded:
  card: 16dp
  hero: 24dp (18dp compact)
  button: full pill (Material default)
  chip: 50%
spacing:
  page-gutter: 20dp
  card-padding: 16dp
  list-gap: 10–12dp
components:
  page: "LilloPage — header, content, bottom actions; side actions on short screens; readable width on tablets"
  primary-button: "LilloBigButton — brand fill, Black label, 60dp tall"
  secondary-button: "LilloSecondaryButton — outlined, ≥ 52dp, grows with its label"
  summary-hero: "LilloSummaryHero — opt-in gradient card for a home with real state to summarise: title, one-line state, chips; compact strip on short screens"
  screen-header: "LilloScreenHeader — the light header: a display-face title in accentText, an optional line and emoji; for screens whose list already says it all"
  accent-edge: "Modifier.accentEdge — a coloured bar on a card's start edge, only when it means something"
  entrance: "Modifier.entrance — fade and lift in once; nothing moves with 'Remove animations' on"
  spring-press: "Modifier.springPress — buttons shrink a little when pressed and spring back (in LilloBigButton and LilloSecondaryButton)"
  idle-float: "Modifier.idleFloat — a slow 4dp bob for the summary hero's emoji"
  confetti: "ConfettiBurst / ConfettiRain — wins only"
motion:
  entrance: "450 ms, FastOutSlowIn, 16dp lift; chips staggered 120 ms"
  respect: "animationsEnabled() — the system's 'Remove animations'"
---

## Overview

Eight apps, one family. Each app has **its own colour** (gold for tournaments, green for the league,
violet for the impostor, red and gold for Christmas…), and every app builds its screens **the same
way** from it: same recipe for surfaces and text, same components from `:core:designsystem`, same
rules for contrast and motion. Every app is moving to a light and a dark look that follow the
phone, with a choice in the About sheet (`LilloTheme(light, dark)`); until an app moves, it keeps
its single look. Five apps are dark only today. Three are light on purpose and documented
as such: the slipper game (a warm pixel-art kitchen), group-polls (paper-like) and stadium-events,
which is light by default for its older readers and the only app that follows the phone's dark mode
(`JaleoLightScheme` / `JaleoDarkScheme`).

The goal is not award-winning flash but **comfortable and usable**: calm tinted backgrounds, one
vivid colour where it matters, text that reads at a glance on a handheld in the sun.

## Colors

**Recipe (dark apps).** The seed colour went through Material 3's *Vibrant* dynamic scheme
(Google's colour system, `material-color-utilities`) once, when the palette was made: it gave tinted
backgrounds and cards, a harmonised accent and text tones with guaranteed contrast. Those tones are
typed by hand into each app's theme object (`ui/theme/*.kt`) and tuned until the screenshot tests'
contrast checks pass; no script derives them, so a new seed means picking the tones again. On top of that, the app keeps its **brand
colour vivid** for fills (primary button, champion banner, summary hero), where colour shows off.

- Backgrounds: tinted, never pure black; a vertical gradient from `containerHigh`-ish at the top to
  `surface` at the bottom.
- Cards: `container`, raised ones `containerHigh` (lighter = higher, as Material's dark theme says).
- Text: off-white around 15:1, secondary text `muted` (~7.6:1). When the seed tints text too much
  (league green, Christmas red), text comes from the Neutral scheme instead.
- Accents as **text** use a softer `<Accent>Text` tone: saturated colours vibrate as small text on
  dark backgrounds. As **fills** (buttons, chips, edges, headers) they stay vivid.
- Colour carries meaning: in progress, finished, favourite, own pack, danger. Decoration alone is
  not a reason to add a colour.

**Each app's tokens** live in its theme object (`ui/theme/*.kt`) and are described in
`apps/<app>/DESIGN.md`. Core components take their colours from the app's Material 3 colour roles
(`surfaceContainer`, `onSurfaceVariant`, `primary`…): set them in the app's `ColorScheme`.

## Typography

One display face, **Figtree** (SIL Open Font License; the licence ships in the APK as
`assets/licenses/figtree-OFL.txt`), for titles and big numbers: the heavy geometric voice the store
graphics speak in Avenir Next, carried into the app. Two static weights (ExtraBold, Black), Latin
and Spanish punctuation only, about 55 KB in all; every Material display, headline and `titleLarge`
style uses it, with tabular figures on the display sizes so counters don't jitter. Body text,
labels and buttons stay the system's Roboto (Regular for body, SemiBold for chips and metadata,
Black for the main button). Checked at twice the font size in the catalogue
(`TypeScaleLargestFontPreview`). Text shrinks to fit (`TextAutoSize`) only where content is
user-made or unbounded (cards, labels), down to a readable minimum, then scrolls.

## Layout

- `LilloPage` for every screen: 20dp gutters, bottom actions, side actions on short screens
  (landscape, handhelds), readable width (640dp) on tablets, two panes from 840dp where a screen
  has two things to show (tournaments, league-cards).
- Short screens (< 480dp tall) are first-class: every screen has a landscape screenshot test.
  Large fonts are checked on league-cards' play screen only (`play_large_font`,
  `play_landscape_large_font`); the other apps have no large-font test yet.

## Components

All in `:core:designsystem`, each with a preview in `Catalogue.kt` (captured with accessibility
checks). Apps wrap them with their colours in `ui/Common.kt`; they never copy them.

### Summary hero (`LilloSummaryHero`)

The home screen's header: a gradient card (the app's two hero colours, white text ≥ 4.5:1 even
under the translucent circles) with a title, a one-line state ("1 in progress · 1 finished") and up
to three chips for what matters now. A big emoji in the corner. Compact one-line strip on short
screens. Opt-in: only on a home with real state to summarise (a live status, a record, several
things in progress), never two per screen; otherwise `LilloScreenHeader`.


### Screen header (`LilloScreenHeader`)

The light alternative to the hero, for a home whose list already shows its state: a big title in
the display face and the app's `accentText`, an optional one-line subtitle and an emoji at the end.
No card, no gradient, so the list stays the first thing the eye lands on.

### Accent edge (`Modifier.accentEdge`)

A 5dp bar on a card's start edge for its state or kind: teal/accent *in progress*, gold *finished*
or *won*, yellow *favourite*, pink *your own*. Only where it means something; a list where every
card has the same edge has no edge.

### Buttons

One primary action per screen (`LilloBigButton`, brand fill). Secondary actions outlined
(`LilloSecondaryButton`) or text buttons. Destructive actions in the danger tone, never as the
primary.

## Motion

Small and purposeful: the summary hero fades and lifts in, chips one after another; confetti for
wins. Everything decorative goes through `animationsEnabled()` and stays still with the system's
*Remove animations*. Screenshot tests switch that on, so captures show the final state.

## Haptics

A light *confirm* tick on the primary button (in `LilloBigButton`), and one at each game's key
moment: the impostor's word showing under the curtain, a league card answered with a swipe, a
tournament's champion decided. Never on every tap; never as the only feedback.

## Characters that react

Where an app has a face, it reacts to how things are going (Focus Friend's lesson): league-cards'
header shows 😎 leading the season, 🙂 in the top half, 😭 at the bottom; tournaments' shows ⚔️
while something is being played and 🏆 once everything is decided.

## Do's and Don'ts

- **Do** derive every dark app's surfaces and text from its seed with the recipe above.
- **Do** keep the brand colour for fills and the softer `<Accent>Text` tones for coloured text.
- **Do** check contrast in screenshot tests, never by eye only; tune colours until they pass.
- **Don't** use pure black backgrounds or pure white body text in dark themes.
- **Don't** add a second vivid colour for decoration, or put two summary heroes on a screen.
- **Don't** change an app's palette without updating its `DESIGN.md` in the same change.
