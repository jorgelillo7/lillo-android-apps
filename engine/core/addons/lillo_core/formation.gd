## Where each member of a crowd stands, relative to its leader: a compact hexagonal blob that grows
## backwards (+z) once it is as wide as the track allows.
class_name LilloFormation
extends RefCounted


## Offsets for `count` members, `spacing` metres apart, never wider than `max_width`.
static func offsets(count: int, spacing: float, max_width: float) -> PackedVector3Array:
	var result := PackedVector3Array()
	if count <= 0:
		return result
	var max_columns := maxi(1, int(max_width / spacing))
	var columns := mini(max_columns, int(ceil(sqrt(float(count)))))
	result.resize(count)
	for i in count:
		var row := i / columns
		var column := i % columns
		var in_row := mini(columns, count - row * columns)
		var stagger := spacing * 0.5 if row % 2 == 1 and in_row < max_columns else 0.0
		var x := (column - (in_row - 1) * 0.5) * spacing + stagger
		result[i] = Vector3(clampf(x, -max_width * 0.5, max_width * 0.5), 0.0, row * spacing)
	return result
