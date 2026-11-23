extends GutTest


func _level(rows: Array[Dictionary], waves: Array[Dictionary], soldiers := 10, length := 100.0) -> SummandosLevel:
	var level := SummandosLevel.new()
	level.start_soldiers = soldiers
	level.length = length
	level.rows = rows
	level.waves = waves
	return level


func _run_for(run: SummandosRun, seconds: float) -> void:
	for i in int(seconds * 60.0):
		run.step(1.0 / 60.0)


func test_the_side_the_squad_is_on_picks_the_gate() -> void:
	var rows: Array[Dictionary] = [{"z": 5.0, "left": SummandosLevel.gate(LilloGate.Op.ADD, 10), "right": SummandosLevel.gate(LilloGate.Op.MULTIPLY, 3)}]
	var left := SummandosRun.new(_level(rows, []))
	left.steer(-2.0)
	_run_for(left, 1.0)
	assert_eq(left.soldiers, 20)
	var right := SummandosRun.new(_level(rows, []))
	right.steer(2.0)
	_run_for(right, 1.0)
	assert_eq(right.soldiers, 30)


func test_taking_a_weapon_skips_the_gate_next_to_it() -> void:
	var rows: Array[Dictionary] = [{"z": 5.0, "left": SummandosLevel.weapon(2.0), "right": SummandosLevel.gate(LilloGate.Op.ADD, 30)}]
	var run := SummandosRun.new(_level(rows, []))
	run.steer(-2.0)
	_run_for(run, 1.0)
	assert_eq(run.soldiers, 10, "no gate taken")
	assert_eq(run.damage, 2.0)


func test_the_squad_never_leaves_the_track() -> void:
	var run := SummandosRun.new(_level([], []))
	run.steer(99.0)
	assert_eq(run.x, SummandosRun.TRACK_HALF_WIDTH)


func test_a_big_squad_shoots_a_wave_down_before_it_arrives() -> void:
	var waves: Array[Dictionary] = [{"z": 40.0, "count": 20, "hp": 1.0}]
	var run := SummandosRun.new(_level([], waves, 50))
	_run_for(run, 12.0)
	assert_eq(run.state, SummandosRun.State.WON)
	assert_eq(run.soldiers, 50, "nobody reached the squad")
	assert_eq(run.kills, 20)


func test_a_small_squad_is_overrun() -> void:
	var waves: Array[Dictionary] = [{"z": 15.0, "count": 200, "hp": 5.0}]
	var run := SummandosRun.new(_level([], waves, 3))
	_run_for(run, 20.0)
	assert_eq(run.state, SummandosRun.State.LOST)
	assert_eq(run.soldiers, 0)


func test_a_stronger_weapon_kills_faster() -> void:
	var waves: Array[Dictionary] = [{"z": 20.0, "count": 1, "hp": 30.0}]
	var plain := SummandosRun.new(_level([], waves, 5))
	var armed := SummandosRun.new(_level([], waves, 5))
	armed.damage = 3.0
	_run_for(plain, 2.0)
	_run_for(armed, 2.0)
	assert_true(armed.kills > plain.kills or armed.waves.is_empty(), "the armed squad has killed the target first")


func test_the_first_level_can_be_won_by_good_choices() -> void:
	var run := SummandosRun.new(SummandosLevel.first())
	var sides := [-2.0, -2.0, 2.0, -2.0, -2.0, -2.0]
	var row := 0
	for i in 60 * 120:
		if row < sides.size() and run.next_row == row:
			run.steer(sides[row])
		if run.next_row > row:
			row += 1
		run.step(1.0 / 60.0)
		if run.state != SummandosRun.State.RUNNING:
			break
	assert_eq(run.state, SummandosRun.State.WON, "soldiers left: %d" % run.soldiers)


func test_shots_only_land_when_aimed_at_the_wave() -> void:
	var waves: Array[Dictionary] = [{"z": 15.0, "x": 3.5, "count": 10, "hp": 1.0}]
	var aimed := SummandosRun.new(_level([], waves, 20))
	aimed.steer(3.5)
	var away := SummandosRun.new(_level([], waves, 20))
	away.steer(-3.5)
	assert_eq(aimed.aim(aimed.waves[0]), 1.0)
	assert_eq(away.aim(away.waves[0]), 0.0)
	_run_for(aimed, 1.0)
	_run_for(away, 1.0)
	assert_gt(aimed.kills, 0, "standing in front of the wave kills")
	assert_eq(away.kills, 0, "shooting at empty track kills nothing")


func test_an_awake_wave_edges_towards_the_squad() -> void:
	var waves: Array[Dictionary] = [{"z": 20.0, "x": 3.0, "count": 50, "hp": 100.0}]
	var run := SummandosRun.new(_level([], waves, 5))
	run.steer(-3.0)
	_run_for(run, 1.0)
	assert_lt(run.waves[0].x, 3.0)


func test_a_boss_is_not_traded_away_on_contact_but_eats_soldiers() -> void:
	var waves: Array[Dictionary] = [{"z": 3.0, "count": 1, "hp": 1000.0, "boss": true, "bite": 5.0}]
	var run := SummandosRun.new(_level([], waves, 30))
	_run_for(run, 3.0)
	assert_eq(run.waves.size(), 1, "the boss is still standing")
	assert_between(run.soldiers, 13, 20, "about 5 soldiers a second eaten")
	assert_between(run.boss_health(), 0.0, 0.99)


func test_a_weapon_brings_a_boss_down_sooner() -> void:
	var waves: Array[Dictionary] = [{"z": 3.0, "count": 1, "hp": 300.0, "boss": true, "bite": 1.0}]
	var plain := SummandosRun.new(_level([], waves, 40, 4.0))
	var armed := SummandosRun.new(_level([], waves, 40, 4.0))
	armed.damage = 2.0
	_run_for(plain, 3.0)
	_run_for(armed, 3.0)
	assert_eq(armed.state, SummandosRun.State.WON)
	assert_eq(plain.state, SummandosRun.State.RUNNING, "still fighting without the weapon")


func test_a_mage_fireball_burns_the_squad_where_it_stood_unless_it_moves() -> void:
	var waves: Array[Dictionary] = [{"z": 30.0, "count": 1, "hp": 100000.0, "boss": true, "bite": 1.0, "burn": 4}]
	var stays := SummandosRun.new(_level([], waves, 40))
	stays.steer(0.0)
	_run_for(stays, SummandosRun.FIREBALL_EVERY + SummandosRun.FIREBALL_FLIGHT + 0.05)
	assert_eq(stays.soldiers, 36, "one fireball landed on the squad")
	var dodges := SummandosRun.new(_level([], waves, 40))
	dodges.steer(0.0)
	_run_for(dodges, SummandosRun.FIREBALL_EVERY + 0.1)
	assert_eq(dodges.fireballs.size(), 1, "a fireball is in the air")
	dodges.steer(3.0)
	_run_for(dodges, SummandosRun.FIREBALL_FLIGHT)
	assert_eq(dodges.soldiers, 40, "moving out of the ring dodged it")


func test_only_the_mage_boss_throws_fireballs() -> void:
	for number in [5, 10, 15, 20]:
		var level := SummandosLevel.generate(SummandosLevel.Mode.EASY, number)
		var boss: Dictionary = level.waves[-1]
		assert_eq(boss.get("burn", 0) > 0, level.boss.model == SummandosLevel.RANGED_BOSS, "level %d" % number)
