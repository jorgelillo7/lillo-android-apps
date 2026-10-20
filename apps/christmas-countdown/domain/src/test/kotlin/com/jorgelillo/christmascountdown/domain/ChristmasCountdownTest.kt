package com.jorgelillo.christmascountdown.domain

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ChristmasCountdownTest {

    private val utc = ZoneId.of("UTC")
    private val madrid = ZoneId.of("Europe/Madrid")

    private fun at(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0, second: Int = 0, zone: ZoneId = utc) = ZonedDateTime.of(year, month, day, hour, minute, second, 0, zone)

    private fun counting(now: ZonedDateTime) = assertIs<CountdownState.Counting>(ChristmasCountdown.stateAt(now))

    @Test
    fun countsDownWithinTheSameYear() {
        val state = counting(at(2026, 10, 2, 12))
        assertEquals(TimeLeft(83, 12, 0, 0), state.timeLeft)
        assertEquals(84, state.sleeps)
        assertEquals(LocalDate.of(2026, 12, 25), state.target)
    }

    @Test
    fun lastSecondBeforeChristmas() {
        val state = counting(at(2026, 12, 24, 23, 59, 59))
        assertEquals(TimeLeft(0, 0, 0, 1), state.timeLeft)
        assertEquals(1, state.sleeps)
    }

    @Test
    fun christmasDayLastsAllDay() {
        assertEquals(CountdownState.ChristmasDay, ChristmasCountdown.stateAt(at(2026, 12, 25)))
        assertEquals(CountdownState.ChristmasDay, ChristmasCountdown.stateAt(at(2026, 12, 25, 23, 59, 59)))
    }

    @Test
    fun rollsOverToNextYearAfterChristmas() {
        val state = counting(at(2026, 12, 26))
        assertEquals(364, state.timeLeft.days)
        assertEquals(364, state.sleeps)
        assertEquals(LocalDate.of(2027, 12, 25), state.target)
    }

    @Test
    fun includesTheDaylightSavingHour() {
        // Clocks go back on 25 Oct 2026 in Madrid, so the real time left is one hour longer.
        val state = counting(at(2026, 10, 2, 18, 0, 0, madrid))
        assertEquals(TimeLeft(83, 7, 0, 0), state.timeLeft)
        assertEquals(84, state.sleeps)
    }
}
