extends GutTest


## A bought model is drawn when this machine has the pack and its free stand-in when not, so
## every boss level has a model either way.
func test_a_missing_bought_model_falls_back_to_a_free_one() -> void:
	for file in SummandosLevel.BOSS_MODELS:
		var shown := SummandosModels.available(file)
		if ResourceLoader.exists(SummandosModels.DIR + file):
			assert_eq(shown, file)
		else:
			assert_eq(shown, SummandosModels.STAND_INS.get(file, file), file)
	assert_eq(SummandosModels.STAND_INS, {"Skeleton_Golem.glb": "Skeleton_Warrior.glb",
		"Necromancer.glb": "Skeleton_Mage.glb"}, "the Golem fights as a warrior, the Necromancer as a mage")
	for stand_in in SummandosModels.STAND_INS.values():
		assert_does_not_have(SummandosModels.STAND_INS.keys(), stand_in, "%s is free" % stand_in)


## The mage and the Necromancer throw fireballs, no other boss does; a stand-in keeps the
## boss's own behaviour.
func test_only_the_mage_and_the_necromancer_throw_fireballs() -> void:
	assert_eq(SummandosLevel.RANGED_BOSSES, ["Skeleton_Mage.glb", "Necromancer.glb"])
	for number in range(5, SummandosLevel.LEVELS_PER_MODE + 1, 5):
		var level := SummandosLevel.generate(SummandosLevel.Mode.EASY, number)
		var boss: Dictionary = level.waves[-1]
		assert_eq(boss.ranged, number in [10, 25], "level %d: the mage is boss 2, the Necromancer boss 5" % number)
		assert_eq(boss.get("burn", 0) > 0, boss.ranged, "level %d: only a ranged boss burns" % number)


## Pistols come as a pair and the bazooka alone, each standing on the ground at the asked length.
func test_weapons_show_as_pistols_or_a_bazooka() -> void:
	var pistols := SummandosModels.weapon(2.0, 1.5)
	var bazooka := SummandosModels.weapon(3.0, 1.5)
	assert_eq(pistols.get_child_count(), 2)
	assert_eq(bazooka.get_child_count(), 1)
	var box := SummandosModels.bounds(bazooka)
	assert_almost_eq(maxf(box.size.x, box.size.z), 1.5, 0.01)
	assert_almost_eq(box.position.y, 0.0, 0.01)
	pistols.free()
	bazooka.free()
