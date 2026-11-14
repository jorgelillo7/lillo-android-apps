package com.jorgelillo.stadiumevents.domain

import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JaleoTest {
    private val today = LocalDate.of(2026, 11, 24)

    private fun event(
        id: String,
        vararg days: String,
        either: Boolean = false,
        level: Level = Level.BUSY,
        time: String? = null
    ) = StadiumEvent(id, id, Kind.MATCH, level, days.toList(), either, time)

    private val viking = event("viking", "2026-11-25", time = "21:00")
    private val betis = event("betis", "2026-12-05", "2026-12-06", either = true)
    private val concert = event("karol-g", "2027-06-24", "2027-06-25", level = Level.MAX)

    @Test
    fun aMatchTomorrowIsABusyDay() {
        val answer = assertIs<Day.Busy>(Jaleo.tomorrow(listOf(viking, betis), today))
        assertEquals(viking, answer.event)
        assertEquals(LocalDate.of(2026, 11, 25), answer.date)
        assertEquals(false, answer.maybe)
    }

    @Test
    fun nothingTomorrowIsACalmDay() {
        assertEquals(Day.Calm(LocalDate.of(2026, 11, 26)), Jaleo.tomorrow(listOf(viking), today.plusDays(1)))
    }

    @Test
    fun anUnfixedDayIsAMaybe() {
        val answer = assertIs<Day.Busy>(Jaleo.tomorrow(listOf(betis), LocalDate.of(2026, 12, 4)))
        assertTrue(answer.maybe)
    }

    @Test
    fun aFixedEventBeatsAMaybeAndTheBiggestWins() {
        val maybe = event("maybe", "2027-06-24", "2027-06-25", either = true)
        val match = event("match", "2027-06-24")
        val answer = assertIs<Day.Busy>(Jaleo.tomorrow(listOf(maybe, match, concert), LocalDate.of(2027, 6, 23)))
        assertEquals(concert, answer.event)
    }

    /** With no concert that day, the fixed match still wins over the maybe, in either order. */
    @Test
    fun aFixedMatchBeatsAMaybeOnItsOwn() {
        val maybe = event("maybe", "2027-06-24", "2027-06-25", either = true)
        val match = event("match", "2027-06-24")
        val day = LocalDate.of(2027, 6, 23)
        listOf(listOf(maybe, match), listOf(match, maybe)).forEach { events ->
            val answer = assertIs<Day.Busy>(Jaleo.tomorrow(events, day))
            assertEquals(match, answer.event)
            assertFalse(answer.maybe)
        }
    }

    @Test
    fun everyNightOfATourIsBusy() {
        assertIs<Day.Busy>(Jaleo.tomorrow(listOf(concert), LocalDate.of(2027, 6, 24)))
    }

    @Test
    fun todayAndTomorrowAreAnsweredByTheSameRule() {
        val eventDay = LocalDate.of(2026, 11, 25)
        assertEquals(Jaleo.tomorrow(listOf(viking), today), Jaleo.on(listOf(viking), eventDay))
        assertEquals(Day.Calm(today), Jaleo.on(listOf(viking), today))
    }

    @Test
    fun theWidgetAnswersTodayWhileTodayIsBusyElseTomorrow() {
        val eventDay = LocalDate.of(2026, 11, 25)
        assertEquals(eventDay, Jaleo.headline(listOf(viking), eventDay).date)
        assertIs<Day.Busy>(Jaleo.headline(listOf(viking), eventDay))
        assertEquals(eventDay, Jaleo.headline(listOf(viking), today).date)
        assertEquals(Day.Calm(eventDay.plusDays(2)), Jaleo.headline(listOf(viking), eventDay.plusDays(1)))
    }

    @Test
    fun theJobRunsAtNineAndAtSeven() {
        val day = today.atStartOfDay()
        assertEquals(day.withHour(9), Jaleo.nextSlot(day))
        assertEquals(day.withHour(9), Jaleo.nextSlot(day.withHour(8).withMinute(59)))
        assertEquals(day.withHour(19), Jaleo.nextSlot(day.withHour(9)))
        assertEquals(day.withHour(19), Jaleo.nextSlot(day.withHour(18).withMinute(59)))
        assertEquals(day.plusDays(1).withHour(9), Jaleo.nextSlot(day.withHour(19)))
        assertEquals(day.plusDays(1).withHour(9), Jaleo.nextSlot(day.withHour(23).withMinute(30)))
    }

    private fun at(date: String, time: String) = LocalDateTime.parse("${date}T$time")

    @Test
    fun theEveningBeforeABusyDayRemindsTomorrowOnce() {
        val reminder = assertNotNull(Jaleo.reminder(listOf(viking), at("2026-11-24", "19:00"), remindedFor = null))
        assertFalse(reminder.today)
        assertEquals(LocalDate.of(2026, 11, 25), reminder.day.date)
        assertNotNull(
            Jaleo.reminder(listOf(viking), at("2026-11-24", "23:50"), remindedFor = null),
            "a late evening run"
        )
        assertNull(Jaleo.reminder(listOf(viking), at("2026-11-24", "19:05"), remindedFor = LocalDate.of(2026, 11, 25)))
        assertNull(
            Jaleo.reminder(listOf(viking), at("2026-11-23", "19:00"), remindedFor = null),
            "the day after is calm"
        )
    }

    @Test
    fun theMorningOfABusyDayCatchesUpOnlyWhenTheEveningSaidNothing() {
        val reminder = assertNotNull(Jaleo.reminder(listOf(viking), at("2026-11-25", "09:00"), remindedFor = null))
        assertTrue(reminder.today)
        assertEquals(viking, reminder.day.event)
        assertNotNull(Jaleo.reminder(listOf(viking), at("2026-11-25", "09:00"), remindedFor = today), "an older day")
        assertNull(Jaleo.reminder(listOf(viking), at("2026-11-25", "09:00"), remindedFor = LocalDate.of(2026, 11, 25)))
        assertNull(Jaleo.reminder(listOf(viking), at("2026-11-24", "09:00"), remindedFor = null), "a calm day")
    }

    @Test
    fun nothingSoundsAtNightOrAfterTheStart() {
        listOf("00:00", "03:00", "08:59").forEach {
            assertNull(Jaleo.reminder(listOf(viking), at("2026-11-25", it), remindedFor = null), "at $it")
        }
        val lunch = event("lunch", "2026-11-25", time = "13:00")
        assertNotNull(Jaleo.reminder(listOf(lunch), at("2026-11-25", "12:59"), remindedFor = null))
        assertNull(Jaleo.reminder(listOf(lunch), at("2026-11-25", "13:00"), remindedFor = null))
        val noTime = event("concert", "2026-11-25")
        assertNotNull(Jaleo.reminder(listOf(noTime), at("2026-11-25", "18:30"), remindedFor = null))
    }

    @Test
    fun anUnfixedDayRemindsAsAMaybeInTheMorningToo() {
        val reminder = assertNotNull(Jaleo.reminder(listOf(betis), at("2026-12-05", "09:00"), remindedFor = null))
        assertTrue(reminder.today)
        assertTrue(reminder.day.maybe)
        assertTrue(assertNotNull(Jaleo.reminder(listOf(betis), at("2026-12-04", "19:00"), null)).day.maybe)
    }

    @Test
    fun upcomingSkipsTomorrowsEventAndThePast() {
        val past = event("past", "2026-11-01")
        val upcoming = Jaleo.upcoming(listOf(concert, past, betis, viking), today)
        assertEquals(listOf("betis", "karol-g"), upcoming.map { it.event.id })
        assertEquals(LocalDate.of(2026, 12, 5), upcoming.first().date)
        assertTrue(upcoming.first().maybe)
    }

    @Test
    fun upcomingIsShort() {
        val many = (1..9).map { event("e$it", "2026-12-%02d".format(it)) }
        assertEquals(3, Jaleo.upcoming(many, today).size)
    }

    @Test
    fun busyFromIsTwoHoursBeforeTheStart() {
        assertEquals("19:00", Jaleo.busyFrom("21:00"))
        assertEquals("16:30", Jaleo.busyFrom("18:30"))
        assertNull(Jaleo.busyFrom(null))
        assertNull(Jaleo.busyFrom("por confirmar"))
    }

    @Test
    fun shortTitles() {
        assertEquals("Atlético – Viking FK", Jaleo.shortTitle("Atlético de Madrid vs Viking FK"))
        assertEquals("España – Inglaterra", Jaleo.shortTitle("España vs Inglaterra"))
        assertEquals("Harry Styles", Jaleo.shortTitle("Harry Styles"))
        assertEquals("UEFA Champions League Final", Jaleo.shortTitle("2027 UEFA Champions League Final"))
    }

    @Test
    fun theFeedIgnoresUnknownFieldsAndValues() {
        val feed = EventFeed.parse(
            """{"version": 1, "sources": [], "future": true, "events": [
              {"id": "a", "title": "A", "kind": "festival", "level": "huge", "days": ["2026-11-25"],
               "either": false, "time": null, "competition": null, "url": "u", "extra": 1}]}"""
        )
        val only = feed.events.single()
        assertEquals(Kind.OTHER, only.kind)
        assertEquals(Level.BUSY, only.level)
        assertEquals(listOf(LocalDate.of(2026, 11, 25)), only.dates)
    }

    @Test
    fun aFeedLargerThanAnyRealOneIsRefused() {
        val huge = """{"events": [], "padding": "${"x".repeat(EventFeed.MAX_CHARS)}"}"""
        assertFailsWith<IllegalArgumentException> { EventFeed.parse(huge) }
    }

    @Test
    fun everyListAndTextIsCutToWhatTheAppShows() {
        val long = "y".repeat(1_000)
        val one = """{"id": "$long", "title": "$long", "days": [${List(50) { "\"2026-11-25\"" }.joinToString()}],
            "time": "$long", "competition": "$long", "url": "$long", "extra": 1}"""
        val e = EventFeed.parse("""{"events": [$one]}""").events.single()
        assertEquals(EventFeed.MAX_TITLE, e.title.length)
        assertEquals(EventFeed.MAX_ID, e.id.length)
        assertEquals(EventFeed.MAX_DAYS, e.days.size)
        assertEquals(EventFeed.MAX_TIME, e.time!!.length)
        assertEquals(EventFeed.MAX_COMPETITION, e.competition!!.length)
        assertEquals(EventFeed.MAX_URL, e.url!!.length)
    }

    @Test
    fun aFeedWithMoreEventsThanTheAppShowsKeepsTheFirstOnes() {
        val short = """{"id": "e", "title": "t", "days": ["2026-11-25"]}"""
        val feed = EventFeed.parse("""{"events": [${List(EventFeed.MAX_EVENTS + 50) { short }.joinToString()}]}""")
        assertEquals(EventFeed.MAX_EVENTS, feed.events.size)
    }
}
