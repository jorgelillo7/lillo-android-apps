# Game ideas

Game ideas for after every current app is on the store. Not started: pick one, then write
its own plan here like [tournaments.md](tournaments.md). Same family rules as the apps: no ads,
no account, no server, "question + what it is" names.

Common hook for both: **challenge by link**. The end screen shares "I scored 1,240, can you beat
it?" with a `ShareLink` holding the seed (or level) and the score, so a friend plays exactly the
same run. It turns a solo game into a WhatsApp rivalry without a server.

## 1. The slipper (working name "¿Te pilla la chancla?")

One-finger runner: you run away from the grandma, dodging the slippers she throws.

- Collect croquetas on the way; the more you collect, the angrier she gets and the faster she throws.
- 30-second runs, restart in under a second.
- Later: modes and bosses (the homing slipper, "¡a comer!").
- Tech: Compose `Canvas` + a `withFrameNanos` loop + rectangle collisions; rules in a pure
  Kotlin `domain` with tests. Roughly two weeks.
- Fun first: get jump/hit/death feeling right with ugly rectangles before drawing anything —
  screen shake, 50 ms hit pause, particles, haptics, short sounds, gradual difficulty.

## 2. Tilt-and-roll puzzle (in the spirit of Kula World)

Roll a ball through levels by tilting the phone: a puzzle more than a reflex game.

- Kula World (PS1): a ball on floating block platforms in space. Gravity follows the ball over
  edges onto other faces, there are keys to collect before the exit opens, a timer, traps and
  jumps. The fun is working out the route.
- Our take: tilt (accelerometer) to roll, plus a tap to jump; short levels with keys, exit,
  holes, spikes and moving blocks; stars for time. Shared challenge = level + time.
- Open decision: **3D like Kula World** (gravity changing faces) needs a real engine — Godot
  (free, open source, exports to Android) rather than Compose. **2D top-down / isometric** fits
  in Compose and in this repo but loses the gravity-flip trick.
- The cost is level design, not code: a level format (text or JSON) and an in-app or desktop
  editor early on, and playtests to tune each level.

## 3. Our fantasy league, as swipe cards (working name "¿Quién llora esta jornada?")

A game about our Biwenger league and its people, built as *Reigns*-style cards so the in-jokes
are the game itself instead of interrupting it.

- **Turn = matchday.** 3–5 cards, each a league member with a situation; swipe left or right.
  Examples: lillogafe signs Isco → next matchday "injured for 5 months"; Fabio, between beers,
  triggers your release clause (pay to keep the star or let him go); Nostrajavi "sees team X
  winning" → they lose 3‑0; lilloelección de capitán → ‑16; Pablo complaining there are no
  shady deals; the Argentinian's complaints.
- **Four meters** like Reigns: 💰 money, ⚽ points, 🩹 squad fitness, 😤 the league's patience.
  One hitting zero (or maxing out) ends the season in a silly way ("kicked out of the WhatsApp
  group").
- **End of matchday** screen with the "llorómetro": the moaning or bragging messages ("this
  matchday nothing", "100 points!") go here, never mid-decision.
- **Season** = 38 matchdays; win the league against bots with each member's personality.
- **Content in JSON** (text, character, effects of each choice, optional follow-up card), so
  anyone in the league can propose cards. The user has message screenshots to mine.
- **Action variant** (if cards feel too calm): a runner dodging injuries while everyone chases
  the star striker; the messages only between matchdays (levels).

**Names: the Pro Evolution Soccer way.** PES without licences had "Roberto Larcos", "Man Red"
and "London FC": recognisable, not the real thing. Same here, from day one:

- Footballers and clubs: parody names (e.g. a "Lamal" or "Mbapi"), no real crests, kits or photos.
- No "Biwenger" name or logo: it's "the league", a fantasy league in general.
- League members: nicknames, and only with each one's OK.
- Distribute through Play closed testing for the league only; with parody names it could
  later go public without changes.
