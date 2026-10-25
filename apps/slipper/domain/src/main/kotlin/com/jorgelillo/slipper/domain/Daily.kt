package com.jorgelillo.slipper.domain

/**
 * The run of the day: everyone gets the same house and the same slippers on the same day (the
 * engine is deterministic), so "today's best" can be compared in the group chat.
 */
object Daily {
    fun seed(epochDay: Long): Long = epochDay * 1_000_003 + 20_241_224
}

/**
 * What the grandma throws: cosmetic, unlocked by croquetas eaten over all runs (never with money).
 * [unlockAt] croquetas in total.
 */
enum class SlipperSkin(val unlockAt: Int) {
    CLASSIC(0),
    CHECKED(150),
    FLIP_FLOP(400),
    BUNNY(800);

    fun unlocked(croquetas: Int): Boolean = croquetas >= unlockAt

    companion object {
        /** The next one still locked, to show how far it is. */
        fun next(croquetas: Int): SlipperSkin? = entries.firstOrNull { !it.unlocked(croquetas) }
    }
}
