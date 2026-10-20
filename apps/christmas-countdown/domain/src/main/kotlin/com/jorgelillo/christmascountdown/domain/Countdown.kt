package com.jorgelillo.christmascountdown.domain

import java.time.Duration
import java.time.LocalDate
import java.time.Month
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/** Remaining time split for display. */
data class TimeLeft(val days: Long, val hours: Int, val minutes: Int, val seconds: Int) {
    companion object {
        fun of(duration: Duration): TimeLeft {
            val totalSeconds = duration.seconds.coerceAtLeast(0)
            return TimeLeft(
                days = totalSeconds / 86_400,
                hours = ((totalSeconds % 86_400) / 3_600).toInt(),
                minutes = ((totalSeconds % 3_600) / 60).toInt(),
                seconds = (totalSeconds % 60).toInt()
            )
        }
    }
}

sealed interface CountdownState {
    /** All of December 25th. */
    data object ChristmasDay : CountdownState

    /**
     * @param timeLeft real elapsed time until midnight of [target] (DST changes included).
     * @param sleeps nights left: calendar days between today and [target].
     */
    data class Counting(val timeLeft: TimeLeft, val sleeps: Long, val target: LocalDate) : CountdownState
}

object ChristmasCountdown {

    fun isChristmasDay(date: LocalDate): Boolean = date.month == Month.DECEMBER && date.dayOfMonth == 25

    /** The next December 25th on or after [today]. */
    fun nextChristmas(today: LocalDate): LocalDate {
        val thisYear = LocalDate.of(today.year, Month.DECEMBER, 25)
        return if (today.isAfter(thisYear)) thisYear.plusYears(1) else thisYear
    }

    fun stateAt(now: ZonedDateTime): CountdownState {
        val today = now.toLocalDate()
        if (isChristmasDay(today)) return CountdownState.ChristmasDay
        val target = nextChristmas(today)
        val start = target.atStartOfDay(now.zone)
        return CountdownState.Counting(
            timeLeft = TimeLeft.of(Duration.between(now, start)),
            sleeps = ChronoUnit.DAYS.between(today, target),
            target = target
        )
    }
}
