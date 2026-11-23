## Level data: what waits along the track. A row offers two choices, left and right, each a gate
## or a weapon; a wave is a group of enemies walking towards the squad.
class_name SummandosLevel
extends RefCounted

enum Mode { EASY, NORMAL, ADULT, EXPERT }

const LEVELS_PER_MODE := 30
## Expert is a short, brutal set: two-step sums at every gate, and the worse gate loses the level.
const EXPERT_LEVELS := 10
const ROW_GAP := 28.0
## The most enemies one wave holds.
const MAX_WAVE := 300

var length := 0.0
var start_soldiers := 1
## Soldiers left at the end by the best choices, the yardstick for stars; 0 when unknown.
var reference := 0
## Taking the worse gate of a pair loses the level at once (expert).
var strict := false
## How long a "think" row stops the run, in seconds.
var think_seconds := 5.0
## The boss at the end, or empty: {"model": file, "colour": index into BOSS_COLOURS, "height": m}.
var boss := {}

## The bosses that throw fireballs.
const RANGED_BOSSES := ["Skeleton_Mage.glb", "Necromancer.glb"]
## The skeleton models a boss can be, in turn. The Golem and the Necromancer come from a bought
## pack; without it SummandosModels draws a free stand-in, so a level is the same either way.
const BOSS_MODELS := ["Skeleton_Warrior.glb", "Skeleton_Mage.glb", "Skeleton_Golem.glb", "Skeleton_Rogue.glb",
	"Necromancer.glb", "Skeleton_Minion.glb"]
## [{z, left, right, think?}] where left/right are {"gate": LilloGate} or {"weapon": damage multiplier}.
var rows: Array[Dictionary] = []
## [{z, x, count, hp, boss?, bite?, burn?}]: x is the lane the wave comes down (0 the middle), hp
## each enemy's; a boss wave is one enemy that eats `bite` soldiers a second once it reaches the
## squad, and a ranged one throws fireballs that burn `burn` soldiers each.
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
		{"z": 250.0, "count": 1, "hp": 400.0, "boss": true, "bite": 8.0},
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


## How many levels `mode` has.
static func level_count(mode: Mode) -> int:
	return EXPERT_LEVELS if mode == Mode.EXPERT else LEVELS_PER_MODE


## Level `number` (from 1) of a mode, the same every time: more rows, harder sums and bigger waves
## as the number grows. Waves are sized from the best choices and shrunk until those choices win.
static func generate(mode: Mode, number: int) -> SummandosLevel:
	var random := RandomNumberGenerator.new()
	random.seed = hash("summandos-%d-%d" % [mode, number])
	var d := clampf((number - 1) / float(level_count(mode) - 1), 0.0, 1.0)
	var expert := mode == Mode.EXPERT
	var level := SummandosLevel.new()
	level.strict = expert
	level.start_soldiers = 5 + random.randi_range(0, 5)
	var row_count := 6 + number / 3 if expert else 5 + mini(4, number / 6)
	var weapon_row := random.randi_range(2, row_count - 1) if number >= 3 and not expert else -1
	var crowd := level.start_soldiers
	var think_left := 1 + int(d * 2.0) if mode == Mode.ADULT else 0
	for i in row_count:
		var row := {"z": 20.0 + i * ROW_GAP}
		var left := _option(mode, d, crowd, random)
		var right := _option(mode, d, crowd, random)
		while LilloGate.better(left.gate, right.gate, crowd) == -1:
			right = _option(mode, d, crowd, random)
		if expert or (mode == Mode.ADULT and think_left > 0 and (left.gate.note != "" or left.gate.shown != "") \
				and random.randf() < 0.6):
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
	level.waves.append({"z": level.length - 10.0, "x": 0.0, "count": 1, "hp": 1.0, "boss": has_boss,
		"ranged": has_boss and level.boss.model in RANGED_BOSSES})
	_size_waves(level, mode, d)
	return level


