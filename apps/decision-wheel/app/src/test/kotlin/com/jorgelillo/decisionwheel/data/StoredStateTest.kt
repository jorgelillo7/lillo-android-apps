package com.jorgelillo.decisionwheel.data

import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.decisionwheel.domain.AppState
import com.jorgelillo.decisionwheel.domain.Decision
import kotlin.test.assertEquals
import org.junit.Test

/** What users already have on their phones must keep reading after any change to the model. */
class StoredStateTest {
    @Test
    fun theStateTheReleasedAppWritesStillReads() {
        val stored = """
            {
                "wheels": [
                    {
                        "id": "w1",
                        "name": "Lunch",
                        "options": [
                            "Pizza",
                            "Sushi",
                            "Tacos"
                        ],
                        "favorite": true
                    },
                    {
                        "id": "w2",
                        "name": "Friday",
                        "options": [
                            "Cinema",
                            "Bowling"
                        ]
                    }
                ],
                "history": [
                    {
                        "wheelId": "w1",
                        "option": "Pizza",
                        "atEpochMillis": 1760000000000
                    }
                ],
                "avoidRepeats": false,
                "soundOn": false
            }
        """.trimIndent()

        val state = JsonStore.decodeStored(AppState.serializer(), stored)

        assertEquals(listOf("Lunch", "Friday"), state.wheels.map { it.name })
        assertEquals(listOf(true, false), state.wheels.map { it.favorite })
        assertEquals(listOf("Pizza", "Sushi", "Tacos"), state.wheel("w1")!!.options)
        assertEquals(listOf(Decision("w1", "Pizza", 1_760_000_000_000)), state.history)
        assertEquals(false, state.avoidRepeats)
        assertEquals(false, state.soundOn)
    }
}
