## The cartoon look for menus every game shares: a chunky font, outlined text and rounded buttons
## with a darker lip that squash when pressed. Colours come from the game's palette.
class_name LilloTheme
extends RefCounted


## A Theme for a whole UI tree: `accent` buttons with white outlined text.
static func build(font: Font, accent: Color, outline: Color) -> Theme:
	var theme := Theme.new()
	theme.default_font = font
	theme.set_color("font_color", "Button", Color.WHITE)
	theme.set_color("font_outline_color", "Button", outline)
	theme.set_constant("outline_size", "Button", 10)
	theme.set_stylebox("normal", "Button", box(accent, accent.darkened(0.35)))
	theme.set_stylebox("hover", "Button", box(accent.lightened(0.1), accent.darkened(0.35)))
	theme.set_stylebox("pressed", "Button", box(accent.darkened(0.15), accent.darkened(0.45), true))
	theme.set_stylebox("disabled", "Button", box(Color(0.55, 0.55, 0.6), Color(0.35, 0.35, 0.4)))
	theme.set_stylebox("focus", "Button", StyleBoxEmpty.new())
	theme.set_color("font_outline_color", "Label", outline)
	theme.set_constant("outline_size", "Label", 12)
	return theme


## A rounded box with a darker lip under it; `pressed` flattens the lip.
static func box(fill: Color, lip: Color, pressed := false, radius := 18) -> StyleBoxFlat:
	var style := StyleBoxFlat.new()
	style.bg_color = fill
	style.border_color = lip
	style.border_width_bottom = 2 if pressed else 8
	style.border_width_top = 6 if pressed else 0
	style.set_corner_radius_all(radius)
	style.set_content_margin_all(10)
	style.shadow_color = Color(0, 0, 0, 0.25)
	style.shadow_size = 4
	style.shadow_offset = Vector2(0, 3)
	return style


## A button that runs `action` and squashes when pressed.
static func button(text: String, action: Callable, font_size := 30, height := 72.0) -> Button:
	var result := Button.new()
	result.text = text
	result.custom_minimum_size = Vector2(0, height)
	result.add_theme_font_size_override("font_size", font_size)
	result.pressed.connect(action)
	LilloFx.squishy(result)
	return result


## A round button in `fill`, for arrows beside a list.
static func round_button(text: String, action: Callable, fill: Color, lip: Color) -> Button:
	var result := button(text, action, 36, 64.0)
	result.custom_minimum_size = Vector2(64, 64)
	result.size_flags_vertical = Control.SIZE_SHRINK_CENTER
	result.add_theme_stylebox_override("normal", box(fill, lip, false, 32))
	return result


## Centred outlined text.
static func title(text: String, font_size: int) -> Label:
	var label := Label.new()
	label.text = text
	label.horizontal_alignment = HORIZONTAL_ALIGNMENT_CENTER
	label.add_theme_font_size_override("font_size", font_size)
	return label


## A dark rounded card to hold text and buttons over a busy scene.
static func card(outline: Color) -> PanelContainer:
	var panel := PanelContainer.new()
	var style := StyleBoxFlat.new()
	style.bg_color = Color(outline, 0.72)
	style.set_corner_radius_all(24)
	style.set_content_margin_all(28)
	panel.add_theme_stylebox_override("panel", style)
	return panel
