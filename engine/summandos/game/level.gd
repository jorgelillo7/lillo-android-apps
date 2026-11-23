## Level data: what waits along the track. A row offers two choices, left and right, each a gate
## or a weapon; a wave is a group of enemies walking towards the squad.
class_name SummandosLevel
extends RefCounted

enum Mode { EASY, NORMAL, ADULT }

const LEVELS_PER_MODE := 30
const ROW_GAP := 28.0

var length := 0.0
var start_soldiers := 1
## Soldiers left at the end by the best choices, the yardstick for stars; 0 when unknown.
var reference := 0
## The boss at the end, or empty: {"model": file, "colour": index into BOSS_COLOURS, "height": m}.
var boss := {}

## The skeleton models a boss can be, in turn.
const BOSS_MODELS := ["Skeleton_Warrior.glb", "Skeleton_Mage.glb", "Skeleton_Rogue.glb", "Skeleton_Minion.glb"]
## [{z, left, right, think?}] where left/right are {"gate": LilloGate} or {"weapon": damage multiplier}.
var rows: Array[Dictionary] = []
## [{z, x, count, hp}]: x is the lane the wave comes down (0 the middle), hp each enemy's.
var waves: Array[Dictionary] = []


static func gate(op: LilloGate.Op, value: int) -> Dictionary:
	return {"gate": LilloGate.new(op, value)}


static func weapon(multiplier: float) -> Dictionary:
	return {"weapon": multiplier}


## The first playable level: gates to grow, a weapon that costs a gate, three waves and a boss.
static func first() -> SummandosLevel:
	var level := SummandosLevel.new()
	level.length = 260.0
	level.start_soldiers = 5
	var Op := LilloGate.Op
	level.rows = [
		{"z": 20.0, "left": gate(Op.ADD, 10), "right": gate(Op.MULTIPLY, 2)},
		{"z": 45.0, "left": gate(Op.MULTIPLY, 3), "right": gate(Op.ADD, 25)},
		{"z": 95.0, "left": weapon(2.0), "right": gate(Op.ADD, 30)},
		{"z": 120.0, "left": gate(Op.SUBTRACT, 20), "right": gate(Op.DIVIDE, 2)},
		{"z": 165.0, "left": gate(Op.PERCENT, 150), "right": gate(Op.ADD, 40)},
		{"z": 190.0, "left": gate(Op.MULTIPLY, 2), "right": weapon(3.0)},
	]
	level.waves = [
		{"z": 75.0, "count": 25, "hp": 1.0},
		{"z": 145.0, "count": 60, "hp": 1.5},
		{"z": 215.0, "count": 120, "hp": 2.0},
		{"z": 250.0, "count": 1, "hp": 400.0},
	]
	return level


## The performance test: about 1,000 soldiers and enemies on screen at once.
static func stress() -> SummandosLevel:
	var level := SummandosLevel.new()
	level.length = 200.0
	level.start_soldiers = 400
	level.rows = [{"z": 15.0, "left": gate(LilloGate.Op.ADD, 100), "right": gate(LilloGate.Op.ADD, 100)}]
	level.waves = [
		{"z": 60.0, "count": 500, "hp": 4.0},
		{"z": 110.0, "count": 500, "hp": 4.0},
		{"z": 170.0, "count": 500, "hp": 4.0},
	]
	return level


## Level `number` (from 1) of a mode, the same every time: more rows, harder sums and bigger waves
## as the number grows. Waves are sized from the best choices and shrunk until those choices win.
static func generate(mode: Mode, number: int) -> SummandosLevel:
	var random := RandomNumberGenerator.new()
	random.seed = hash("summandos-%d-%d" % [mode, number])
	var d := clampf((number - 1) / float(LEVELS_PER_MODE - 1), 0.0, 1.0)
	var level := SummandosLevel.new()
	level.start_soldiers = 5 + random.randi_range(0, 5)
	var row_count := 5 + mini(4, number / 6)
	var weapon_row := random.randi_range(2, row_count - 1) if number >= 3 else -1
	var crowd := level.start_soldiers
	var think_left := 1 + int(d * 2.0) if mode == Mode.ADULT else 0
	for i in row_count:
		var row := {"z": 20.0 + i * ROW_GAP}
		var left := _option(mode, d, crowd, random)
		var right := _option(mode, d, crowd, random)
		while LilloGate.better(left.gate, right.gate, crowd) == -1:
			right = _option(mode, d, crowd, random)
		if mode == Mode.ADULT and think_left > 0 and (left.gate.note != "" or left.gate.shown != "") \
				and random.randf() < 0.6:
			row["think"] = true
			think_left -= 1
		crowd = maxi(left.gate.apply(crowd), right.gate.apply(crowd))
		if i == weapon_row:
			var weapon_side := random.randi_range(0, 1)
			if weapon_side == 0:
				left = weapon(2.0 + random.randi_range(0, 1))
			else:
				right = weapon(2.0 + random.randi_range(0, 1))
		row["left"] = left
		row["right"] = right
		level.rows.append(row)
	level.length = 20.0 + row_count * ROW_GAP + 30.0
	level.waves = []
	for i in range(1, row_count, 2):
		level.waves.append({"z": 20.0 + i * ROW_GAP + ROW_GAP * 0.5, "x": random.randf_range(-2.5, 2.5),
			"count": 1, "hp": 1.0 + d})
	var has_boss := number % 5 == 0
	if has_boss:
		level.boss = boss_look(mode, number)
	level.waves.append({"z": level.length - 10.0, "x": 0.0, "count": 1, "hp": 1.0 if not has_boss else 50.0})
	_size_waves(level, d, has_boss)
	return level


