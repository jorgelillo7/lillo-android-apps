## A small round button with a drawn icon ("gear", "sound", "muted", "info", "help", "back"), for the
## corners of a screen where a text button would be too loud.
class_name LilloIconButton
extends Button

var kind := "gear"
var ink := Color.WHITE


func _init(icon_kind: String, fill: Color, icon_colour := Color.WHITE, diameter := 64.0) -> void:
	kind = icon_kind
	ink = icon_colour
	custom_minimum_size = Vector2(diameter, diameter)
	focus_mode = Control.FOCUS_NONE
	var face := LilloTheme.box(fill, fill.darkened(0.35), false, int(diameter / 2))
	face.set_content_margin_all(0)
	for state in ["normal", "hover", "pressed", "focus"]:
		add_theme_stylebox_override(state, face)
	LilloFx.squishy(self)


func set_kind(icon_kind: String) -> void:
	kind = icon_kind
	queue_redraw()


func _draw() -> void:
	var c := size * 0.5 - Vector2(0, 2)
	var r := minf(size.x, size.y) * 0.26
	match kind:
		"gear":
			for i in 8:
				var a := i * TAU / 8.0
				draw_set_transform(c, a)
				draw_rect(Rect2(Vector2(-r * 0.22, -r * 1.25), Vector2(r * 0.44, r * 0.5)), ink)
			draw_set_transform(Vector2.ZERO)
			draw_circle(c, r * 0.95, ink)
			draw_circle(c, r * 0.4, get_theme_stylebox("normal").bg_color)
		"sound", "muted":
			var body := PackedVector2Array([c + Vector2(-r, -r * 0.4), c + Vector2(-r * 0.45, -r * 0.4),
				c + Vector2(r * 0.15, -r), c + Vector2(r * 0.15, r), c + Vector2(-r * 0.45, r * 0.4),
				c + Vector2(-r, r * 0.4)])
			draw_colored_polygon(body, ink)
			if kind == "sound":
				draw_arc(c + Vector2(r * 0.2, 0), r * 0.5, -0.9, 0.9, 10, ink, r * 0.18, true)
				draw_arc(c + Vector2(r * 0.2, 0), r * 0.95, -0.9, 0.9, 12, ink, r * 0.18, true)
			else:
				var x := c + Vector2(r * 0.75, 0)
				draw_line(x + Vector2(-r * 0.35, -r * 0.35), x + Vector2(r * 0.35, r * 0.35), ink, r * 0.2, true)
				draw_line(x + Vector2(-r * 0.35, r * 0.35), x + Vector2(r * 0.35, -r * 0.35), ink, r * 0.2, true)
		"info":
			draw_circle(c + Vector2(0, -r * 0.7), r * 0.18, ink)
			draw_rect(Rect2(c + Vector2(-r * 0.14, -r * 0.3), Vector2(r * 0.28, r * 1.1)), ink)
		"help":
			draw_arc(c + Vector2(0, -r * 0.35), r * 0.5, PI * 1.05, TAU + PI * 0.45, 14, ink, r * 0.24, true)
			draw_line(c + Vector2(r * 0.08, r * 0.05), c + Vector2(0, r * 0.4), ink, r * 0.24, true)
			draw_circle(c + Vector2(0, r * 0.85), r * 0.16, ink)
		"back":
			draw_colored_polygon(PackedVector2Array([c + Vector2(-r * 0.8, 0), c + Vector2(r * 0.3, -r * 0.8),
				c + Vector2(r * 0.3, r * 0.8)]), ink)
