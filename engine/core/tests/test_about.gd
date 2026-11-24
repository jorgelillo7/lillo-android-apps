extends GutTest


func _about(privacy: Array) -> LilloAbout:
	var credits := [{"what": "Engine", "who": "Godot", "licence": "MIT", "link": "godotengine.org"}]
	var about := LilloAbout.new("Game", "Version 1", credits, "No data", "Close", Color.BLACK, Color.YELLOW,
		SystemFont.new(), "", privacy)
	add_child_autofree(about)
	return about


func _links(about: LilloAbout) -> Array[String]:
	var texts: Array[String] = []
	for node in about.find_children("*", "LinkButton", true, false):
		texts.append((node as LinkButton).text)
	return texts


func test_the_privacy_policy_link_shows_its_name() -> void:
	assert_eq(_links(_about(["Privacy policy", "example.com/privacy/"])), ["godotengine.org", "Privacy policy"] as Array[String])


func test_without_a_privacy_policy_only_the_credits_link() -> void:
	assert_eq(_links(_about([])), ["godotengine.org"] as Array[String])
