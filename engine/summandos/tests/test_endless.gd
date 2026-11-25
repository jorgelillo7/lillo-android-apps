extends GutTest

const PATH := "user://test_endless.cfg"


func after_each() -> void:
	DirAccess.remove_absolute(PATH)


func test_every_endless_level_can_be_won_with_the_best_choices() -> void:
	for number in [1, 2, 5, 10, 15, 20, 30, 40, 60]:
		var level := SummandosLevel.endless(number)
		var run := SummandosRun.simulate(level, SummandosLevel.best_sides(level))
		assert_eq(run.state, SummandosRun.State.WON, "endless level %d" % number)
		assert_gte(run.lowest, SummandosLevel.floor_soldiers(level, SummandosLevel.Mode.ADULT),
			"endless level %d: best play never falls below the floor" % number)


func test_endless_levels_get_harder_and_are_the_same_every_time() -> void:
	assert_lt(_kept(range(30, 35)), _kept(range(1, 6)), "later levels leave the best play fewer soldiers")
	var a := SummandosLevel.endless(12)
	var b := SummandosLevel.endless(12)
	assert_eq(a.reference, b.reference)
	assert_eq(a.rows.size(), b.rows.size())
	assert_false(SummandosLevel.endless(10).boss.is_empty(), "a boss every fifth level")


func test_endless_opens_once_every_rank_is_finished_and_keeps_the_record() -> void:
	assert_false(SummandosProgress.endless_unlocked(PATH))
	for mode in SummandosLevel.Mode.values():
		assert_false(SummandosProgress.endless_unlocked(PATH), "not before every rank's last level")
		SummandosProgress.record(mode, SummandosLevel.level_count(mode), 1, PATH)
	assert_true(SummandosProgress.endless_unlocked(PATH))
	assert_true(SummandosProgress.record_endless(7, PATH))
	assert_false(SummandosProgress.record_endless(4, PATH), "a shorter run keeps the record")
	assert_eq(SummandosProgress.endless_best(PATH), 7)


func test_endless_levels_are_made_ahead() -> void:
	var levels := SummandosLevelsAhead.new()
	levels.prepare(SummandosLevel.ENDLESS, 3)
	assert_eq(levels.take(SummandosLevel.ENDLESS, 3).reference, SummandosLevel.endless(3).reference)
	levels.finish()


## The share of their soldiers the best choices keep through `numbers`, on average.
func _kept(numbers: Array) -> float:
	var total := 0.0
	for number in numbers:
		var level := SummandosLevel.endless(number)
		var sides := SummandosLevel.best_sides(level)
		var crowd: Array[int] = SummandosLevel._crowd_by_row(level, sides)
		total += SummandosRun.simulate(level, sides).soldiers / float(maxi(1, crowd.max()))
	return total / numbers.size()
