package com.jorgelillo.whoslying.data

import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.whoslying.domain.Entry
import com.jorgelillo.whoslying.domain.GameMode
import com.jorgelillo.whoslying.domain.GameSettings
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import org.junit.Test

/** What users already have on their phones must keep reading after any change to the model. */
class StoredStateTest {
    @Test
    fun theStateTheReleasedAppWritesStillReads() {
        val stored = """
            {
                "players": [
                    "Ana",
                    "Bea",
                    "Carlos"
                ],
                "settings": {
                    "mode": "BLIND",
                    "impostors": 2,
                    "chaos": true,
                    "packIds": [
                        "food"
                    ],
                    "discussionSeconds": 120
                },
                "customPacks": [
                    {
                        "id": "mine-1",
                        "name": "Office",
                        "emoji": "🏢",
                        "entries": [
                            {
                                "word": "Stapler",
                                "decoys": [
                                    "Clip"
                                ]
                            },
                            {
                                "word": "Printer"
                            }
                        ],
                        "custom": true
                    }
                ],
                "recentWords": [
                    "Pan"
                ],
                "scores": {
                    "Ana": 3
                },
                "roundsPlayed": 2,
                "welcomeSeen": true
            }
        """.trimIndent()

        val saved = JsonStore.decodeStored(SavedState.serializer(), stored)

        assertEquals(listOf("Ana", "Bea", "Carlos"), saved.players)
        assertEquals(GameMode.CHAOS, saved.settings.mode, "the old chaos switch is now the Chaos mode")
        assertEquals(2, saved.settings.impostors)
        assertEquals(setOf("food"), saved.settings.packIds)
        assertEquals(120, saved.settings.discussionSeconds)
        val pack = saved.customPacks.single()
        assertEquals("Office", pack.name)
        assertEquals(listOf(Entry("Stapler", listOf("Clip")), Entry("Printer")), pack.entries)
        assertEquals(mapOf("Ana" to 3), saved.scores)
        assertEquals(2, saved.roundsPlayed)
        assertTrue(saved.welcomeSeen)
    }

    @Test
    fun theModesOfEarlierVersionsMapOntoTheNewOnes() {
        fun read(settings: String) =
            JsonStore.decodeStored(SavedState.serializer(), """{"settings": $settings}""").settings

        assertEquals(GameSettings(GameMode.MYSTERIOUS), read("""{"mode": "CLASSIC"}"""))
        assertEquals(
            GameSettings(GameMode.ORIGINAL, impostorHint = false),
            read("""{"mode": "BLIND", "categoryHint": true}""")
        )
        assertEquals(GameSettings(GameMode.MYSTERIOUS, journalists = 1), read("""{"mode": "DRIFTER"}"""))
        assertEquals(GameSettings(GameMode.CHAOS), read("""{"mode": "CLASSIC", "chaos": true}"""))
        assertEquals(GameSettings(), read("""{"mode": "SOMETHING_NEWER"}"""))
    }

    @Test
    fun theNewSettingsReadBackAsWritten() {
        val settings =
            GameSettings(GameMode.MYSTERIOUS, impostors = 0, impostorHint = false, journalists = 2, rounds = 5)
        val json = Json.encodeToString(SavedState.serializer(), SavedState(settings = settings))
        assertFalse("CLASSIC" in json || "\"chaos\": true" in json, json)
        assertEquals(settings, JsonStore.decodeStored(SavedState.serializer(), json).settings)
    }
}
