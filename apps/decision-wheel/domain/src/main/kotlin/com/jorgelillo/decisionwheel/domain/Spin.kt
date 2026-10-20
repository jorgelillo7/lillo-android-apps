package com.jorgelillo.decisionwheel.domain

import kotlin.math.abs
import kotlin.math.pow
import kotlin.random.Random

/** Where a spin ends and how long it takes. The landing angle is uniformly random. */
data class SpinPlan(val targetDegrees: Double, val durationMillis: Int)

object Spin {

    /**
     * Plans a spin from [currentDegrees]. A stronger [flingDegreesPerSecond] adds turns and time;
     * without a fling (button) the spin uses a medium strength. Because the final angle is
     * uniform, each segment wins with exactly its share of the circle.
     */
    fun plan(currentDegrees: Double, random: Random, flingDegreesPerSecond: Float? = null): SpinPlan {
        val strength = flingDegreesPerSecond?.let { (abs(it) / 3000f).coerceIn(0.4f, 1.6f) } ?: 1f
        val fullTurns = (4 + strength * 3).toInt() + random.nextInt(2)
        val landing = random.nextDouble(0.0, 360.0)
        val base = currentDegrees - (currentDegrees % 360.0)
        return SpinPlan(
            targetDegrees = base + fullTurns * 360.0 + landing,
            durationMillis = (3800 + strength * 1400).toInt()
        )
    }

    /** Ease-out curve of a wheel slowing down by friction (quartic). */
    fun easeOut(fraction: Float): Float = 1f - (1f - fraction.coerceIn(0f, 1f)).pow(4)
}
