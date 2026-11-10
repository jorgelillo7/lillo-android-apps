package com.jorgelillo.stadiumevents.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException

/** The answer the app gives: is tomorrow a busy day around the stadium? */
sealed interface Tomorrow {
    val date: LocalDate

    /** [maybe]: the event is on tomorrow or another day, not fixed yet ("sábado o domingo"). */
    data class Busy(override val date: LocalDate, val event: StadiumEvent, val maybe: Boolean) : Tomorrow

    data class Calm(override val date: LocalDate) : Tomorrow
}

/** A busy day after tomorrow, for the short list under the answer. */
data class UpcomingDay(val event: StadiumEvent, val date: LocalDate, val maybe: Boolean)

object Jaleo {
    /** How long before the start the area gets busy: a rule of thumb, not traffic data. */
    const val BUSY_HOURS_BEFORE = 2L

    /** "2027 UEFA Champions League Final": the year is noise next to a date. */
    private val LEADING_YEAR = Regex("^\\d{4}\\s+")

    fun tomorrow(events: List<StadiumEvent>, today: LocalDate): Tomorrow {
        val date = today.plusDays(1)
        val event = events
            .filter { date in it.dates }
            // A fixed date beats a maybe, and the biggest event wins (a concert over a match).
            .sortedWith(compareBy<StadiumEvent> { it.either }.thenBy { if (it.level == Level.MAX) 0 else 1 })
            .firstOrNull()
        return if (event == null) Tomorrow.Calm(date) else Tomorrow.Busy(date, event, maybe = event.either)
    }

    /**
     * The next busy days after tomorrow, one row per event (its first day after tomorrow), soonest
     * first. Tomorrow's own event is left out: the answer card already shows it.
     */
    fun upcoming(events: List<StadiumEvent>, today: LocalDate, limit: Int = 3): List<UpcomingDay> {
        val shown = (tomorrow(events, today) as? Tomorrow.Busy)?.event?.id
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
    fun busyFrom(time: String?): String? = time?.let {
        try {
            LocalTime.parse(it.padStart(5, '0')).minusHours(BUSY_HOURS_BEFORE).toString()
        } catch (_: DateTimeParseException) {
            null
        }
    }

    /** "Atlético de Madrid vs Viking FK" → "Atlético – Viking FK": shorter, for big text. */
    fun shortTitle(title: String): String = title
        .replace(LEADING_YEAR, "")
        .replace("Atlético de Madrid", "Atlético")
        .replace(" vs ", " – ")
        .trim()
}
