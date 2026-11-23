## A row of star shapes, earned ones filled: for level tiles and result screens.
class_name LilloStars
extends Control

var earned := 0
var total := 3
var radius := 11.0
var fill := Color("#FFD23F")
var empty := Color(1, 1, 1, 0.18)


func _init(earned_stars: int, star_radius: float, fill_colour: Color, of := 3) -> void:
	earned = earned_stars
	radius = star_radius
	fill = fill_colour
	total = of
	custom_minimum_size = Vector2(radius * 2.4 * total, radius * 2.0)
	mouse_filter = Control.MOUSE_FILTER_IGNORE


func _draw() -> void:
	var gap := radius * 2.4
	var start := size.x * 0.5 - gap * (total - 1) * 0.5
	for i in total:
		var points := shape(Vector2(start + i * gap, size.y * 0.5), radius)
		draw_colored_polygon(points, fill if i < earned else empty)
		if i < earned:
			points.append(points[0])
			draw_polyline(points, fill.darkened(0.4), maxf(1.5, radius * 0.12), true)


## The ten points of a five-pointed star around `centre`.
static func shape(centre: Vector2, outer: float) -> PackedVector2Array:
	var points := PackedVector2Array()
	for k in 10:
		var r := outer if k % 2 == 0 else outer * 0.44
		points.append(centre + Vector2.from_angle(-PI / 2.0 + k * PI / 5.0) * r)
	return points
