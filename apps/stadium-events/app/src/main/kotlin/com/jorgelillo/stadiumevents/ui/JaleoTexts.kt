package com.jorgelillo.stadiumevents.ui

import android.content.res.Resources
import androidx.core.os.ConfigurationCompat
import com.jorgelillo.stadiumevents.R
import com.jorgelillo.stadiumevents.domain.Jaleo
import com.jorgelillo.stadiumevents.domain.Kind
import com.jorgelillo.stadiumevents.domain.StadiumEvent
import com.jorgelillo.stadiumevents.domain.Tomorrow
import com.jorgelillo.stadiumevents.domain.UpcomingDay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Every sentence the app says about an event, in one place, so the screen, the widget and the
 * notification use the same words. Dates in the app's language.
 */
class JaleoTexts(private val resources: Resources) {
    private val locale: Locale = ConfigurationCompat.getLocales(resources.configuration)[0] ?: Locale.getDefault()

    private fun format(date: LocalDate, pattern: String) = DateTimeFormatter.ofPattern(pattern, locale).format(date)

    private fun capitalised(text: String) = text.replaceFirstChar { it.titlecase(locale) }

    /** "Mañana, miércoles 25". */
    fun tomorrowLabel(date: LocalDate) = resources.getString(R.string.tomorrow_label, format(date, "EEEE d"))

    fun kind(event: StadiumEvent) = resources.getString(
        when (event.kind) {
            Kind.MATCH -> R.string.kind_match
            Kind.FINAL -> R.string.kind_final
            Kind.CONCERT -> R.string.kind_concert
            Kind.OTHER -> R.string.kind_other
        }
    )

    /** "Partido · 21:00", or just "Concierto" when there is no time. */
    fun what(event: StadiumEvent) =
        event.time?.let { resources.getString(R.string.what_at, kind(event), it) } ?: kind(event)

    /** "Atlético – Viking (Champions)". */
    fun title(event: StadiumEvent): String {
        val title = Jaleo.shortTitle(event.title)
        val competition = event.competition?.let { if ("Champions" in it) "Champions" else it }
        return if (competition == null || competition in title) title else "$title ($competition)"
    }

    /** The day or days of an unfixed event: "sábado 5 o domingo 6". */
    fun eitherDays(event: StadiumEvent) = event.dates.joinToString(resources.getString(R.string.or_separator)) {
        format(it, "EEEE d")
    }

    /** What the neighbours should expect: from when it gets busy, or that the day isn't fixed. */
    fun hint(busy: Tomorrow.Busy): String = when {
        busy.maybe -> resources.getString(R.string.hint_maybe, eitherDays(busy.event))

        else -> Jaleo.busyFrom(busy.event.time)?.let { resources.getString(R.string.hint_busy_from, it) }
            ?: resources.getString(R.string.hint_busy_day)
    }

    fun notification(busy: Tomorrow.Busy) = resources.getString(
        R.string.notification_text,
        what(busy.event),
        title(busy.event),
        hint(busy)
    )

    /** "Sáb 5 o dom 6 dic" for an unfixed match, "Jueves 24 de junio" otherwise. */
    fun upcomingWhen(day: UpcomingDay): String = if (day.maybe) {
        capitalised(
            day.event.dates.joinToString(resources.getString(R.string.or_separator)) { format(it, "EEE d") } +
                " " + format(day.event.dates.last(), "MMM")
        )
    } else {
        capitalised(format(day.date, resources.getString(R.string.date_pattern_long)))
    }

    fun upcomingWhat(day: UpcomingDay): String {
        val title = title(day.event)
        val kind = kind(day.event)
        // "UEFA Champions League Final" already says what it is.
        val parts = if (title.contains(kind, ignoreCase = true)) mutableListOf(title) else mutableListOf(kind, title)
        if (day.maybe) parts += resources.getString(R.string.day_to_be_fixed)
        val nights = day.event.dates.count { it >= day.date }
        if (!day.event.either &&
            nights > 1
        ) {
            parts += resources.getQuantityString(R.plurals.days_in_a_row, nights, nights)
        }
        return parts.joinToString(" · ")
    }

    /** "Revisado hoy a las 8:05" / "Revisado el 10 nov a las 8:05". */
    fun checkedAt(millis: Long, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): String {
        val at = Instant.ofEpochMilli(millis).atZone(zone)
        val time = DateTimeFormatter.ofPattern("H:mm", locale).format(at)
        return if (at.toLocalDate() == today) {
            resources.getString(R.string.checked_today, time)
        } else {
            resources.getString(R.string.checked_on, DateTimeFormatter.ofPattern("d MMM", locale).format(at), time)
        }
    }
}
