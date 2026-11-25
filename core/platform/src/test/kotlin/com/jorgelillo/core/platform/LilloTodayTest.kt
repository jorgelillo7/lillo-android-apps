package com.jorgelillo.core.platform

import java.time.LocalDate
import kotlin.test.assertEquals
import org.junit.Test

class LilloTodayTest {
    @Test
    fun theDateMovesOnWhenCheckedAfterMidnight() {
        var day = LocalDate.of(2026, 11, 30)
        val today = LilloToday { day }
        assertEquals(LocalDate.of(2026, 11, 30), today.date.value)
        day = day.plusDays(1)
        assertEquals(LocalDate.of(2026, 11, 30), today.date.value, "unchanged until checked")
        today.check()
        assertEquals(LocalDate.of(2026, 12, 1), today.date.value)
    }
}
