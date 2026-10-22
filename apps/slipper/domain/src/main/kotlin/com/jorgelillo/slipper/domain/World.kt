package com.jorgelillo.slipper.domain

/** An axis-aligned box: [x], [y] its bottom-left corner. */
data class Box(val x: Double, val y: Double, val w: Double, val h: Double) {
    fun overlaps(o: Box): Boolean = x < o.x + o.w && o.x < x + w && y < o.y + o.h && o.y < y + h

    fun shrunk(by: Double) = Box(x + by, y + by, w - 2 * by, h - 2 * by)
}

enum class SlipperKind { LOW, HIGH }

/** A slipper in flight; [x] grows as it flies after the kid. [spin] is for the drawing. */
data class Slipper(val x: Double, val kind: SlipperKind, val speed: Double, val spin: Double = 0.0) {
    val y: Double get() = if (kind == SlipperKind.LOW) GameConfig.LOW_Y else GameConfig.HIGH_Y
    val box: Box get() = Box(x, y, GameConfig.SLIPPER_W, GameConfig.SLIPPER_H)
}

data class Croqueta(val x: Double, val y: Double) {
    val box: Box get() = Box(x, y, GameConfig.CROQUETA_W, GameConfig.CROQUETA_H)
}

/** Furniture: you can land on it and run across the top; running into its side makes you stumble. */
enum class FurnitureKind(val w: Double, val h: Double) {
    STOOL(4.0, 5.0),
    CHAIR(6.0, 8.0),
    SOFA(16.0, 7.0),
    TABLE(18.0, 10.0)
}

data class Furniture(val kind: FurnitureKind, val x: Double, val bumped: Boolean = false) {
    val top: Double get() = kind.h
    val box: Box get() = Box(x, 0.0, kind.w, kind.h)
}

/** The rooms the house goes through; only the drawing changes. */
enum class Room { KITCHEN, HALL, LIVING_ROOM }

/** A stretch of the house: where it starts and ends, and which room it is. */
data class Chunk(val index: Int, val start: Double, val end: Double, val room: Room)

data class Kid(
    val y: Double = 0.0,
    val vy: Double = 0.0,
    /** How long the current jump has been held (up to [GameConfig.MAX_HOLD]). */
    val held: Double = 0.0,
    /** Time since the kid last stood on something: jumping still works within the coyote time. */
    val airTime: Double = 0.0,
    /** A tap not yet turned into a jump, and how long ago it came. */
    val buffered: Double? = null,
    /** Seconds of stumbling left (running slowly after bumping into furniture or a slipper). */
    val stumble: Double = 0.0
) {
    val standing: Boolean get() = airTime == 0.0
}

/** The whole run at one moment. Immutable: [Engine.step] returns the next one. */
data class World(
    val seed: Long,
    val time: Double = 0.0,
    /** How far the kid has run: his x. */
    val distance: Double = 0.0,
    val kid: Kid = Kid(),
    /** How far behind him the grandma runs. */
    val gap: Double = GameConfig.START_GAP,
    val chunks: List<Chunk> = emptyList(),
    val furniture: List<Furniture> = emptyList(),
    val croquetas: List<Croqueta> = emptyList(),
    val slippers: List<Slipper> = emptyList(),
    val eaten: Int = 0,
    val nextThrow: Double = GameConfig.FIRST_THROW,
    val throws: Int = 0,
    val caught: Boolean = false
) {
    val over: Boolean get() = caught

    val score: Int get() = (distance * GameConfig.POINTS_PER_UNIT).toInt() + eaten * GameConfig.POINTS_PER_CROQUETA

    /** 0 at the start; each croqueta eaten adds [GameConfig.ANGER_PER_CROQUETA]. */
    val anger: Double get() = eaten * GameConfig.ANGER_PER_CROQUETA

    val kidBox: Box get() = Box(distance, kid.y, GameConfig.KID_W, GameConfig.KID_H)

    /** Where the grandma is (her x). */
    val grandmaX: Double get() = distance - gap

    fun roomAt(x: Double): Room = chunks.lastOrNull { it.start <= x }?.room ?: Room.KITCHEN
}

/** The player's finger during one step: [tapped] once per touch, [holding] while it stays down. */
data class Input(val tapped: Boolean = false, val holding: Boolean = false) {
    companion object {
        val NONE = Input()
    }
}
