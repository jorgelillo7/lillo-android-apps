package com.jorgelillo.leaguecards.data

import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.leaguecards.domain.Meters
import com.jorgelillo.leaguecards.domain.Saved
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

/** What users already have on their phones must keep reading after any change to the model. */
class StoredStateTest {
    @Test
    fun aSeasonInProgressTheReleasedAppWroteStillReads() {
        val stored = """
            {
                "season": {
                    "seed": 4162312786520847869,
                    "length": 19,
                    "matchday": 2,
                    "meters": {
                        "money": 44,
                        "patience": 53,
                        "tears": 45
                    },
                    "hand": [
                        "competidor-clausula",
                        "gafe-ficha",
                        "capitan"
                    ],
                    "seen": {
                        "excel-regla": 1,
                        "audios": 1,
                        "reportero-aviso": 1
                    },
                    "table": {
                        "you": 38,
                        "competidor": 60,
                        "gafe": 14,
                        "profeta": 0,
                        "excel": 36,
                        "reportero": 22,
                        "trapichero": 52,
                        "todoonada": 63,
                        "audios": 21
                    },
                    "form": {
                        "competidor": 0,
                        "gafe": 0,
                        "profeta": 0,
                        "excel": 0,
                        "reportero": 0,
                        "trapichero": 0,
                        "todoonada": 15,
                        "audios": 0
                    },
                    "report": {
                        "matchday": 1,
                        "yourPoints": 38,
                        "scores": {
                            "you": 38,
                            "competidor": 60,
                            "gafe": 14,
                            "profeta": 0,
                            "excel": 36,
                            "reportero": 22,
                            "trapichero": 52,
                            "todoonada": 63,
                            "audios": 21
                        },
                        "events": [],
                        "chat": [
                            {
                                "character": "gafe",
                                "text": "38 puntos… alguien ha hecho trampas."
                            },
                            {
                                "character": "reportero",
                                "text": "Os lo dije: hay movimientos raros en el mercado."
                            },
                            {
                                "character": "trapichero",
                                "text": "Esta liga está comprada."
                            }
                        ]
                    },
                    "reportPending": true,
                    "chatUsed": [
                        5,
                        6,
                        4
                    ]
                },
                "deck": "Liga de ejemplo",
                "seasonsPlayed": 3,
                "titles": 1,
                "bestPosition": 2,
                "endingsSeen": [
                    "money-low"
                ],
                "achievements": [
                    "first-season"
                ]
            }
        """.trimIndent()

        val saved = JsonStore.decodeStored(Saved.serializer(), stored)

        val season = saved.season!!
        assertEquals(19, season.length)
        assertEquals(2, season.matchday)
        assertEquals(Meters(money = 44, squad = 50, patience = 53, tears = 45), season.meters)
        assertEquals(listOf("competidor-clausula", "gafe-ficha", "capitan"), season.hand)
        assertTrue(season.reportPending)
        assertEquals("Liga de ejemplo", saved.deck)
        assertEquals(3, saved.seasonsPlayed)
        assertEquals(1, saved.titles)
        assertEquals(2, saved.bestPosition)
        assertEquals(setOf("money-low"), saved.endingsSeen)
        assertEquals(setOf("first-season"), saved.achievements)
    }
}
