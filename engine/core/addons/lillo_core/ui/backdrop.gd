## A cheerful animated backdrop for menus: sun rays from below, drifting clouds and two rows of
## rolling hills, all from the colours given. Fills its rect; draws nothing interactive.
class_name LilloBackdrop
extends Control

var sky_top: Color
var sky: Color
var hill_far: Color
var hill_near: Color
var cloud: Color
var _time := 0.0
var _rays: ColorRect


func _init(colours: Dictionary) -> void:
	sky_top = colours.sky_top
	sky = colours.sky
	hill_far = colours.hill_far
	hill_near = colours.hill_near
	cloud = colours.cloud
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	set_anchors_preset(Control.PRESET_FULL_RECT)
	_rays = ColorRect.new()
	_rays.set_anchors_preset(Control.PRESET_FULL_RECT)
	_rays.mouse_filter = Control.MOUSE_FILTER_IGNORE
	var shader := Shader.new()
	shader.code = """
shader_type canvas_item;
uniform vec4 light : source_color;
uniform vec4 dark : source_color;
void fragment() {
	vec2 p = UV - vec2(0.5, 1.1);
	float angle = atan(p.x, -p.y) + TIME * 0.03;
	float ray = step(0.5, fract(angle * 3.5));
	vec3 colour = mix(dark.rgb, light.rgb, ray * 0.3 + (1.0 - length(p)) * 0.6);
	COLOR = vec4(colour, 1.0);
}
"""
	var material := ShaderMaterial.new()
	material.shader = shader
	material.set_shader_parameter("light", sky)
	material.set_shader_parameter("dark", sky_top)
	_rays.material = material
	add_child(_rays)
	_rays.show_behind_parent = true


func _process(delta: float) -> void:
	_time += delta
	queue_redraw()


func _draw() -> void:
	var w := size.x
	var h := size.y
	for i in 4:
		var speed := 12.0 + i * 5.0
		var x := fposmod(i * w * 0.31 + _time * speed, w + 240.0) - 120.0
		_cloud(Vector2(x, h * (0.12 + 0.09 * (i % 3))), 26.0 + 8.0 * (i % 2))
	_hills(h * 0.8, 70.0, w / 2.6, hill_far, 0.0)
	_hills(h * 0.88, 60.0, w / 1.8, hill_near, 1.7)


func _cloud(at: Vector2, r: float) -> void:
	for offset in [Vector2(-1.1, 0.2), Vector2(0, -0.3), Vector2(1.1, 0.2), Vector2(0.4, 0.35), Vector2(-0.5, 0.35)]:
		draw_circle(at + offset * r, r, cloud)


func _hills(top: float, height: float, wave: float, colour: Color, phase: float) -> void:
	var points := PackedVector2Array()
	var steps := 48
	for i in steps + 1:
		var x := size.x * i / float(steps)
		points.append(Vector2(x, top - height * 0.5 * (1.0 + sin(x / wave * TAU * 0.5 + phase))))
	points.append(Vector2(size.x, size.y))
	points.append(Vector2(0, size.y))
	draw_colored_polygon(points, colour)
