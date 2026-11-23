extends GutTest


func test_one_offset_per_member() -> void:
	assert_eq(LilloFormation.offsets(0, 0.5, 8.0).size(), 0)
	assert_eq(LilloFormation.offsets(1, 0.5, 8.0).size(), 1)
	assert_eq(LilloFormation.offsets(250, 0.5, 8.0).size(), 250)


func test_a_lone_member_stands_on_the_leader() -> void:
	assert_eq(LilloFormation.offsets(1, 0.5, 8.0)[0], Vector3.ZERO)


func test_a_big_crowd_never_spills_off_the_track() -> void:
	for offset in LilloFormation.offsets(1000, 0.5, 6.0):
		assert_true(absf(offset.x) <= 3.0, "x %f within half the width" % offset.x)
		assert_true(offset.z >= 0.0, "members stand behind the leader")


func test_members_do_not_overlap() -> void:
	var offsets := LilloFormation.offsets(60, 0.5, 8.0)
	for i in offsets.size():
		for j in range(i + 1, offsets.size()):
			assert_true(offsets[i].distance_to(offsets[j]) >= 0.49, "members %d and %d apart" % [i, j])
