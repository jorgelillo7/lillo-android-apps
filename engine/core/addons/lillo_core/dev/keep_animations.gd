@tool
## Import script for third-party character models: keeps only the animations a game uses, which
## are most of a model's size. The list is the fourth column of models.txt next to the model
## ("<file> <sha256> <url> <animation,animation>"). Set as the model's import_script/path.
extends EditorScenePostImport


func _post_import(scene: Node) -> Object:
	var source := get_source_file()
	var keep := kept(source.get_base_dir().path_join("models.txt"), source.get_file())
	if keep.is_empty():
		return scene
	for player in scene.find_children("*", "AnimationPlayer", true, false):
		for library_name in player.get_animation_library_list():
			var library: AnimationLibrary = player.get_animation_library(library_name)
			for animation in library.get_animation_list():
				if not keep.has(String(animation)):
					library.remove_animation(animation)
	return scene


## The animations listed for `file` in a models.txt, or none when it lists none.
static func kept(manifest: String, file: String) -> PackedStringArray:
	var text := FileAccess.get_file_as_string(manifest)
	for line in text.split("\n"):
		var fields := line.strip_edges().split(" ", false)
		if fields.size() >= 4 and fields[0] == file:
			return fields[3].split(",", false)
	return PackedStringArray()
