## Stars earned per mode and level, kept between sessions; a level opens once the one before it
## has been won.
class_name SummandosProgress
extends RefCounted


static func stars(mode: SummandosLevel.Mode, number: int, path := LilloSettings.PATH) -> int:
	var earned: Array = LilloSettings.read("stars_%d" % mode, [], path)
	return earned[number - 1] if number - 1 < earned.size() else 0


## Keeps the best result: a worse run never takes stars away.
static func record(mode: SummandosLevel.Mode, number: int, earned: int, path := LilloSettings.PATH) -> void:
	var all: Array = LilloSettings.read("stars_%d" % mode, [], path)
	while all.size() < number:
		all.append(0)
	all[number - 1] = maxi(all[number - 1], earned)
	LilloSettings.write("stars_%d" % mode, all, path)


static func unlocked(mode: SummandosLevel.Mode, number: int, path := LilloSettings.PATH) -> bool:
	return number == 1 or stars(mode, number - 1, path) > 0
