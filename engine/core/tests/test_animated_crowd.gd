extends GutTest


func _crowd(poses: int) -> LilloAnimatedCrowd:
	var frames: Array[Mesh] = []
	for i in poses:
		frames.append(BoxMesh.new())
	var crowd := LilloAnimatedCrowd.new(frames, 100)
	add_child_autofree(crowd)
	return crowd


func test_members_spread_over_the_poses() -> void:
	var crowd := _crowd(4)
	var offsets := PackedVector3Array()
	for i in 10:
		offsets.append(Vector3(i, 0, 0))
	crowd.show_at(Vector3.ZERO, offsets)
	var shown := 0
	for view in crowd.poses():
		shown += view.multimesh.visible_instance_count
		assert_between(view.multimesh.visible_instance_count, 2, 3, "each pose draws a share")
	assert_eq(shown, 10, "every member drawn once")


func test_a_single_pose_behaves_like_a_still_crowd() -> void:
	var crowd := _crowd(1)
	crowd.show_points(PackedVector3Array([Vector3.ZERO, Vector3.ONE]))
	assert_eq(crowd.poses()[0].multimesh.visible_instance_count, 2)


func test_play_switches_clip_and_hides_the_other() -> void:
	var crowd := _crowd(2)
	var shooting: Array[Mesh] = [BoxMesh.new(), BoxMesh.new()]
	crowd.add_clip("shoot", shooting)
	crowd.show_points(PackedVector3Array([Vector3.ZERO, Vector3.ONE]))
	crowd.play("shoot")
	crowd.show_points(PackedVector3Array([Vector3.ZERO, Vector3.ONE]))
	var running := 0
	for view in crowd.clips["default"]:
		running += view.multimesh.visible_instance_count
	var aiming := 0
	for view in crowd.clips["shoot"]:
		aiming += view.multimesh.visible_instance_count
	assert_eq(running, 0, "the running poses are hidden")
	assert_eq(aiming, 2)
