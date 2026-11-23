## Saves a screenshot and quits when the game is run with `-- --shot=<png> [--after=<seconds>]`
## (default 3 s), for docs, store images and checking a change on the Mac. Does nothing otherwise.
## With `--shot-size=<W>x<H>` the game is drawn off-screen at exactly that size (store images
## taller than the Mac's screen): call `LilloCapture.rehost(self)` first thing in the main scene's
## _ready and return when it says true.
class_name LilloCapture
extends Node

## The UI's base size (project.godot's viewport size); `expand` stretching grows the long side.
const BASE := 720.0

var path := ""
var after := 3.0
var _elapsed := 0.0


## Moves `main` into an off-screen viewport of the --shot-size, stretching its UI like the window
## would, and runs its _ready again there. True when the caller should stop its current _ready.
static func rehost(main: Node) -> bool:
	var size := _shot_size()
	if size == Vector2i.ZERO or main.get_parent() is SubViewport:
		return false
	var move := func() -> void:
		var viewport := SubViewport.new()
		viewport.size = size
		viewport.render_target_update_mode = SubViewport.UPDATE_ALWAYS
		var scale := BASE / minf(size.x, size.y)
		viewport.size_2d_override = Vector2i(roundi(size.x * scale), roundi(size.y * scale))
		viewport.size_2d_override_stretch = true
		var root := main.get_tree().root
		root.add_child(viewport)
		root.remove_child(main)
		main.request_ready()
		viewport.add_child(main)
	move.call_deferred()
	return true


static func _shot_size() -> Vector2i:
	for arg in OS.get_cmdline_user_args():
		if arg.begins_with("--shot-size="):
			var parts := arg.trim_prefix("--shot-size=").split("x")
			return Vector2i(int(parts[0]), int(parts[1]))
	return Vector2i.ZERO


func _ready() -> void:
	for arg in OS.get_cmdline_user_args():
		if arg.begins_with("--shot="):
			path = arg.trim_prefix("--shot=")
		elif arg.begins_with("--after="):
			after = float(arg.trim_prefix("--after="))
	set_process(path != "")


func _process(delta: float) -> void:
	_elapsed += delta
	if _elapsed >= after:
		get_viewport().get_texture().get_image().save_png(path)
		get_tree().quit()