## How the boss of level `number` looks: models and colours rotate so no two bosses in a mode
## share both, and each is bigger than the last.
static func boss_look(mode: Mode, number: int) -> Dictionary:
	var turn := maxi(0, number / 5 - 1)
	return {
		"model": BOSS_MODELS[turn % BOSS_MODELS.size()],
		"colour": (turn + mode * 2) % SummandosPalette.BOSS_COLOURS.size(),
		"height": 3.6 + turn * 0.45,
	}


## A gate for a mode: easy keeps to + − ×2 ÷2, normal adds percentages and sums to work out,
## adult writes operands as powers, roots and equations.
static func _option(mode: Mode, d: float, crowd: int, random: RandomNumberGenerator) -> Dictionary:
	var Op := LilloGate.Op
	var ops := [Op.ADD, Op.ADD, Op.SUBTRACT, Op.MULTIPLY, Op.DIVIDE]
	if mode != Mode.EASY:
		ops.append(Op.PERCENT)
	var op: LilloGate.Op = ops[random.randi_range(0, ops.size() - 1)]
	var value := 0
	match op:
		Op.ADD:
			value = random.randi_range(5, 20 + int(d * 40.0))
		Op.SUBTRACT:
			value = random.randi_range(3, maxi(4, mini(crowd - 1, 10 + int(d * 30.0))))
		Op.MULTIPLY:
			value = random.randi_range(2, 3 if mode == Mode.EASY else 4 + int(d * 2.0))
		Op.DIVIDE:
			value = 2 if mode == Mode.EASY else random.randi_range(2, 4)
		Op.PERCENT:
			value = random.randi_range(5, 20) * 10
	var gate := LilloGate.new(op, value)
	var tier := 0
	if mode == Mode.NORMAL and random.randf() < 0.4 + d * 0.5:
		tier = 1
	elif mode == Mode.ADULT:
		tier = 2 if random.randf() < 0.5 + d * 0.4 else 1
	if op != Op.PERCENT and tier > 0:
		var puzzle := LilloMathPuzzle.express(value, tier, random)
		if puzzle.text != str(value):
			gate.shown = puzzle.text
			gate.note = puzzle.note
	return {"gate": gate}


## Sizes the waves from the crowd the best choices bring to each, then shrinks them until the best
## choices win; records what those choices leave standing as the level's reference.
static func _size_waves(level: SummandosLevel, d: float, boss: bool) -> void:
	var sides := best_sides(level)
	var crowd_at := _crowd_by_row(level, sides)
	var strength := 0.25 + d * 0.75
	for attempt in 12:
		for wave in level.waves:
			var crowd := _crowd_before(level, crowd_at, wave.z)
			if wave == level.waves[-1] and boss:
				wave.count = 1
				wave.hp = maxf(20.0, crowd * strength * 6.0)
			else:
				wave.count = maxi(3, int(crowd * strength))
		var run := SummandosRun.simulate(level, sides)
		if run.state == SummandosRun.State.WON and run.soldiers >= maxi(2, level.start_soldiers / 2):
			level.reference = run.soldiers
			return
		strength *= 0.8
	level.reference = maxi(1, SummandosRun.simulate(level, sides).soldiers)


## The side (-1 left, 1 right) to take at each row: the gate leaving more soldiers, and for a
## weapon row whichever of weapon or gate finishes the level with more soldiers.
static func best_sides(level: SummandosLevel) -> Array[float]:
	var sides: Array[float] = []
	var crowd := level.start_soldiers
	var weapon_rows: Array[int] = []
	for row in level.rows:
		if row.left.has("weapon") or row.right.has("weapon"):
			weapon_rows.append(sides.size())
			var gate: LilloGate = row.right.gate if row.left.has("weapon") else row.left.gate
			sides.append(1.0 if row.left.has("weapon") else -1.0)
			crowd = gate.apply(crowd)
		else:
			var pick := LilloGate.better(row.left.gate, row.right.gate, crowd)
			sides.append(-1.0 if pick == 0 else 1.0)
			crowd = (row.left.gate if pick == 0 else row.right.gate).apply(crowd)
	if weapon_rows.is_empty() or level.waves.is_empty():
		return sides
	var with_weapon := sides.duplicate()
	for i in weapon_rows:
		with_weapon[i] = -sides[i]
	var plain := SummandosRun.simulate(level, sides)
	var armed := SummandosRun.simulate(level, with_weapon)
	return with_weapon if armed.soldiers > plain.soldiers else sides


## Soldiers after each row when taking `sides`, ignoring the waves.
static func _crowd_by_row(level: SummandosLevel, sides: Array[float]) -> Array[int]:
	var result: Array[int] = []
	var crowd := level.start_soldiers
	for i in level.rows.size():
		var pick: Dictionary = level.rows[i].left if sides[i] < 0.0 else level.rows[i].right
		if pick.has("gate"):
			crowd = (pick.gate as LilloGate).apply(crowd)
		result.append(crowd)
	return result


static func _crowd_before(level: SummandosLevel, crowd_at: Array[int], z: float) -> int:
	var crowd := level.start_soldiers
	for i in level.rows.size():
		if level.rows[i].z < z:
			crowd = crowd_at[i]
	return crowd
