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
	for stand_in in SummandosModels.STAND_INS.values():
		assert_does_not_have(SummandosModels.STAND_INS.keys(), stand_in, "%s is free" % stand_in)


## The Necromancer throws fireballs like the Mage; a stand-in keeps the boss's own behaviour.
func test_the_necromancer_throws_fireballs() -> void:
	assert_has(SummandosLevel.RANGED_BOSSES, "Necromancer.glb")
	for number in range(5, SummandosLevel.LEVELS_PER_MODE + 1, 5):
		var level := SummandosLevel.generate(SummandosLevel.Mode.EASY, number)
		var boss: Dictionary = level.waves[-1]
		assert_eq(boss.ranged, level.boss.model in SummandosLevel.RANGED_BOSSES, "level %d" % number)


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
