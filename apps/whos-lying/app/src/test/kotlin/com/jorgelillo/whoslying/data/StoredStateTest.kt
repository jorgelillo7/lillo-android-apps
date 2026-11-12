package com.jorgelillo.whoslying.data

import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.whoslying.domain.Entry
import com.jorgelillo.whoslying.domain.GameMode
import kotlin.test.assertEquals
import kotlin.test.assertTrue
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
        assertEquals(GameMode.BLIND, saved.settings.mode)
        assertEquals(2, saved.settings.impostors)
        assertTrue(saved.settings.chaos)
        assertEquals(setOf("food"), saved.settings.packIds)
        assertEquals(120, saved.settings.discussionSeconds)
        val pack = saved.customPacks.single()
        assertEquals("Office", pack.name)
        assertEquals(listOf(Entry("Stapler", listOf("Clip")), Entry("Printer")), pack.entries)
        assertEquals(mapOf("Ana" to 3), saved.scores)
        assertEquals(2, saved.roundsPlayed)
        assertTrue(saved.welcomeSeen)
    }
}
