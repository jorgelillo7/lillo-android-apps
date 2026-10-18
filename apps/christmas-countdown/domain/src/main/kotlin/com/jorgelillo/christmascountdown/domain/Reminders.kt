package com.jorgelillo.christmascountdown.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month

/** The notifications of the season. */
enum class Reminder {
    ONE_MONTH_LEFT,
    ONE_WEEK_LEFT,
    ADVENT_STARTS,
    /** Today's door is still closed (only sent if it is). */
    DOOR_WAITING,
    CHRISTMAS_DAY,
}

/** The two moments of the day a reminder can go out. */
enum class ReminderSlot(val time: LocalTime) {
    MORNING(LocalTime.of(10, 0)),
    EVENING(LocalTime.of(18, 0)),
}

/**
 * When each reminder goes out: 1 month and 1 week left (25 Nov, 18 Dec), Advent starts (1 Dec)
 * and Christmas Day (25 Dec) in the morning; the door of the day, from the 2nd to the 24th, in the
 * evening and only if it hasn't been opened. At most one reminder per slot.
 */
object Reminders {

    fun due(date: LocalDate, slot: ReminderSlot, doorOpenedToday: Boolean): Reminder? {
        val day = date.dayOfMonth
        return when (slot) {
            ReminderSlot.MORNING -> when {
                date.month == Month.NOVEMBER && day == 25 -> Reminder.ONE_MONTH_LEFT
                date.month == Month.DECEMBER && day == 1 -> Reminder.ADVENT_STARTS
                date.month == Month.DECEMBER && day == 18 -> Reminder.ONE_WEEK_LEFT
                date.month == Month.DECEMBER && day == 25 -> Reminder.CHRISTMAS_DAY
                else -> null
            }
            ReminderSlot.EVENING ->
                Reminder.DOOR_WAITING.takeIf { date.month == Month.DECEMBER && day in 2..AdventCalendar.DOORS && !doorOpenedToday }
        }
    }

    /** The next slot strictly after [now] at which a reminder may go out (assuming the door is closed). */
    fun nextSlot(now: LocalDateTime): Pair<LocalDate, ReminderSlot> {
        var date = now.toLocalDate()
        repeat(MAX_DAYS) {
            for (slot in ReminderSlot.entries) {
                if (date.atTime(slot.time).isAfter(now) && due(date, slot, doorOpenedToday = false) != null) return date to slot
            }
            date = date.plusDays(1)
        }
        error("no reminder within a year of $now")
    }

    private const val MAX_DAYS = 400
}
