# Summandos — Release notes

Newest first. User-facing notes per language are in
`play/src/main/play/release-notes/<locale>/default.txt` (the current version's). Not on Google Play
yet: the first release goes straight to production as a draft (OPERATIONS.md → Release).

Before any upload: `engine/summandos/tools/smoke_test.sh` on the emulator (OPERATIONS.md).

| versionCode | Version | Date | Notes |
|---|---|---|---|
| 1 | 1.0 | unreleased (test APKs 0.1 on the owner's handheld) | Four ranks (Recruit, Soldier, Captain, 10-level Legend) with How to play; 100 levels with stars; maths gates from plain sums to powers, roots, equations and two-step sums, with thinking pauses; aiming at waves down the lanes; weapon crates (pistols ×2, bazooka ×3) shown as models and in a HUD badge; a boss every fifth level that eats soldiers until shot down, six skeletons in turn (Golem and Necromancer from a bought pack), mage and Necromancer fireballs to dodge; bosses strike as they arrive and every second after, so a small squad can die there, and fights stay under about 10 seconds; good choices never leave the squad below where it started (Recruit) or a floor of 3, so every level can be won; one wrong gate is forgiven in Recruit (most runs still win), less in Soldier and Captain, and costs a star; levels are generated ahead on a worker thread; squad colours checked for colour blindness; sunset and night looks; synthesised sound and music; About with credits and where the name comes from; slim engine (19 MB APK) |
