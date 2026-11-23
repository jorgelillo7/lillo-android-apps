## A card of short sections (a heading in the display font, a line or two in a reading font), for
## help screens. Scrolls when it does not fit. Emits `closed`.
class_name LilloInfoCard
extends Control

signal closed


## `sections`: [{"heading", "body"}].
func _init(title: String, sections: Array, close_text: String, outline: Color, accent: Color, reading: Font) -> void:
	set_anchors_preset(Control.PRESET_FULL_RECT)
	var centre := CenterContainer.new()
	centre.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(centre)
	var card := LilloTheme.card(outline)
	centre.add_child(card)
	var column := VBoxContainer.new()
	column.add_theme_constant_override("separation", 12)
	card.add_child(column)
	var heading := LilloTheme.title(title, 44)
	heading.add_theme_color_override("font_color", accent)
	column.add_child(heading)
	var scroll := ScrollContainer.new()
	scroll.horizontal_scroll_mode = ScrollContainer.SCROLL_MODE_DISABLED
	scroll.custom_minimum_size = Vector2(470, 0)
	column.add_child(scroll)
	var box := VBoxContainer.new()
	box.size_flags_horizontal = Control.SIZE_EXPAND_FILL
	box.add_theme_constant_override("separation", 4)
	scroll.add_child(box)
	for section in sections:
		var name := LilloTheme.title(String(section.heading).to_upper(), 24)
		name.add_theme_color_override("font_color", accent)
		box.add_child(name)
		box.add_child(LilloAbout._text(section.body, reading, 22, Color.WHITE))
		box.add_child(LilloAbout._gap(8))
	column.add_child(LilloTheme.button(close_text, func() -> void: closed.emit()))
	resized.connect(func() -> void:
		var area := get_viewport().get_visible_rect().size
		scroll.custom_minimum_size.y = minf(box.get_combined_minimum_size().y, area.y - 300.0))
