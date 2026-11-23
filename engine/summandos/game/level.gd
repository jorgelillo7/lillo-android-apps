## Level data: what waits along the track. A row offers two choices, left and right, each a gate
## or a weapon; a wave is a group of enemies walking towards the squad.
class_name SummandosLevel
extends RefCounted

var length := 0.0
var start_soldiers := 1
## [{z, left, right}] where left/right are {"gate": LilloGate} or {"weapon": damage multiplier}.
var rows: Array[Dictionary] = []
## [{z, count, hp}]: hp is each enemy's.
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
