package com.jorgelillo.christmascountdown.domain

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AdventCalendarTest {

    @Test
    fun lockedOutsideDecember() {
        assertEquals(0, AdventCalendar.unlockedDoors(LocalDate.of(2026, 11, 30)))
        assertEquals(0, AdventCalendar.unlockedDoors(LocalDate.of(2027, 1, 1)))
        assertFalse(AdventCalendar.isAdventSeason(LocalDate.of(2026, 10, 2)))
    }

    @Test
    fun oneDoorPerDayInDecember() {
        val today = LocalDate.of(2026, 12, 5)
        assertEquals(5, AdventCalendar.unlockedDoors(today))
        assertTrue(AdventCalendar.canOpen(5, today))
        assertFalse(AdventCalendar.canOpen(6, today))
        assertFalse(AdventCalendar.canOpen(0, today))
    }

    @Test
    fun allDoorsOpenFromChristmasEve() {
        assertEquals(24, AdventCalendar.unlockedDoors(LocalDate.of(2026, 12, 24)))
        assertEquals(24, AdventCalendar.unlockedDoors(LocalDate.of(2026, 12, 31)))
    }

    @Test
    fun doorOrderIsAShuffleOfAllDoors() {
        for (year in 2026..2040) {
            assertEquals((1..24).toList(), AdventCalendar.doorOrder(year).sorted())
        }
    }

    @Test
    fun doorOrderIsStableWithinAYearAndChangesBetweenYears() {
        assertEquals(AdventCalendar.doorOrder(2026), AdventCalendar.doorOrder(2026))
        assertNotEquals(AdventCalendar.doorOrder(2026), AdventCalendar.doorOrder(2027))
    }

    @Test
    fun doorOrderForAPublishedYearNeverChanges() {
        // Snapshot: users already see this layout, so the algorithm must not change.
        assertEquals(EXPECTED_2026, AdventCalendar.doorOrder(2026))
    }

    private companion object {
        val EXPECTED_2026 =
            listOf(22, 19, 9, 16, 17, 18, 10, 5, 24, 7, 14, 2, 8, 12, 20, 15, 21, 3, 4, 23, 13, 1, 6, 11)
    }
}
