package com.jorgelillo.stadiumevents.data

import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.stadiumevents.domain.AppState
import com.jorgelillo.stadiumevents.domain.EventFeed
import com.jorgelillo.stadiumevents.domain.Kind
import com.jorgelillo.stadiumevents.domain.Level
import com.jorgelillo.stadiumevents.domain.StadiumEvent
import java.time.LocalDate
import kotlin.test.assertEquals
import org.junit.Test

/** What users already have on their phones must keep reading after any change to the model. */
class StoredStateTest {
    @Test
    fun theStateTheReleasedAppWritesStillReads() {
        val stored = """
            {
                "feed": {
                    "version": 1,
                    "events": [
                        {
                            "id": "espana-vs-inglaterra",
                            "title": "España vs Inglaterra",
                            "kind": "match",
                            "level": "max",
                            "days": ["2026-11-15"],
                            "time": "20:45",
                            "competition": "UEFA Nations League"
                        }
                    ]
                },
                "fetchedAt": 1795000000000,
                "notificationsAsked": true
            }
        """.trimIndent()

        val event = StadiumEvent(
            "espana-vs-inglaterra",
            "España vs Inglaterra",
            Kind.MATCH,
            Level.MAX,
            listOf("2026-11-15"),
            time = "20:45",
            competition = "UEFA Nations League"
        )
        assertEquals(
            AppState(EventFeed(events = listOf(event)), fetchedAt = 1_795_000_000_000L, notificationsAsked = true),
            JsonStore.decodeStored(AppState.serializer(), stored)
        )
    }

    @Test
    fun theDayLastRemindedIsStoredAsADate() {
        val stored = """{"notificationsAsked": true, "remindedFor": "2026-11-15"}"""
        assertEquals(LocalDate.of(2026, 11, 15), JsonStore.decodeStored(AppState.serializer(), stored).remindedFor)
    }
}
