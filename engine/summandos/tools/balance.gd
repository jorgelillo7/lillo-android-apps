## Prints how hard each generated level is: what the best choices leave (the stars' yardstick),
## the biggest wave, how long the boss lasts, and how often random gates or the best gates with
## careless aim still win. Run: scripts/godot.sh --headless --path engine/summandos --script tools/balance.gd
extends SceneTree


func _init() -> void:
	var random := RandomNumberGenerator.new()
	random.seed = 1
	for mode in SummandosLevel.Mode.values():
		for number in [1, 3, 5, 10, 15, 20, 25, 30]:
			if number > SummandosLevel.level_count(mode):
				continue
			var level := SummandosLevel.generate(mode, number)
			var wins := 0
			for attempt in 20:
				var sides: Array[float] = []
				for row in level.rows:
					sides.append(-1.0 if random.randf() < 0.5 else 1.0)
				if SummandosRun.simulate(level, sides).state == SummandosRun.State.WON:
					wins += 1
			var best := SummandosLevel.best_sides(level)
			var careless := _careless(level, best)
			var biggest := 0
			var boss := ""
			for wave in level.waves:
				if wave.get("boss", false):
					boss = "boss %5d hp %4.1fs" % [wave.hp, _boss_seconds(level, best)]
				else:
					biggest = maxi(biggest, wave.count)
			print("mode %d level %2d  rows %2d  best leaves %4d  biggest wave %4d  %s  random wins %2d/20  careless aim %s" % [
				mode, number, level.rows.size(), level.reference, biggest, boss, wins,
				"wins %d" % careless.soldiers if careless.state == SummandosRun.State.WON else "LOSES"])
	quit()


## The best gates, but steering only for them and never in front of a wave.
func _careless(level: SummandosLevel, sides: Array[float]) -> SummandosRun:
	var run := SummandosRun.new(level)
	run.think_enabled = false
	for i in 30 * 300:
		if run.state != SummandosRun.State.RUNNING:
			break
		run.steer(2.0 * sides[mini(run.next_row, sides.size() - 1)])
		run.step(1.0 / 30.0)
	return run


## Seconds from the boss waking to its death, playing the best gates.
func _boss_seconds(level: SummandosLevel, sides: Array[float]) -> float:
	var run := SummandosRun.new(level)
	run.think_enabled = false
	var seconds := 0.0
	for i in 30 * 300:
		if run.state != SummandosRun.State.RUNNING:
			break
		run.steer(run.steer_hint(2.0 * sides[mini(run.next_row, sides.size() - 1)]))
		run.step(1.0 / 30.0)
		if run.boss_health() >= 0.0:
			seconds += 1.0 / 30.0
	return seconds
