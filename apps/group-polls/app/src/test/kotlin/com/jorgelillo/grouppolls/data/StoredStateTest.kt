package com.jorgelillo.grouppolls.data

import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.grouppolls.domain.Side
import kotlin.test.assertEquals
import org.junit.Test

/** What users already have on their phones must keep reading after any change to the model. */
class StoredStateTest {
    @Test
    fun theLocalStateStillReads() {
        val stored = """
            {
                "name": "Lucía",
                "acceptedTerms": true,
                "myPolls": [
                    "ABC123"
                ],
                "votes": {
                    "ABC123": {
                        "side": "RED",
                        "prediction": "BLUE"
                    }
                },
                "resolvedPredictions": [
                    "OLD1"
                ],
                "predictionHits": 1,
                "predictionsResolved": 2,
                "hiddenCreators": [
                    "troll"
                ]
            }
        """.trimIndent()

        assertEquals(
            LocalState(
                name = "Lucía",
                acceptedTerms = true,
                myPolls = listOf("ABC123"),
                votes = mapOf("ABC123" to MyVote(Side.RED, Side.BLUE)),
                resolvedPredictions = setOf("OLD1"),
                predictionHits = 1,
                predictionsResolved = 2,
                hiddenCreators = setOf("troll")
            ),
            JsonStore.decodeStored(LocalState.serializer(), stored)
        )
    }
}
