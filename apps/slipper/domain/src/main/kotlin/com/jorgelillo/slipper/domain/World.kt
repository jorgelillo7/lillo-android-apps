package com.jorgelillo.slipper.domain

/** An axis-aligned box: [x], [y] its bottom-left corner. */
data class Box(val x: Double, val y: Double, val w: Double, val h: Double) {
    fun overlaps(o: Box): Boolean = x < o.x + o.w && o.x < x + w && y < o.y + o.h && o.y < y + h

    fun shrunk(by: Double) = Box(x + by, y + by, w - 2 * by, h - 2 * by)
}

enum class SlipperKind { LOW, HIGH }

/** A slipper in flight; [x] grows as it flies towards the kid. [spin] is for the drawing. */
data class Slipper(val x: Double, val kind: SlipperKind, val speed: Double, val spin: Double = 0.0) {
    val y: Double get() = if (kind == SlipperKind.LOW) GameConfig.LOW_Y else GameConfig.HIGH_Y
    val box: Box get() = Box(x, y, GameConfig.SLIPPER_W, GameConfig.SLIPPER_H)
}

data class Croqueta(val x: Double, val y: Double) {
    val box: Box get() = Box(x, y, GameConfig.CROQUETA_W, GameConfig.CROQUETA_H)
}

data class Kid(
    val y: Double = 0.0,
    val vy: Double = 0.0,
    /** How long the current jump has been held (up to [GameConfig.MAX_HOLD]). */
    val held: Double = 0.0,
    /** Time since the kid last stood on the floor: jumping still works within the coyote time. */
    val airTime: Double = 0.0,
    /** A tap not yet turned into a jump, and how long ago it came. */
    val buffered: Double? = null
) {
    val onFloor: Boolean get() = y <= 0.0
    val box: Box get() = Box(GameConfig.KID_X, y, GameConfig.KID_W, GameConfig.KID_H)
}

/** The whole run at one moment. Immutable: [Engine.step] returns the next one. */
data class World(
    val seed: Long,
    val time: Double = 0.0,
    val kid: Kid = Kid(),
    val slippers: List<Slipper> = emptyList(),
    val croquetas: List<Croqueta> = emptyList(),
    val eaten: Int = 0,
    /** How far the floor has scrolled: for the drawing. */
    val scrolled: Double = 0.0,
    val nextThrow: Double = GameConfig.FIRST_THROW,
    val throws: Int = 0,
    val nextCroqueta: Double = GameConfig.CROQUETA_GAP_MIN,
    val croquetasSpawned: Int = 0,
    /** Null while running; the slipper that caught the kid once it is over. */
    val caughtBy: Slipper? = null
) {
    val over: Boolean get() = caughtBy != null

    val score: Int get() = (time * GameConfig.POINTS_PER_SECOND).toInt() + eaten * GameConfig.POINTS_PER_CROQUETA

    /** 0 at the start; each croqueta eaten adds [GameConfig.ANGER_PER_CROQUETA]. */
    val anger: Double get() = eaten * GameConfig.ANGER_PER_CROQUETA
}

/** The player's finger during one step: [tapped] once per touch, [holding] while it stays down. */
data class Input(val tapped: Boolean = false, val holding: Boolean = false) {
    companion object {
        val NONE = Input()
    }
}
