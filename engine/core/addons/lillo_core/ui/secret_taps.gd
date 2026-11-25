## Counts quick taps towards a hidden unlock, like Android's "tap build number seven times": each
## tap within `gap` seconds of the last brings it closer; a longer pause starts again.
class_name LilloSecretTaps
extends RefCounted

var needed: int
var gap: float
var _count := 0
var _last := -INF


func _init(taps_needed := 7, max_gap := 1.5) -> void:
	needed = taps_needed
	gap = max_gap


## Records a tap at `now` (seconds) and returns how many taps are still missing: 0 on the tap that
## completes the run, after which counting starts again.
func tap(now: float) -> int:
	_count = _count + 1 if now - _last <= gap else 1
	_last = now
	var left := needed - _count
	if left <= 0:
		_count = 0
		_last = -INF
		return 0
	return left
