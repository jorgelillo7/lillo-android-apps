# Fake-ad games (idea, a collection)

Status: **idea; technology study in [fake-ad-games-tech.md](fake-ad-games-tech.md)**. Open item in [PENDING.md](../../PENDING.md).

Mobile ads often show a quick, satisfying mini-game that the installed app barely contains. The
idea is a collection that delivers those ads as real games, as faithful to the ad as possible in
mechanics, pacing, camera and feel, with our own art, characters and names (never a game's name,
characters or look).

Leaving the Kotlin/Compose stack is allowed (Godot, Unity…) if real 3D or crowds of 1,000+ need
it. The engine is decided during the study and recorded in [decisions.md](../decisions.md).

Two kinds of target, both welcome:

- **The ad is fake**: the advertised game is something else, so the ad's mini-game does not exist
  as a full game. We build it.
- **The ad is real but the game is buried**: the mechanic is there, but every level ends in
  interstitials, coins, chests and "watch a video for ×8". We build the same mechanic without any
  of that: no ads, no currency, no purchases.

## Reference ads

What each ad shows, and what the advertised game really is. The ad is what we build.

| Ad mechanic | Seen in ads for | What the ad shows | What the real game is |
|---|---|---|---|
| Maths gates crowd runner — **game one** | Top War, Last War: Survival; real games built on it: Count Masters: Crowd Runner 3D (the genre's reference: lead a crowd down a corridor through + / × gates to a horde at the end), Mob Control (a cannon at the bottom fires the crowd up lanes of gates at an enemy base), Weapon Craft Run (gates upgrade the weapon instead of the crowd) | A squad runs along a bridge, shooting or passing through blue gates (+20, ×2) and red ones (−15) to grow before a giant boss or an enemy army | Base-building strategy and resource management; Last War has a few similar mini-games early on |
| Number tower | Hero Wars | A knight picks which monster to fight by power (10 vs 5), absorbs the weaker ones and climbs to rescue a princess | Turn-based auto-battle RPG with little direct control |
| Pull the pin (rescue) | Homescapes, Gardenscapes | A character trapped in a burning or freezing house; pull pins in the right order to save him | Match-3 to earn stars and decorate a mansion |
| Pull the pin (treasure and lava) — **game two** | Evony: The King's Return, Royal Match, Homescapes | Pull pins so gold falls on the hero while lava or monsters fall elsewhere | Multiplayer empire strategy; a few pin levels added later |
| Makeover choices | Project Makeover | A dramatic story where someone needs a makeover; the ad picks the wrong choices on purpose | Match-3 with basic outfit and decoration choices |
| Lv. 1 to Lv. 100 | Mafia City | A level-1 crook robs a bank, picks money off the floor and becomes a boss surrounded by luxury | Static building management, timers and clan auto-battles |
| Unscrew by colour | Screwdom (and many screw-puzzle clones) | A 3D house or toy covered in coloured screws; rotate it, tap a screw and it flies to the box of its colour at the top (3 holes; a full box leaves and a new one comes) or to a 5-hole waiting row; pieces fall as their screws go; fill the waiting row and you lose | Close to the ad, but buried in ads, coins and rewarded-video multipliers after every level |
| Upgrade-with-cash tower defense | (screenshots to come) | Defend against waves, spend the cash on upgrades | — |
| Idle-arcade farm | (screenshots to come) | Cut corn, collect eggs, sell them, buy upgrades | — |

Other common ad mechanics to look at: draw a line to protect, rescue-the-hero doors, merge
soldiers, stack runner that builds a bridge, parking jam, colour sort.

## Leads

- KFC's "Original Fake Games": playable versions of fake ads (rolling barrels, a trapped king, a
  giant pin). [The Drum](https://www.thedrum.com/opinion/2025/04/08/why-we-made-phony-game-ads-reality-kfc)
- A developer who turned fake ads into a real game. [80.lv](https://80.lv/articles/game-developer-turned-fake-mobile-game-ad-into-real-game)
- "Fake Mobile Game", a parody with maths gates and knights. [itch.io](https://ray3d.itch.io/fake-mobile-game)
- Games that really play like the ads (forum thread). [Straight Dope](https://boards.straightdope.com/t/games-which-actually-play-like-the-fake-mobile-ads/1003054)
- Pull the pin, its history and effect. [PocketGamer.biz](https://www.pocketgamer.biz/the-evolution-of-pull-the-pin-why-is-this-a-good-thing-for-the-games-industry)
- How fake games fail expectations (study). [Penn State](https://psu.edu/news/research/story/five-ways-fake-mobile-games-fail-meet-advertised-expectations)

## The study should answer

- Which ads to build first, and whether they ship as one app with many games or one app each.
- The engine: Compose Canvas with fake perspective, or Godot/Unity for real 3D and big crowds.
- What each game needs beyond the ad's 30 seconds to stay fun: levels, upgrades, difficulty curve.
