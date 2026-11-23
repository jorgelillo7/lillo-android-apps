## Writes a number as a small sum to work out in one's head, at three tiers: 0 the plain number,
## 1 an operation with small numbers ("3+4", "21÷3"), 2 adult maths (a power, a square root or a
## one-step equation for x). Every result is exact: the expression always equals the value.
class_name LilloMathPuzzle
extends RefCounted

const SUPERSCRIPTS := {2: "²", 3: "³", 4: "⁴", 5: "⁵", 6: "⁶", 7: "⁷", 8: "⁸", 9: "⁹"}


## {"text": what replaces the number, "note": a second line or "", "hard": worth a pause to think}.
static func express(value: int, tier: int, random: RandomNumberGenerator) -> Dictionary:
	if tier <= 0 or value < 2:
		return {"text": str(value), "note": "", "hard": false}
	if tier == 1:
		return {"text": _operation(value, random), "note": "", "hard": false}
	var forms: Array[Dictionary] = []
	var power := _power(value)
	if power != "":
		forms.append({"text": power, "note": "", "hard": true})
	if value <= 20:
		forms.append({"text": "√%d" % (value * value), "note": "", "hard": true})
	forms.append(_equation(value, random))
	return forms[random.randi_range(0, forms.size() - 1)]


static func _operation(value: int, random: RandomNumberGenerator) -> String:
	var choices := ["sum", "quotient"]
	var factor := _small_factor(value, random)
	if factor > 1:
		choices.append("product")
	if value > 3:
		choices.append("difference")
	match choices[random.randi_range(0, choices.size() - 1)]:
		"sum":
			var a := random.randi_range(1, value - 1)
			return "%d+%d" % [a, value - a]
		"difference":
			var b := random.randi_range(1, 9)
			return "%d−%d" % [value + b, b]
		"product":
			return "%d×%d" % [factor, value / factor]
	var k := random.randi_range(2, 5)
	return "%d÷%d" % [value * k, k]


static func _small_factor(value: int, random: RandomNumberGenerator) -> int:
	var factors: Array[int] = []
	for f in range(2, mini(value, 10)):
		if value % f == 0:
			factors.append(f)
	return factors[random.randi_range(0, factors.size() - 1)] if not factors.is_empty() else 1


## The value as base^exponent with an exponent of 2 or more, or "" when it is not a power.
static func _power(value: int) -> String:
	for exponent in range(9, 1, -1):
		var base := int(round(pow(value, 1.0 / exponent)))
		if base >= 2 and int(pow(base, exponent)) == value:
			return "%d%s" % [base, SUPERSCRIPTS[exponent]]
	return ""


static func _equation(value: int, random: RandomNumberGenerator) -> Dictionary:
	var a := random.randi_range(2, 4)
	var b := random.randi_range(1, 20)
	return {"text": "x", "note": "%dx+%d=%d" % [a, b, a * value + b], "hard": true}
