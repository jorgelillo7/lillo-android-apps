package com.jorgelillo.christmascountdown.domain

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RemindersTest {

    private fun dec(day: Int) = LocalDate.of(2026, 12, day)

    @Test
    fun milestonesAndBigDaysInTheMorning() {
        assertEquals(Reminder.ONE_MONTH_LEFT, Reminders.due(LocalDate.of(2026, 11, 25), ReminderSlot.MORNING, false))
        assertEquals(Reminder.ADVENT_STARTS, Reminders.due(dec(1), ReminderSlot.MORNING, false))
        assertEquals(Reminder.ONE_WEEK_LEFT, Reminders.due(dec(18), ReminderSlot.MORNING, false))
        assertEquals(Reminder.CHRISTMAS_DAY, Reminders.due(dec(25), ReminderSlot.MORNING, true))
        assertNull(Reminders.due(dec(10), ReminderSlot.MORNING, false))
    }

    @Test
    fun doorReminderOnlyWhenItIsStillClosed() {
        assertEquals(Reminder.DOOR_WAITING, Reminders.due(dec(2), ReminderSlot.EVENING, false))
        assertEquals(Reminder.DOOR_WAITING, Reminders.due(dec(24), ReminderSlot.EVENING, false))
        assertNull(Reminders.due(dec(12), ReminderSlot.EVENING, true))
        assertNull(Reminders.due(dec(1), ReminderSlot.EVENING, false)) // the morning one is enough
        assertNull(Reminders.due(dec(25), ReminderSlot.EVENING, false))
        assertNull(Reminders.due(LocalDate.of(2026, 11, 30), ReminderSlot.EVENING, false))
    }

    @Test
    fun nextSlotWalksTheSeasonAndWrapsToNextYear() {
        assertEquals(
            LocalDate.of(2026, 11, 25) to ReminderSlot.MORNING,
            Reminders.nextSlot(LocalDateTime.of(2026, 10, 18, 12, 0))
        )
        assertEquals(dec(1) to ReminderSlot.MORNING, Reminders.nextSlot(LocalDateTime.of(2026, 11, 25, 10, 0)))
        assertEquals(dec(1) to ReminderSlot.MORNING, Reminders.nextSlot(LocalDateTime.of(2026, 11, 30, 23, 0)))
        assertEquals(dec(2) to ReminderSlot.EVENING, Reminders.nextSlot(LocalDateTime.of(2026, 12, 1, 10, 0)))
        assertEquals(dec(18) to ReminderSlot.MORNING, Reminders.nextSlot(LocalDateTime.of(2026, 12, 17, 18, 0)))
        assertEquals(dec(18) to ReminderSlot.EVENING, Reminders.nextSlot(LocalDateTime.of(2026, 12, 18, 10, 0)))
        assertEquals(dec(25) to ReminderSlot.MORNING, Reminders.nextSlot(LocalDateTime.of(2026, 12, 24, 18, 0)))
        assertEquals(
            LocalDate.of(2027, 11, 25) to ReminderSlot.MORNING,
            Reminders.nextSlot(LocalDateTime.of(2026, 12, 25, 10, 0))
        )
    }
}
