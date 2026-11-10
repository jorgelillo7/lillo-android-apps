package com.jorgelillo.stadiumevents.ui

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jorgelillo.stadiumevents.SampleFeed
import com.jorgelillo.stadiumevents.domain.Jaleo
import com.jorgelillo.stadiumevents.domain.Tomorrow
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
        val busy = assertIs<Tomorrow.Busy>(Jaleo.tomorrow(events, today))
        assertEquals(
            "Partido · 21:00 · Atlético – Viking FK (Champions). Más gente y tráfico desde las 19:00 aprox. " +
                "por Canillejas, Las Rosas, Coslada y la M-40.",
            texts.notification(busy)
        )
        assertEquals("Mañana, miércoles 25", texts.tomorrowLabel(busy.date))
    }

    @Test
    fun anUnfixedMatchShowsBothDays() {
        val maybe = assertIs<Tomorrow.Busy>(Jaleo.tomorrow(events, today.plusDays(10)))
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
}
