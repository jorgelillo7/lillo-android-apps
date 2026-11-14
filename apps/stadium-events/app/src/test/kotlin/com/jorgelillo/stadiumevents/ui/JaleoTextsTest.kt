package com.jorgelillo.stadiumevents.ui

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jorgelillo.stadiumevents.SampleFeed
import com.jorgelillo.stadiumevents.domain.Day
import com.jorgelillo.stadiumevents.domain.Jaleo
import com.jorgelillo.stadiumevents.domain.Kind
import com.jorgelillo.stadiumevents.domain.Level
import com.jorgelillo.stadiumevents.domain.Reminder
import com.jorgelillo.stadiumevents.domain.StadiumEvent
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** What the app says, in Spanish: the notification, the list rows and when the data was checked. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = "es")
class JaleoTextsTest {
    private val texts = JaleoTexts(ApplicationProvider.getApplicationContext<android.app.Application>().resources)
    private val today = LocalDate.of(2026, 11, 24)
    private val events = SampleFeed.events(today)

    @Test
    fun theReminderSaysWhatWhenAndFromWhen() {
        val busy = assertIs<Day.Busy>(Jaleo.tomorrow(events, today))
        assertEquals(
            "Partido · 21:00 · Atlético – Viking FK (Champions). Más gente y tráfico desde las 19:00 aprox. " +
                "por Canillejas, Las Rosas, Coslada y la M-40.",
            texts.notification(busy)
        )
        assertEquals("Mañana, miércoles 25", texts.tomorrowLabel(busy.date))
    }

    @Test
    fun anUnfixedMatchShowsBothDays() {
        val maybe = assertIs<Day.Busy>(Jaleo.tomorrow(events, today.plusDays(10)))
        assertEquals("El sábado 5 o domingo 6: el día aún no está confirmado", texts.hint(maybe))
        val row = Jaleo.upcoming(events, today).first()
        assertEquals("Sáb 5 o dom 6 dic", texts.upcomingWhen(row))
        assertEquals("Partido · Atlético – Betis (LaLiga) · día por confirmar", texts.upcomingWhat(row))
    }

    @Test
    fun aTourCountsItsNights() {
        val tour = Jaleo.upcoming(events, today, limit = 5).last()
        assertEquals("Concierto · Karol G · 4 días", texts.upcomingWhat(tour))
    }

    @Test
    fun checkedTodayOrOnADay() {
        val madrid = ZoneId.of("Europe/Madrid")
        val at = ZonedDateTime.of(2026, 11, 24, 8, 5, 0, 0, madrid).toInstant().toEpochMilli()
        assertEquals("Revisado hoy a las 8:05", texts.checkedAt(at, today, madrid))
        assertEquals("Revisado el 24 nov a las 8:05", texts.checkedAt(at, today.plusDays(1), madrid))
    }

    @Test
    fun aMatchWithNoTimeAndNoCompetitionReadsCleanly() {
        // A match Atlético does not play, sold out (level max), with no kick-off or competition in the feed.
        val national = StadiumEvent(
            "espana-inglaterra",
            "España vs Inglaterra",
            Kind.MATCH,
            Level.MAX,
            listOf(today.plusDays(1).toString())
        )
        val busy = assertIs<Day.Busy>(Jaleo.tomorrow(listOf(national), today))
        assertEquals("Partido", texts.what(national))
        assertEquals("España – Inglaterra", texts.title(national))
        assertEquals(
            "Partido · España – Inglaterra. Más gente y tráfico de lo normal por la zona del estadio.",
            texts.notification(busy)
        )
    }

    @Test
    fun theEveningTitleNamesTheDayAndTheMorningOneSaysToday() {
        val spain = StadiumEvent(
            "espana-vs-inglaterra",
            "España vs Inglaterra",
            Kind.MATCH,
            Level.MAX,
            listOf("2026-11-15"),
            time = "20:45",
            competition = "Nations League"
        )
        val sunday = assertIs<Day.Busy>(Jaleo.on(listOf(spain), LocalDate.of(2026, 11, 15)))
        assertEquals(
            "Mañana, domingo 15, hay jaleo en el Metropolitano",
            texts.notificationTitle(Reminder(sunday, today = false))
        )
        assertEquals("Hoy hay jaleo en el Metropolitano", texts.notificationTitle(Reminder(sunday, today = true)))
        assertEquals(
            "Partido · 20:45 · España – Inglaterra (Nations League). Más gente y tráfico desde las 18:45 aprox. " +
                "por Canillejas, Las Rosas, Coslada y la M-40.",
            texts.notification(sunday)
        )
        assertEquals("Hoy, domingo 15", texts.todayLabel(sunday.date))
    }

    @Test
    fun anUnfixedDayKeepsTheMaybeInBothTitles() {
        val maybe = assertIs<Day.Busy>(Jaleo.tomorrow(events, today.plusDays(10)))
        assertEquals(
            "Mañana, sábado 5, puede haber jaleo en el Metropolitano",
            texts.notificationTitle(Reminder(maybe, today = false))
        )
        assertEquals(
            "Hoy puede haber jaleo en el Metropolitano",
            texts.notificationTitle(Reminder(maybe, today = true))
        )
    }
}
