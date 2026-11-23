## A maths gate a crowd runs through: an operation and its operand, e.g. ×3 or +50 %.
class_name LilloGate
extends RefCounted

enum Op { ADD, SUBTRACT, MULTIPLY, DIVIDE, PERCENT }

const SYMBOLS := {Op.ADD: "+", Op.SUBTRACT: "−", Op.MULTIPLY: "×", Op.DIVIDE: "÷", Op.PERCENT: "%"}

var op: Op
var value: int
## How the operand is written on the gate when it is a puzzle, e.g. "2⁵" or "x"; empty shows the
## plain number.
var shown := ""
## A second line under the gate, e.g. the equation that gives x.
var note := ""


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
	var operand := str(value) if shown == "" else shown
	if shown != "" and shown.length() > 1 and _has_operator(shown):
		operand = "(%s)" % shown
	if op == Op.PERCENT:
		return "%s %%" % operand
	return "%s%s" % [SYMBOLS[op], operand]


static func _has_operator(text: String) -> bool:
	for symbol in ["+", "−", "×", "÷"]:
		if text.contains(symbol):
			return true
	return false
