## An About card: the game's name and version, then one block per credit (what it is in the
## display font, who made it, its licence and a tappable link in a reading font) and a button to
## close it. Emits `closed`.
class_name LilloAbout
extends Control

signal closed


## `credits`: [{"what", "who", "licence", "link"}] ("link" optional, opened in the browser).
## `reading` is the font for names and licences; `accent` colours the headings.
func _init(title: String, version: String, credits: Array, note: String, close_text: String,
		outline: Color, accent: Color, reading: Font) -> void:
	set_anchors_preset(Control.PRESET_FULL_RECT)
	var centre := CenterContainer.new()
	centre.set_anchors_preset(Control.PRESET_FULL_RECT)
	add_child(centre)
	var card := LilloTheme.card(outline)
	centre.add_child(card)
	var box := VBoxContainer.new()
	box.custom_minimum_size = Vector2(460, 0)
	box.add_theme_constant_override("separation", 6)
	card.add_child(box)
	var heading := LilloTheme.title(title, 56)
	heading.add_theme_color_override("font_color", accent)
	box.add_child(heading)
	box.add_child(_text(version, reading, 22, Color(1, 1, 1, 0.7)))
	for credit in credits:
		box.add_child(_gap(14))
		var what := LilloTheme.title(String(credit.what).to_upper(), 22)
		what.add_theme_color_override("font_color", accent)
		box.add_child(what)
		box.add_child(_text(credit.who, reading, 26, Color.WHITE))
		var line: String = credit.licence
		if credit.has("link"):
			box.add_child(_text(line, reading, 20, Color(1, 1, 1, 0.8)))
			box.add_child(_link(String(credit.link), reading, accent))
		else:
			box.add_child(_text(line, reading, 20, Color(1, 1, 1, 0.8)))
	if note != "":
		box.add_child(_gap(16))
		box.add_child(_text(note, reading, 22, Color(1, 1, 1, 0.85)))
	box.add_child(_gap(16))
	box.add_child(LilloTheme.button(close_text, func() -> void: closed.emit()))


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
