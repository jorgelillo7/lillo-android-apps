package com.jorgelillo.christmascountdown.domain

import java.time.LocalDate
import java.time.Month

/** Advent calendar rules: door N opens on December N, from the 1st to the 24th. */
object AdventCalendar {

    const val DOORS = 24

    /** How many doors can be opened on [today]: 0 outside December, all of them from the 24th on. */
    fun unlockedDoors(today: LocalDate): Int = if (today.month ==
        Month.DECEMBER
    ) {
        today.dayOfMonth.coerceAtMost(DOORS)
    } else {
        0
    }

    fun canOpen(door: Int, today: LocalDate): Boolean = door in 1..unlockedDoors(today)

    fun isAdventSeason(today: LocalDate): Boolean = unlockedDoors(today) > 0

    /**
     * Shuffled door layout for [year]: identical on every device and app version during that year,
     * different the next one. Uses its own fixed generator (not kotlin.random) so the order can
     * never change under users' feet when the standard library is updated.
     */
    fun doorOrder(year: Int): List<Int> {
        val doors = (1..DOORS).toMutableList()
        var state = year.toLong() * 0x9E3779B97F4A7C15uL.toLong()
        fun next(): Long {
            // SplitMix64
            state += 0x9E3779B97F4A7C15uL.toLong()
            var z = state
            z = (z xor (z ushr 30)) * 0xBF58476D1CE4E5B9uL.toLong()
            z = (z xor (z ushr 27)) * 0x94D049BB133111EBuL.toLong()
            return z xor (z ushr 31)
        }
        // Fisher-Yates shuffle
        for (i in doors.lastIndex downTo 1) {
            val j = java.lang.Long.remainderUnsigned(next(), (i + 1).toLong()).toInt()
            doors[i] = doors[j].also { doors[j] = doors[i] }
        }
        return doors
    }
}
