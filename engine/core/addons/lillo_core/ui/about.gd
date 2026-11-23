## An About card: the game's name and version, then credit lines (third-party models, fonts, the
## engine) and a button to close it. Emits `closed`.
class_name LilloAbout
extends Control

signal closed


func _init(title: String, version: String, lines: PackedStringArray, close_text: String, outline: Color) -> void:
	set_anchors_preset(Control.PRESET_FULL_RECT)
	var centre := CenterContainer.new()
	centre.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(centre)
	var card := LilloTheme.card(outline)
	centre.add_child(card)
	var box := VBoxContainer.new()
	box.custom_minimum_size = Vector2(440, 0)
	box.add_theme_constant_override("separation", 14)
	card.add_child(box)
	box.add_child(LilloTheme.title(title, 56))
	box.add_child(LilloTheme.title(version, 24))
	for line in lines:
		var label := LilloTheme.title(line, 22)
		label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
		box.add_child(label)
	box.add_child(LilloTheme.button(close_text, func() -> void: closed.emit()))
