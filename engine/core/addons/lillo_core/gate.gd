## A maths gate a crowd runs through: an operation and its operand, e.g. ×3 or +50 %.
class_name LilloGate
extends RefCounted

enum Op { ADD, SUBTRACT, MULTIPLY, DIVIDE, PERCENT }

const SYMBOLS := {Op.ADD: "+", Op.SUBTRACT: "−", Op.MULTIPLY: "×", Op.DIVIDE: "÷", Op.PERCENT: "%"}

var op: Op
var value: int


func _init(gate_op: Op, gate_value: int) -> void:
	op = gate_op
	value = gate_value


## The crowd after the gate: never negative, divisions and percentages rounded down.
func apply(crowd: int) -> int:
	var result := crowd
	match op:
		Op.ADD:
			result = crowd + value
		Op.SUBTRACT:
			result = crowd - value
		Op.MULTIPLY:
			result = crowd * value
		Op.DIVIDE:
			result = crowd / value if value != 0 else crowd
		Op.PERCENT:
			result = crowd * value / 100
	return maxi(result, 0)


## Which of two gates leaves the bigger crowd: 0 or 1, or -1 when they tie.
static func better(left: LilloGate, right: LilloGate, crowd: int) -> int:
	var a := left.apply(crowd)
	var b := right.apply(crowd)
	if a == b:
		return -1
	return 0 if a > b else 1


func label() -> String:
	if op == Op.PERCENT:
		return "%d %%" % value
	return "%s%d" % [SYMBOLS[op], value]
