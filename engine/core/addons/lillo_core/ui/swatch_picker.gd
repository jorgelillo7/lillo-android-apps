## A card of round colour swatches to pick one from, the current one ringed. Emits `picked(name)`
## with the swatch's name, or `closed`.
class_name LilloSwatchPicker
extends Control

signal picked(name: String)
signal closed


## `swatches`: name -> colour, in display order.
func _init(title: String, swatches: Dictionary, selected: String, close_text: String, outline: Color) -> void:
	set_anchors_preset(Control.PRESET_FULL_RECT)
	var centre := CenterContainer.new()
	centre.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(centre)
	var card := LilloTheme.card(outline)
	centre.add_child(card)
	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 18)
	card.add_child(box)
	box.add_child(LilloTheme.title(title, 40))
	var row := HBoxContainer.new()
	row.alignment = BoxContainer.ALIGNMENT_CENTER
	row.add_theme_constant_override("separation", 14)
	box.add_child(row)
	for name in swatches:
		var swatch := Button.new()
		swatch.custom_minimum_size = Vector2(64, 64)
		var face := LilloTheme.box(swatches[name], (swatches[name] as Color).darkened(0.4), false, 32)
		if name == selected:
			face.border_width_top = 6
			face.border_width_left = 6
			face.border_width_right = 6
			face.border_width_bottom = 6
			face.border_color = Color.WHITE
		for state in ["normal", "hover", "pressed"]:
			swatch.add_theme_stylebox_override(state, face)
		swatch.pressed.connect(func() -> void: picked.emit(name))
		LilloFx.squishy(swatch)
		row.add_child(swatch)
	box.add_child(LilloTheme.button(close_text, func() -> void: closed.emit()))
