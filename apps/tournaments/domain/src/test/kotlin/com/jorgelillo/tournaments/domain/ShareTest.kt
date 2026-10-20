package com.jorgelillo.tournaments.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShareTest {

    private val names = listOf(
        "Pepe", "Luis", "María José", "Ana", "Íñigo", "Zoë", "Carlos", "Lucía",
        "Javi", "Marta", "Sergio", "Paula", "Álvaro", "Nuria", "Diego", "Sara"
    )

    private fun played(): Tournament {
        var t = Tournaments.create("abc123", "Viernes de Magic", 20_371, names, Format.ELIMINATION, game = "Magic", random = Random(4))
        while (Tournaments.pending(t).isNotEmpty()) {
            t = Tournaments.record(t, Tournaments.pending(t).first(), 2, 1, "20 vidas a 0, mazo rojo")
        }
        return t
    }

    @Test
    fun roundTrip() {
        val t = played()
        assertEquals(t, Share.decode(Share.encode(t)))
    }

    @Test
    fun smallEnoughForAQrCode() {
        val link = Share.link(played())
        // A QR code (level M) holds ~2,300 characters; stay well below to keep it easy to scan.
        assertTrue(link.length < 1_000, "link is ${link.length} chars")
    }

    @Test
    fun sixtyFourPlayersStillFitAQrCode() {
        var t = Tournaments.create("abc123", "Liga de verano", 20_371, (1..64).map { "Jugador número $it" }, Format.ELIMINATION, random = Random(1))
        while (Tournaments.pending(t).isNotEmpty()) {
            t = Tournaments.record(t, Tournaments.pending(t).first(), 2, 1, "20 vidas a 0")
        }
        val link = Share.link(t)
        println("64 players: ${link.length} chars")
        assertTrue(link.length < Share.QR_MAX_CHARS, "link is ${link.length} chars")
    }

    @Test
    fun linkAndBareCode() {
        val t = played()
        val link = Share.link(t)
        assertTrue(link.startsWith("https://jorgelillo7.github.io/t/#1"))
        assertEquals(t, Share.decode(Share.codeFromLink(link)!!))
        assertEquals(t, Share.decode(Share.codeFromLink(Share.encode(t))!!))
        // The website's "open in the app" button passes it as ?d= (URL-encoded).
        val viaWebsite = "https://jorgelillo7.github.io/t/?d=" + java.net.URLEncoder.encode(Share.encode(t), "UTF-8")
        assertEquals(t, Share.decode(Share.codeFromLink(viaWebsite)!!))
    }

    @Test
    fun garbageIsNull() {
        assertNull(Share.decode(""))
        assertNull(Share.decode("1notbase64!!"))
        assertNull(Share.decode(Share.encode(played()).dropLast(20)))
        assertNull(Share.codeFromLink("https://example.com/"))
    }
}
