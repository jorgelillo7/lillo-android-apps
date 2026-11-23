## Saves a screenshot and quits when the game is run with `-- --shot=<png> [--after=<seconds>]`
## (default 3 s), for docs, store images and checking a change on the Mac. Does nothing otherwise.
class_name LilloCapture
extends Node

var path := ""
var after := 3.0
var _elapsed := 0.0


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
