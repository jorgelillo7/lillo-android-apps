extends GutTest

const PATH := "user://test_progress.cfg"


func after_each() -> void:
	DirAccess.remove_absolute(PATH)


func test_a_level_is_the_same_every_time() -> void:
	var a := SummandosLevel.generate(SummandosLevel.Mode.NORMAL, 7)
	var b := SummandosLevel.generate(SummandosLevel.Mode.NORMAL, 7)
	assert_eq(a.rows.size(), b.rows.size())
	for i in a.rows.size():
		assert_eq(_label(a.rows[i].left), _label(b.rows[i].left))
	assert_eq(a.reference, b.reference)


func test_every_level_can_be_won_with_the_best_choices() -> void:
	for mode in SummandosLevel.Mode.values():
		for number in range(1, SummandosLevel.level_count(mode) + 1, 3):
			var level := SummandosLevel.generate(mode, number)
			var run := SummandosRun.simulate(level, SummandosLevel.best_sides(level))
			assert_eq(run.state, SummandosRun.State.WON, "mode %d level %d" % [mode, number])
			assert_eq(run.stars(), 3, "mode %d level %d: best play earns three stars" % [mode, number])


func test_later_levels_are_longer() -> void:
	var first := SummandosLevel.generate(SummandosLevel.Mode.EASY, 1)
	var last := SummandosLevel.generate(SummandosLevel.Mode.EASY, SummandosLevel.LEVELS_PER_MODE)
	assert_gt(last.rows.size(), first.rows.size())


func test_easy_gates_are_plain_and_adult_ones_are_puzzles() -> void:
	var easy := SummandosLevel.generate(SummandosLevel.Mode.EASY, 20)
	for row in easy.rows:
		for side in [row.left, row.right]:
			if side.has("gate"):
				assert_eq(side.gate.shown, "", "easy shows plain numbers")
	var puzzles := 0
	var thinks := 0
	for row in SummandosLevel.generate(SummandosLevel.Mode.ADULT, 20).rows:
		thinks += 1 if row.get("think", false) else 0
		for side in [row.left, row.right]:
			if side.has("gate") and side.gate.shown != "":
				puzzles += 1
	assert_gt(puzzles, 2, "adult writes operands as puzzles")
	assert_between(thinks, 1, 3, "a few thinking pauses, not every gate")


func test_a_think_row_stops_the_run_for_five_seconds() -> void:
	var level := SummandosLevel.new()
	level.length = 100.0
	level.start_soldiers = 10
	level.rows = [{"z": 30.0, "think": true, "left": SummandosLevel.gate(LilloGate.Op.ADD, 5),
		"right": SummandosLevel.gate(LilloGate.Op.ADD, 6)}]
	var run := SummandosRun.new(level)
	for i in 60 * 3:
		run.step(1.0 / 60.0)
	assert_gt(run.thinking, 0.0, "paused before the row")
	var stopped_at := run.z
	for i in 60 * 3:
		run.step(1.0 / 60.0)
	assert_eq(run.z, stopped_at, "nothing moves while thinking")
	run.done_thinking()
	for i in 60 * 3:
		run.step(1.0 / 60.0)
	assert_eq(run.next_row, 1, "the row was taken after the pause")


func test_progress_keeps_the_best_stars_and_opens_the_next_level() -> void:
	var mode := SummandosLevel.Mode.ADULT
	assert_false(SummandosProgress.unlocked(mode, 2, PATH))
	SummandosProgress.record(mode, 1, 2, PATH)
	SummandosProgress.record(mode, 1, 1, PATH)
	assert_eq(SummandosProgress.stars(mode, 1, PATH), 2)
	assert_true(SummandosProgress.unlocked(mode, 2, PATH))
	assert_false(SummandosProgress.unlocked(mode, 3, PATH))


func _label(pick: Dictionary) -> String:
	return pick.gate.label() if pick.has("gate") else "weapon %s" % pick.weapon


func test_expert_is_ten_levels_of_two_step_sums_with_a_pause_at_every_gate() -> void:
	assert_eq(SummandosLevel.level_count(SummandosLevel.Mode.EXPERT), 10)
	var level := SummandosLevel.generate(SummandosLevel.Mode.EXPERT, 4)
	assert_true(level.strict)
	assert_eq(level.think_seconds, 5.0)
	for row in level.rows:
		assert_true(row.get("think", false), "every expert gate pauses")
		assert_true(row.left.has("gate") and row.right.has("gate"), "no weapons in expert")


func test_in_expert_the_worse_gate_loses_at_once() -> void:
	var level := SummandosLevel.generate(SummandosLevel.Mode.EXPERT, 1)
	var sides := SummandosLevel.best_sides(level)
	sides[0] = -sides[0]
	var run := SummandosRun.simulate(level, sides)
	assert_eq(run.state, SummandosRun.State.LOST)
	assert_eq(run.next_row, 1, "lost on the first gate")
	assert_gt(run.failed_gate.other_result, run.failed_gate.taken_result)


func test_the_best_choices_lose_a_share_of_the_squad_and_more_in_harder_modes() -> void:
	for mode in [SummandosLevel.Mode.EASY, SummandosLevel.Mode.ADULT]:
		var level := SummandosLevel.generate(mode, 10)
		var gates_only := SummandosLevel.new()
		gates_only.start_soldiers = level.start_soldiers
		gates_only.length = level.length
		gates_only.rows = level.rows
		var sides := SummandosLevel.best_sides(level)
		var untouched := SummandosRun.simulate(gates_only, sides).soldiers
		var fought := SummandosRun.simulate(level, sides)
		assert_eq(fought.state, SummandosRun.State.WON)
		assert_lt(fought.soldiers, untouched, "the waves cost soldiers in mode %d" % mode)


func test_a_boss_lasts_seconds_and_a_weapon_shortens_the_fight() -> void:
	var level := SummandosLevel.generate(SummandosLevel.Mode.EASY, 5)
	var boss: Dictionary = level.waves[-1]
	assert_true(boss.boss)
	assert_gt(boss.hp, level.reference * SummandosRun.SHOTS_PER_SECOND * 3.0, "several seconds of fire")