## How the boss of level `number` looks: models and colours rotate so no two bosses in a mode
## share both, and each is bigger than the last.
static func boss_look(mode: Mode, number: int) -> Dictionary:
	var turn := maxi(0, number / 5 - 1)
	return {
		"model": BOSS_MODELS[turn % BOSS_MODELS.size()],
		"colour": (turn + turn / BOSS_MODELS.size() + mode * 2) % SummandosPalette.BOSS_COLOURS.size(),
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
	elif mode == Mode.EXPERT:
		tier = 3
	if op != Op.PERCENT and tier > 0:
		var puzzle := LilloMathPuzzle.express(value, tier, random)
		if puzzle.text != str(value):
			gate.shown = puzzle.text
			gate.note = puzzle.note
	return {"gate": gate}


## What share of the soldiers the best choices should still have at the end of a level: the waves
## and the boss take the rest. Lower for later levels and harder modes.
static func keep_share(mode: Mode, d: float) -> float:
	return 0.85 - d * 0.2 - mode * 0.1


## Sizes the waves and the boss from the crowd the best choices bring to each, scaled until those
## choices finish with keep_share() of their soldiers; records what they leave as the reference.
## Enemies get tougher with the level and the mode, so a weapon is worth more than a few soldiers.
static func _size_waves(level: SummandosLevel, mode: Mode, d: float) -> void:
	var enemy_hp := 2.0 + d * 3.0 + mode
	var boss_seconds := 6.0 + d * 3.0 + mode * 1.5
	var sides := best_sides(level)
	var crowd_at := _crowd_by_row(level, sides)
	var keep := keep_share(mode, d)
	var goal := maxf(2.0, crowd_at[-1] * keep)
	var low := 0.02
	var high := 8.0
	for attempt in 16:
		var scale := sqrt(low * high)
		_scale_waves(level, crowd_at, scale, enemy_hp, boss_seconds, keep)
		var run := SummandosRun.simulate(level, sides)
		if run.state == SummandosRun.State.WON and run.soldiers >= goal:
			low = scale
		else:
			high = scale
	_scale_waves(level, crowd_at, low, enemy_hp, boss_seconds, keep)
	sides = best_sides(level)
	level.reference = maxi(1, SummandosRun.simulate(level, sides).soldiers)


## A boss lasts about `boss_seconds` under the fire of the crowd expected to reach it (`keep` of
## the gates' crowd) armed with the level's weapon, so skipping the weapon makes the fight longer,
## and eats a tenth of that crowd over the fight; the other waves grow with `scale`. Past MAX_WAVE enemies a wave stops
## growing and its enemies get tougher instead, so the screen never holds more than it can draw.
static func _scale_waves(level: SummandosLevel, crowd_at: Array[int], scale: float, enemy_hp: float,
		boss_seconds: float, keep: float) -> void:
	for wave in level.waves:
		var crowd := _crowd_before(level, crowd_at, wave.z)
		if wave.get("boss", false):
			crowd = maxi(1, int(crowd * keep))
			wave.count = 1
			wave.hp = maxf(30.0, crowd * SummandosRun.SHOTS_PER_SECOND * boss_seconds * sqrt(_weapon_power(level)))
			wave.bite = maxf(1.0, crowd * 0.1 / boss_seconds)
			if wave.get("ranged", false):
				wave.burn = maxi(1, int(crowd * 0.04))
		else:
			var wanted := maxf(3.0, crowd * scale)
			wave.count = int(minf(wanted, MAX_WAVE))
			wave.hp = enemy_hp * wanted / wave.count


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


## The damage the level's weapons give together; 1 without any.
static func _weapon_power(level: SummandosLevel) -> float:
	var power := 1.0
	for row in level.rows:
		for side in [row.left, row.right]:
			if side.has("weapon"):
				power *= side.weapon
	return power


static func _crowd_before(level: SummandosLevel, crowd_at: Array[int], z: float) -> int:
	var crowd := level.start_soldiers
	for i in level.rows.size():
		if level.rows[i].z < z:
			crowd = crowd_at[i]
	return crowd
