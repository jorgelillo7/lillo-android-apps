## A little 3D stage for menus: one character (already built and animating) in its own world,
## lit and framed, drawn on a transparent background so it can stand in a corner of a 2D screen.
class_name LilloModelStage
extends SubViewportContainer

var viewport: SubViewport


## `character` stands on y = 0 and is about `height` metres tall; `turn` is its yaw in radians
## (0 faces the camera when the model faces +z).
func _init(character: Node3D, height: float, pixels: Vector2i, turn := 0.0) -> void:
	stretch = true
	mouse_filter = Control.MOUSE_FILTER_IGNORE
	custom_minimum_size = Vector2(pixels)
	size = Vector2(pixels)
	viewport = SubViewport.new()
	viewport.size = pixels
	viewport.transparent_bg = true
	viewport.own_world_3d = true
	viewport.render_target_update_mode = SubViewport.UPDATE_ALWAYS
	add_child(viewport)
	character.rotation.y = turn
	viewport.add_child(character)
	var sun := DirectionalLight3D.new()
	sun.rotation_degrees = Vector3(-40, 30, 0)
	viewport.add_child(sun)
	var environment := WorldEnvironment.new()
	var env := Environment.new()
	env.background_mode = Environment.BG_CLEAR_COLOR
	env.ambient_light_source = Environment.AMBIENT_SOURCE_COLOR
	env.ambient_light_color = Color(1, 1, 1)
	env.ambient_light_energy = 0.7
	environment.environment = env
	viewport.add_child(environment)
	var camera := Camera3D.new()
	camera.fov = 30.0
	camera.position = Vector3(0, height * 0.55, height * 2.3)
	viewport.add_child(camera)
	camera.look_at.call_deferred(Vector3(0, height * 0.5, 0))
