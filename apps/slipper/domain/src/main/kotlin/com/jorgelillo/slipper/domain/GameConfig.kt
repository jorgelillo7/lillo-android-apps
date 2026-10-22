package com.jorgelillo.slipper.domain

/**
 * Every number that shapes how the run feels, in one place: tune them on a phone. Units are
 * logical (y grows upwards from the floor at 0; x is the distance along the house), seconds and
 * units per second.
 */
object GameConfig {
    /** Fixed simulation step: the same inputs give the same run on every phone (challenge links). */
    const val STEP = 1.0 / 120

    // The kid runs on his own; the camera follows him.
    const val KID_W = 7.0
    const val KID_H = 11.0
    const val RUN_SPEED = 30.0
    const val RUN_SPEED_GAIN = 0.4
    const val MAX_RUN_SPEED = 52.0

    // Jump: tap for a short hop, hold for a higher one (lower gravity while held and rising).
    const val JUMP_SPEED = 70.0
    const val GRAVITY = 260.0
    const val HOLD_GRAVITY = 110.0
    const val MAX_HOLD = 0.22

    /** A tap this long before landing still jumps; a jump this long after leaving a ledge still works. */
    const val JUMP_BUFFER = 0.10
    const val COYOTE_TIME = 0.08

    /** Hit boxes are this much smaller than the drawings on each side: near misses feel fair. */
    const val FORGIVENESS = 0.8

    /** Running into furniture: this long at a fraction of the speed, and she closes in. */
    const val STUMBLE_TIME = 0.5
    const val STUMBLE_SPEED = 0.4

    // The chase: the grandma runs behind at a distance (the gap). It closes when you stumble or a
    // slipper hits you, opens slowly while you run clean, and she catches you at CATCH_GAP.
    const val START_GAP = 24.0
    const val MAX_GAP = 30.0
    const val CATCH_GAP = 2.0
    const val GRANDMA_PACE = 0.96
    const val HIT_LOSS = 4.0

    /** Each croqueta eaten makes her this much angrier: faster slippers, and she runs harder. */
    const val ANGER_PER_CROQUETA = 0.04
    const val ANGER_PACE = 0.12
    const val MAX_PACE = 1.02

    // Slippers leave her hand and fly forwards, faster than the kid runs.
    const val SLIPPER_W = 7.0
    const val SLIPPER_H = 3.0
    const val LOW_Y = 0.5
    const val HIGH_Y = 12.5
    const val SLIPPER_SPEED = 40.0
    const val MAX_SLIPPER_SPEED = 80.0
    const val FIRST_THROW = 2.5
    const val THROW_GAP_START = 2.2
    const val THROW_GAP_MIN = 0.8
    const val THROW_GAP_SHRINK = 0.03
    const val THROW_JITTER = 0.8

    /** No high slippers in the first seconds: the first lesson is "jump". */
    const val HIGH_FROM = 6.0
    const val HIGH_SHARE = 0.35

    // The house, built ahead of the kid in chunks; how far ahead and behind it is kept.
    const val BUILD_AHEAD = 140.0
    const val KEEP_BEHIND = 60.0
    const val CROQUETA_W = 4.0
    const val CROQUETA_H = 3.0

    // Score: distance run plus croquetas.
    const val POINTS_PER_UNIT = 0.3
    const val POINTS_PER_CROQUETA = 10
}
