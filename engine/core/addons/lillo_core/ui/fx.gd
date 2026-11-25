## Small bits of juice every game screen shares: pop-ins, squishy buttons, rising numbers and
## confetti. All are fire-and-forget tweens or particles on the node given.
class_name LilloFx
extends RefCounted


## Scales `node` up from nothing with a little overshoot, after `delay` seconds.
static func pop_in(node: Control, delay := 0.0) -> void:
	node.scale = Vector2.ZERO
	var tween := node.create_tween()
	tween.tween_interval(delay)
	tween.tween_callback(func() -> void: node.pivot_offset = node.size * 0.5)
	tween.tween_property(node, "scale", Vector2.ONE, 0.35).set_trans(Tween.TRANS_BACK).set_ease(Tween.EASE_OUT)


## Makes `button` squash when pressed and spring back when released.
static func squishy(button: BaseButton) -> void:
	button.button_down.connect(func() -> void:
		button.pivot_offset = button.size * 0.5
		button.create_tween().tween_property(button, "scale", Vector2(1.08, 0.9), 0.06))
	button.button_up.connect(func() -> void:
		button.create_tween().tween_property(button, "scale", Vector2.ONE, 0.25) \
			.set_trans(Tween.TRANS_ELASTIC).set_ease(Tween.EASE_OUT))


## A quick bump in size, for a number that just changed.
static func pulse(node: Node3D, amount := 1.3) -> void:
	node.scale = Vector3.ONE * amount
	node.create_tween().tween_property(node, "scale", Vector3.ONE, 0.3).set_trans(Tween.TRANS_BACK)


## Text that rises from `at` (in `parent`'s space, so it travels with it) and fades, e.g. "+25"
## over a squad that just took a gate.
static func rising_text(parent: Node3D, at: Vector3, text: String, colour: Color, outline: Color) -> void:
	var label := Label3D.new()
	label.text = text
	label.font_size = 110
	label.outline_size = 32
	label.pixel_size = 0.01
	label.billboard = BaseMaterial3D.BILLBOARD_ENABLED
	label.no_depth_test = true
	label.modulate = colour
	label.outline_modulate = outline
	label.position = at
	parent.add_child(label)
	var tween := label.create_tween().set_parallel()
	tween.tween_property(label, "position", at + Vector3(0, 1.2, 0), 0.9).set_ease(Tween.EASE_OUT)
	tween.tween_property(label, "modulate:a", 0.0, 0.9).set_delay(0.3)
	tween.chain().tween_callback(label.queue_free)


## A burst of confetti from the top of `parent`, in `colours`.
static func confetti(parent: Control, colours: Array[Color]) -> void:
	for colour in colours:
		var burst := CPUParticles2D.new()
		burst.position = Vector2(parent.size.x * 0.5, -20)
		burst.emitting = false
		burst.one_shot = true
		burst.amount = 40
		burst.lifetime = 2.6
		burst.explosiveness = 0.9
		burst.emission_shape = CPUParticles2D.EMISSION_SHAPE_RECTANGLE
		burst.emission_rect_extents = Vector2(parent.size.x * 0.5, 10)
		burst.direction = Vector2(0, 1)
		burst.spread = 35.0
		burst.initial_velocity_min = 120.0
		burst.initial_velocity_max = 320.0
		burst.gravity = Vector2(0, 260)
		burst.angular_velocity_min = -360.0
		burst.angular_velocity_max = 360.0
		burst.scale_amount_min = 6.0
		burst.scale_amount_max = 12.0
		burst.color = colour
		parent.add_child(burst)
		burst.emitting = true
		burst.finished.connect(burst.queue_free)


## A short message at the bottom of `parent` that fades away after `seconds`, like Android's toast;
## a newer one replaces it.
static func toast(parent: Control, text: String, font_colour: Color, outline: Color, seconds := 2.0) -> void:
	for old in parent.get_children():
		if old.has_meta("lillo_toast"):
			old.queue_free()
	var label := Label.new()
	label.set_meta("lillo_toast", true)
	label.text = text
	label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	label.mouse_filter = Control.MOUSE_FILTER_IGNORE
	label.add_theme_font_size_override("font_size", 30)
	label.add_theme_color_override("font_color", font_colour)
	label.add_theme_color_override("font_outline_color", outline)
	label.add_theme_constant_override("outline_size", 12)
	label.set_anchors_and_offsets_preset(Control.PRESET_CENTER_BOTTOM)
	label.offset_left = -parent.size.x * 0.4
	label.offset_right = parent.size.x * 0.4
	label.offset_top = -200.0
	label.offset_bottom = -140.0
	parent.add_child(label)
	var tween := label.create_tween()
	tween.tween_interval(seconds)
	tween.tween_property(label, "modulate:a", 0.0, 0.4)
	tween.tween_callback(label.queue_free)
