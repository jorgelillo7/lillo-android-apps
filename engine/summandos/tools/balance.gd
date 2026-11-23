## Prints how hard each generated level is: what the best choices leave, and how often random
## choices win. Run: scripts/godot.sh --headless --path engine/summandos --script tools/balance.gd
extends SceneTree


func _init() -> void:
	var random := RandomNumberGenerator.new()
	random.seed = 1
	for mode in SummandosLevel.Mode.values():
		for number in [1, 5, 10, 15, 20, 25, 30]:
			var level := SummandosLevel.generate(mode, number)
			var wins := 0
			for attempt in 20:
				var sides: Array[float] = []
				for row in level.rows:
					sides.append(-1.0 if random.randf() < 0.5 else 1.0)
				if SummandosRun.simulate(level, sides).state == SummandosRun.State.WON:
					wins += 1
			print("mode %d level %2d  rows %d  best leaves %4d  random wins %2d/20" % [
				mode, number, level.rows.size(), level.reference, wins])
	quit()
