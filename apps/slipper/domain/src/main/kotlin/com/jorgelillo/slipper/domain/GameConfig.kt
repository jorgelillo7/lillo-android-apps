package com.jorgelillo.slipper.domain

/**
 * Every number that shapes how the run feels, in one place: tune them on a phone. Units are
 * logical (the visible world is [WORLD_WIDTH] wide, y grows upwards from the floor at 0), seconds
 * and units per second.
 */
object GameConfig {
    /** Fixed simulation step: the same inputs give the same run on every phone (challenge links). */
    const val STEP = 1.0 / 120

    const val WORLD_WIDTH = 100.0

    // The kid: stays at a fixed x while the world scrolls.
    const val KID_X = 60.0
    const val KID_W = 7.0
    const val KID_H = 11.0

    // Jump: tap for a short hop, hold for a higher one (lower gravity while held and rising).
    const val JUMP_SPEED = 70.0
    const val GRAVITY = 260.0
    const val HOLD_GRAVITY = 110.0
    const val MAX_HOLD = 0.22

    /** A tap this long before landing still jumps; a jump this long after leaving the floor still works. */
    const val JUMP_BUFFER = 0.10
    const val COYOTE_TIME = 0.08

    /** Hit boxes are this much smaller than the drawings on each side: near misses feel fair. */
    const val FORGIVENESS = 0.8

    // The grandma, on the left; slippers leave her hand and fly right, towards the kid.
    const val GRANDMA_X = 12.0
    const val THROW_X = 18.0
    const val SLIPPER_W = 7.0
    const val SLIPPER_H = 3.0
    const val LOW_Y = 0.5
    const val HIGH_Y = 12.5
    const val SLIPPER_SPEED = 45.0
    const val MAX_SLIPPER_SPEED = 90.0

    /** Each croqueta eaten makes her this much angrier: her slippers fly faster. */
    const val ANGER_PER_CROQUETA = 0.04

    const val FIRST_THROW = 1.5
    const val THROW_GAP_START = 1.6
    const val THROW_GAP_MIN = 0.55
    const val THROW_GAP_SHRINK = 0.035
    const val THROW_JITTER = 0.5

    /** No high slippers in the first seconds: the first lesson is "jump". */
    const val HIGH_FROM = 4.0
    const val HIGH_SHARE = 0.35

    // Croquetas come from the right with the scrolling world.
    const val WORLD_SPEED = 30.0
    const val WORLD_SPEED_GAIN = 0.5
    const val MAX_WORLD_SPEED = 55.0
    const val CROQUETA_W = 4.0
    const val CROQUETA_H = 3.0
    const val CROQUETA_GAP_MIN = 0.9
    const val CROQUETA_GAP_MAX = 1.8
    const val CROQUETA_AIR_SHARE = 0.5

    // Score: survival time plus croquetas.
    const val POINTS_PER_SECOND = 10
    const val POINTS_PER_CROQUETA = 10
}
