## Small player preferences kept between sessions in `user://settings.cfg`, shared by every game.
class_name LilloSettings
extends RefCounted

const PATH := "user://settings.cfg"
const SECTION := "player"


static func read(key: String, fallback: Variant, path := PATH) -> Variant:
	var file := ConfigFile.new()
	if file.load(path) != OK:
		return fallback
	return file.get_value(SECTION, key, fallback)


static func write(key: String, value: Variant, path := PATH) -> void:
	var file := ConfigFile.new()
	file.load(path)
	file.set_value(SECTION, key, value)
	file.save(path)
