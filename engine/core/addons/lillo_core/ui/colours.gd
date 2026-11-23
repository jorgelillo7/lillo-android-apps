## Colour checks for palettes: how different two colours look (CIE76 ΔE in Lab), also through
## simulated colour blindness (Machado et al. 2009, full severity), so a game can test that its
## teams and backgrounds stay apart for everyone.
class_name LilloColours
extends RefCounted

const VISIONS := ["normal", "protan", "deutan", "tritan"]
const MATRICES := {
	"protan": [[0.152286, 1.052583, -0.204868], [0.114503, 0.786281, 0.099216], [-0.003882, -0.048116, 1.051998]],
	"deutan": [[0.367322, 0.860646, -0.227968], [0.280085, 0.672501, 0.047413], [-0.011820, 0.042940, 0.968881]],
	"tritan": [[1.255528, -0.076749, -0.178779], [-0.078411, 0.930809, 0.147602], [0.004733, 0.691367, 0.303900]],
}


## ΔE between `a` and `b` as seen with `vision` (one of VISIONS).
static func difference(a: Color, b: Color, vision := "normal") -> float:
	return _lab(_seen(a, vision)).distance_to(_lab(_seen(b, vision)))


## The smallest ΔE between `a` and `b` over every vision.
static func worst_difference(a: Color, b: Color) -> float:
	var worst := INF
	for vision in VISIONS:
		worst = minf(worst, difference(a, b, vision))
	return worst


static func _seen(colour: Color, vision: String) -> Vector3:
	var rgb := Vector3(colour.srgb_to_linear().r, colour.srgb_to_linear().g, colour.srgb_to_linear().b)
	if not MATRICES.has(vision):
		return rgb
	var m: Array = MATRICES[vision]
	return Vector3(
		clampf(m[0][0] * rgb.x + m[0][1] * rgb.y + m[0][2] * rgb.z, 0.0, 1.0),
		clampf(m[1][0] * rgb.x + m[1][1] * rgb.y + m[1][2] * rgb.z, 0.0, 1.0),
		clampf(m[2][0] * rgb.x + m[2][1] * rgb.y + m[2][2] * rgb.z, 0.0, 1.0))


static func _lab(rgb: Vector3) -> Vector3:
	var x := (0.4124 * rgb.x + 0.3576 * rgb.y + 0.1805 * rgb.z) / 0.95047
	var y := 0.2126 * rgb.x + 0.7152 * rgb.y + 0.0722 * rgb.z
	var z := (0.0193 * rgb.x + 0.1192 * rgb.y + 0.9505 * rgb.z) / 1.08883
	var fx := _f(x)
	var fy := _f(y)
	var fz := _f(z)
	return Vector3(116.0 * fy - 16.0, 500.0 * (fx - fy), 200.0 * (fy - fz))


static func _f(t: float) -> float:
	return pow(t, 1.0 / 3.0) if t > 0.008856 else 7.787 * t + 16.0 / 116.0
