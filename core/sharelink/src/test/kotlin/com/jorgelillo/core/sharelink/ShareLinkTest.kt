package com.jorgelillo.core.sharelink

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ShareLinkTest {

    @Serializable
    data class Thing(val name: String, val items: List<String> = emptyList())

    private val links = ShareLink(Thing.serializer(), "/x/")
    private val thing = Thing("Árbol de Navidad 🎄", List(40) { "palabra $it" })

    @Test
    fun roundTripsThroughEveryForm() {
        val code = links.encode(thing)
        assertEquals(thing, links.decode(code))
        assertEquals(thing, links.fromText(links.link(thing)))
        assertEquals(thing, links.fromText("¡Mira esto! ${links.link(thing)} 😄"))
        assertEquals(thing, links.fromText(code))
        val website = "https://jorgelillo7.github.io/x/?d=" + java.net.URLEncoder.encode(code, "UTF-8")
        assertEquals(thing, links.fromText(website))
    }

    @Test
    fun compressesRepetitiveContent() {
        assertTrue(links.link(thing).length < 300, "${links.link(thing).length}")
    }

    @Test
    fun garbageAndOtherPagesAreNull() {
        assertNull(links.fromText(""))
        assertNull(links.fromText("1notbase64!!"))
        assertNull(links.decode(links.encode(thing).dropLast(10)))
        assertNull(links.fromText("https://example.com/"))
        assertNull(ShareLink(Thing.serializer(), "/y/").codeFromLink(links.link(thing)))
    }
}
