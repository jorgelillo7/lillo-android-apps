# Summandos — Release notes

Newest first. User-facing notes per language are in
`play/src/main/play/release-notes/<locale>/default.txt` (the current version's).

Before any upload: `engine/summandos/tools/smoke_test.sh` on the emulator (OPERATIONS.md).

| versionCode | Version | Date | Notes |
|---|---|---|---|
| 2 | 1.1 | 2026-11-25, production | Endless mode, opened by winning every rank's last level: generated levels one after another with Captain's gates, waves and bosses growing past Captain's last level, until one is lost; the levels cleared are the record, shown under the menu's Endless button; the next level is generated while one is played. Store gallery: night mode and other squad colours |
| 1 | 1.0 | 2026-11-25, production | Four ranks (Recruit, Soldier, Captain, 10-level Legend) with How to play; 100 levels with stars; maths gates from plain sums to powers, roots, equations and two-step sums, with thinking pauses; aiming at waves down the lanes; weapon crates (pistols ×2, bazooka ×3) shown as models and in a HUD badge; a boss every fifth level that eats soldiers until shot down, six skeletons in turn (Golem and Necromancer from a bought pack), mage and Necromancer fireballs to dodge; bosses strike as they arrive and every second after, so a small squad can die there, and fights stay under about 10 seconds; good choices never leave the squad below where it started (Recruit) or a floor of 3, so every level can be won; one wrong gate is forgiven in Recruit (most runs still win), less in Soldier and Captain, and costs a star; levels are generated ahead on a worker thread; squad colours checked for colour blindness; sunset and night looks; synthesised sound and music; About with credits and where the name comes from; slim engine (19 MB APK) |
