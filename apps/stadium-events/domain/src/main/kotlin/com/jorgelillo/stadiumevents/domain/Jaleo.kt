package com.jorgelillo.stadiumevents.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

/** The answer the app gives for one day: is it busy around the stadium? */
sealed interface Day {
    val date: LocalDate

    /** [maybe]: the event is on this day or another, not fixed yet ("sábado o domingo"). */
    data class Busy(override val date: LocalDate, val event: StadiumEvent, val maybe: Boolean) : Day

    data class Calm(override val date: LocalDate) : Day
}

/** A busy day after tomorrow, for the short list under the answer. */
data class UpcomingDay(val event: StadiumEvent, val date: LocalDate, val maybe: Boolean)

/** The two times a day the reminder job runs: the evening before, and the morning as a catch-up. */
enum class Slot(val time: LocalTime) {
    MORNING(LocalTime.of(9, 0)),
    EVENING(LocalTime.of(19, 0))
}

/** A reminder to show: about today (the morning catch-up) or tomorrow (the evening before). */
data class Reminder(val day: Day.Busy, val today: Boolean)

object Jaleo {
    /** How long before the start the area gets busy: a rule of thumb, not traffic data. */
    const val BUSY_HOURS_BEFORE = 2L

    /** "2027 UEFA Champions League Final": the year is noise next to a date. */
    private val LEADING_YEAR = Regex("^\\d{4}\\s+")

    /** The answer for [date], the same rule for today and tomorrow. */
    fun on(events: List<StadiumEvent>, date: LocalDate): Day {
        val event = events
            .filter { date in it.dates }
            // A fixed date beats a maybe, and the biggest event wins (a concert over a match).
            .sortedWith(compareBy<StadiumEvent> { it.either }.thenBy { if (it.level == Level.MAX) 0 else 1 })
            .firstOrNull()
        return if (event == null) Day.Calm(date) else Day.Busy(date, event, maybe = event.either)
    }

    fun tomorrow(events: List<StadiumEvent>, today: LocalDate): Day = on(events, today.plusDays(1))

    /** The widget's one answer: today while today is busy, else tomorrow. */
    fun headline(events: List<StadiumEvent>, today: LocalDate): Day =
        on(events, today) as? Day.Busy ?: tomorrow(events, today)

    /** The next run of the reminder job: the first [Slot] strictly after [now]. */
    fun nextSlot(now: LocalDateTime): LocalDateTime = (0L..1L).asSequence()
        .flatMap { days -> Slot.entries.asSequence().map { now.toLocalDate().plusDays(days).atTime(it.time) } }
        .first { it > now }

    /**
     * What the reminder job shows when it runs at [now], decided from the clock and the day already
     * reminded ([remindedFor]), never from when the job was planned, so a late run still does the
     * right thing:
     * - from 19:00, tomorrow if it is busy (the evening before);
     * - from 09:00, today if it is busy and nothing was shown for it yet (the evening reminder did
     *   not arrive: phone off, app installed that morning), unless the event has started;
     * - nothing between 00:00 and 08:59.
     */
    fun reminder(events: List<StadiumEvent>, now: LocalDateTime, remindedFor: LocalDate?): Reminder? {
        val time = now.toLocalTime()
        val today = now.toLocalDate()
        return when {
            time < Slot.MORNING.time -> null

            time < Slot.EVENING.time -> (on(events, today) as? Day.Busy)
                ?.takeIf { remindedFor != today && !started(it.event, time) }
                ?.let { Reminder(it, today = true) }

            else -> (tomorrow(events, today) as? Day.Busy)
                ?.takeIf { remindedFor != it.date }
                ?.let { Reminder(it, today = false) }
        }
    }

    private fun started(event: StadiumEvent, time: LocalTime): Boolean =
        startTime(event.time)?.let { time >= it } ?: false

    private fun startTime(time: String?): LocalTime? = time?.let {
        try {
            LocalTime.parse(it.padStart(5, '0'))
        } catch (_: DateTimeParseException) {
            null
        }
    }

    /**
     * The next busy days after tomorrow, one row per event (its first day after tomorrow), soonest
     * first. Tomorrow's own event is left out: the answer card already shows it.
     */
    fun upcoming(events: List<StadiumEvent>, today: LocalDate, limit: Int = 3): List<UpcomingDay> {
        val shown = (tomorrow(events, today) as? Day.Busy)?.event?.id
        val after = today.plusDays(1)
        return events
            .filter { it.id != shown }
            .mapNotNull { event ->
                event.dates.firstOrNull { it > after }?.let { UpcomingDay(event, it, event.either) }
            }
            .sortedBy { it.date }
            .take(limit)
    }

    /** "19:00" for a 21:00 start: from when the area gets busy. Null without a start time. */
    fun busyFrom(time: String?): String? = startTime(time)?.minusHours(BUSY_HOURS_BEFORE)?.toString()

    /** "Atlético de Madrid vs Viking FK" → "Atlético – Viking FK": shorter, for big text. */
    fun shortTitle(title: String): String = title
        .replace(LEADING_YEAR, "")
        .replace("Atlético de Madrid", "Atlético")
        .replace(" vs ", " – ")
        .trim()
}
