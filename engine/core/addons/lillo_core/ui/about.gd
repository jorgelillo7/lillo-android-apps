## An About card: the game's name and version, then one block per credit (what it is in the
## display font, who made it, its licence and a tappable link in a reading font) and a button to
## close it. The credits sit side by side on a wide screen so the card fits its height. Emits
## `closed`.
class_name LilloAbout
extends Control

signal closed


## `credits`: [{"what", "who", "licence", "link"}] ("link" optional, opened in the browser).
## `reading` is the font for names and licences; `accent` colours the headings; `story`, when
## given, is a line under the version (where the name comes from, say).
func _init(title: String, version: String, credits: Array, note: String, close_text: String,
		outline: Color, accent: Color, reading: Font, story := "") -> void:
	set_anchors_preset(Control.PRESET_FULL_RECT)
	var centre := CenterContainer.new()
	centre.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(centre)
	var card := LilloTheme.card(outline)
	centre.add_child(card)
	var box := VBoxContainer.new()
	box.add_theme_constant_override("separation", 6)
	card.add_child(box)
	var grid := GridContainer.new()
	grid.add_theme_constant_override("h_separation", 28)
	grid.add_theme_constant_override("v_separation", 0)
	var heading := LilloTheme.title(title, 56)
	heading.add_theme_color_override("font_color", accent)
	box.add_child(heading)
	box.add_child(_text(version, reading, 22, Color(1, 1, 1, 0.7)))
	if story != "":
		box.add_child(_gap(6))
		box.add_child(_text(story, reading, 22, Color.WHITE))
	box.add_child(grid)
	for credit in credits:
		var block := VBoxContainer.new()
		block.size_flags_horizontal = Control.SIZE_EXPAND_FILL
		block.add_theme_constant_override("separation", 2)
		grid.add_child(block)
		block.add_child(_gap(10))
		var what := LilloTheme.title(String(credit.what).to_upper(), 22)
		what.add_theme_color_override("font_color", accent)
		block.add_child(what)
		block.add_child(_text(credit.who, reading, 24, Color.WHITE))
		block.add_child(_text(credit.licence, reading, 20, Color(1, 1, 1, 0.8)))
		if credit.has("link"):
			block.add_child(_link(String(credit.link), reading, accent))
	if note != "":
		box.add_child(_gap(16))
		box.add_child(_text(note, reading, 22, Color(1, 1, 1, 0.85)))
	box.add_child(_gap(16))
	box.add_child(LilloTheme.button(close_text, func() -> void: closed.emit()))
	var lay_out := func() -> void:
		var area := get_viewport_rect().size
		var wide := area.x > area.y * 1.2
		grid.columns = mini(3, credits.size()) if wide else 1
		box.custom_minimum_size.x = minf(area.x - 80.0, 880.0) if wide else 460.0
	resized.connect(lay_out)


static func _text(text: String, font: Font, size: int, colour: Color) -> Label:
	var label := Label.new()
	label.text = text
	label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	label.autowrap_mode = TextServer.AUTOWRAP_WORD_SMART
	label.add_theme_font_override("font", font)
	label.add_theme_font_size_override("font_size", size)
	label.add_theme_color_override("font_color", colour)
	label.add_theme_constant_override("outline_size", 0)
	return label


## A web address that opens in the browser when tapped: in the accent colour and underlined, so
## it reads as something to press.
static func _link(address: String, font: Font, accent: Color) -> Control:
	var link := LinkButton.new()
	link.text = address
	link.underline = LinkButton.UNDERLINE_MODE_ALWAYS
	link.size_flags_horizontal = Control.SIZE_SHRINK_CENTER
	link.add_theme_font_override("font", font)
	link.add_theme_font_size_override("font_size", 21)
	link.add_theme_color_override("font_color", accent)
	link.add_theme_color_override("font_hover_color", accent.lightened(0.3))
	link.add_theme_color_override("font_pressed_color", accent.lightened(0.5))
	link.pressed.connect(OS.shell_open.bind("https://" + address))
	return link


static func _gap(height: float) -> Control:
	var gap := Control.new()
	gap.custom_minimum_size = Vector2(0, height)
	return gap
