## A cartoon character drawn flat for menus and corners: a "soldier" that waves or a "monster"
## that sways, both breathing and blinking. Size it with custom_minimum_size or anchors.
class_name LilloSticker
extends Control

var kind := "soldier"
var body: Color
var face: Color
var accent: Color
var outline: Color
var _time := 0.0


func _init(sticker_kind: String, body_colour: Color, face_colour: Color, accent_colour: Color,
		outline_colour: Color) -> void:
	kind = sticker_kind
	body = body_colour
	face = face_colour
	accent = accent_colour
	outline = outline_colour
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	_time = randf() * 10.0


func _process(delta: float) -> void:
	_time += delta
	queue_redraw()


func _draw() -> void:
	var k := minf(size.x, size.y) / 100.0
	var bob := sin(_time * 2.2) * 2.0 * k
	var blink := fmod(_time, 3.7) < 0.12
	if kind == "monster":
		draw_set_transform(Vector2(size.x * 0.5, size.y + bob), sin(_time * 1.3) * 0.05)
		_monster(k, blink)
	else:
		_soldier(k, blink)


func _blob(centre: Vector2, radius: float, colour: Color, k: float) -> void:
	draw_circle(centre, radius + 2.5 * k, outline)
	draw_circle(centre, radius, colour)


func _eyes(centre: Vector2, apart: float, radius: float, blink: bool, k: float) -> void:
	for side in [-1.0, 1.0]:
		var at := centre + Vector2(side * apart, 0)
		if blink:
			draw_line(at - Vector2(radius, 0), at + Vector2(radius, 0), outline, 2.5 * k)
		else:
			draw_circle(at, radius, Color.WHITE)
			draw_circle(at + Vector2(0, radius * 0.15), radius * 0.55, outline)


func _soldier(k: float, blink: bool) -> void:
	var base := Vector2(size.x * 0.5, size.y + sin(_time * 2.2) * 2.0 * k)
	draw_set_transform(base, 0.0)
	var torso := Rect2(Vector2(-30, -68) * k, Vector2(60, 68) * k)
	draw_rect(torso.grow(2.5 * k), outline)
	draw_rect(torso, body)
	_blob(Vector2(0, -88) * k, 24.0 * k, face, k)
	var helmet := PackedVector2Array()
	for i in 13:
		helmet.append(Vector2(0, -94) * k + Vector2.from_angle(PI + PI * i / 12.0) * 28.0 * k)
	draw_colored_polygon(helmet, accent)
	draw_rect(Rect2(Vector2(-32, -96) * k, Vector2(64, 7) * k), accent)
	_eyes(Vector2(0, -82) * k, 9.0 * k, 5.0 * k, blink, k)
	draw_arc(Vector2(0, -76) * k, 7.0 * k, 0.3, PI - 0.3, 12, outline, 2.5 * k)
	draw_set_transform(base + Vector2(24, -58) * k, 0.55 + sin(_time * 6.0) * 0.4)
	draw_rect(Rect2(Vector2(-8, -40) * k, Vector2(16, 42) * k), outline)
	draw_rect(Rect2(Vector2(-5.5, -38) * k, Vector2(11, 38) * k), body)
	_blob(Vector2(0, -42) * k, 7.5 * k, face, k)
	draw_set_transform(base, 0.0)


func _monster(k: float, blink: bool) -> void:
	for side in [-1.0, 1.0]:
		var horn := PackedVector2Array([Vector2(side * 22, -78) * k, Vector2(side * 40, -112) * k, Vector2(side * 10, -86) * k])
		draw_colored_polygon(horn, accent)
		draw_polyline(horn + PackedVector2Array([horn[0]]), outline, 2.5 * k)
	_blob(Vector2(0, -48) * k, 46.0 * k, body, k)
	draw_circle(Vector2(0, -34) * k, 26.0 * k, face)
	_eyes(Vector2(0, -64) * k, 16.0 * k, 10.0 * k, blink, k)
	var mouth := Rect2(Vector2(-16, -44) * k, Vector2(32, 12) * k)
	draw_rect(mouth, outline)
	for i in 3:
		var x := -12.0 + i * 10.0
		draw_colored_polygon(PackedVector2Array([Vector2(x, -44) * k, Vector2(x + 6, -44) * k, Vector2(x + 3, -38) * k]), Color.WHITE)
