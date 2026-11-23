## Keeps a container's children clear of display cutouts and system bars, on every resize.
class_name LilloSafeArea
extends RefCounted

const MINIMUM := 16


## Sets `root`'s margins now and whenever the window changes size or turns.
static func attach(root: MarginContainer) -> void:
	fit(root)
	root.get_viewport().size_changed.connect(fit.bind(root))


static func fit(root: MarginContainer) -> void:
	var window := DisplayServer.window_get_size()
	var safe := DisplayServer.get_display_safe_area()
	var scale := root.get_viewport().get_visible_rect().size.x / maxf(1.0, float(window.x))
	var margins := {"margin_left": MINIMUM, "margin_top": MINIMUM, "margin_right": MINIMUM, "margin_bottom": MINIMUM}
	if safe.size.x > 0:
		margins = {
			"margin_left": maxi(MINIMUM, int(safe.position.x * scale)),
			"margin_top": maxi(MINIMUM, int(safe.position.y * scale)),
			"margin_right": maxi(MINIMUM, int((window.x - safe.end.x) * scale)),
			"margin_bottom": maxi(MINIMUM, int((window.y - safe.end.y) * scale)),
		}
	for key in margins:
		root.add_theme_constant_override(key, margins[key])
