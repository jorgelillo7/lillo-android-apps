package com.jorgelillo.tournaments.data

import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.tournaments.domain.Format
import com.jorgelillo.tournaments.domain.Library
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

/** What users already have on their phones must keep reading after any change to the model. */
class StoredStateTest {
    @Test
    fun theLibraryTheReleasedAppWritesStillReads() {
        val stored = """
            {
                "tournaments": [
                    {
                        "id": "ko",
                        "nm": "Cup",
                        "g": "Ping-pong",
                        "dt": 20000,
                        "p": [
                            {
                                "i": 1,
                                "n": "Ann"
                            },
                            {
                                "i": 2,
                                "n": "Bob"
                            },
                            {
                                "i": 3,
                                "n": "Cat"
                            },
                            {
                                "i": 4,
                                "n": "Dan"
                            }
                        ],
                        "m": [
                            {
                                "t": "e",
                                "r": 1,
                                "s": 0,
                                "a": 3,
                                "b": 1,
                                "x": 2,
                                "y": 1,
                                "c": "close"
                            },
                            {
                                "t": "e",
                                "r": 1,
                                "s": 1,
                                "a": 2,
                                "b": 4
                            },
                            {
                                "t": "e",
                                "r": 2,
                                "s": 0,
                                "a": 3
                            }
                        ],
                        "v": 2
                    },
                    {
                        "id": "sw",
                        "nm": "Swiss night",
                        "dt": 20001,
                        "bo": 1,
                        "f": "s",
                        "p": [
                            {
                                "i": 1,
                                "n": "Eva"
                            },
                            {
                                "i": 2,
                                "n": "Fer"
                            },
                            {
                                "i": 3,
                                "n": "Gil"
                            },
                            {
                                "i": 4,
                                "n": "Hal"
                            },
                            {
                                "i": 5,
                                "n": "Ivy"
                            }
                        ],
                        "sr": 3,
                        "tc": 2,
                        "m": [
                            {
                                "t": "s",
                                "r": 1,
                                "s": 0,
                                "a": 4,
                                "b": 1,
                                "d": true
                            },
                            {
                                "t": "s",
                                "r": 1,
                                "s": 1,
                                "a": 3,
                                "b": 2
                            },
                            {
                                "t": "s",
                                "r": 1,
                                "s": 2,
                                "a": 5
                            }
                        ],
                        "v": 2
                    }
                ]
            }
        """.trimIndent()

        val library = JsonStore.decodeStored(Library.serializer(), stored)

        val cup = library.tournament("ko")!!
        assertEquals("Cup", cup.name)
        assertEquals("Ping-pong", cup.game)
        assertEquals(Format.ELIMINATION, cup.format)
        assertEquals(listOf("Ann", "Bob", "Cat", "Dan"), cup.players.map { it.name })
        val played = cup.matches.first()
        assertEquals(2 to 1, played.winsA to played.winsB)
        assertEquals("close", played.comment)

        val swiss = library.tournament("sw")!!
        assertEquals(Format.SWISS, swiss.format)
        assertEquals(1, swiss.bestOf)
        assertEquals(2, swiss.topCut)
        assertTrue(swiss.matches.first().draw)
    }
}
