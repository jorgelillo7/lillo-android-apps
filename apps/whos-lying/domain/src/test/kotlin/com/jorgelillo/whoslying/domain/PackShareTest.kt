package com.jorgelillo.whoslying.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PackShareTest {

    private val pack = WordPack(
        id = "custom-1234",
        name = "Clase de 3ºB",
        emoji = "🏫",
        entries = listOf(Entry("Pizarra", listOf("Tiza")), Entry("Recreo"), Entry("Mochila", listOf("Estuche")), Entry("Profe")),
        custom = true
    )

    @Test
    fun aPackTravelsInAShortLink() {
        val link = PackShare.link(pack)
        assertTrue(link.startsWith("https://jorgelillo7.github.io/p/#1"))
        assertTrue(link.length < 300, "${link.length}")
        assertEquals(pack, PackShare.fromText("Mirad este paquete para el impostor: $link"))
    }

    @Test
    fun unplayableOrForeignLinksAreIgnored() {
        assertNull(PackShare.fromText(PackShare.link(pack.copy(entries = pack.entries.take(2)))))
        assertNull(PackShare.fromText(PackShare.link(pack.copy(name = " "))))
        assertNull(PackShare.fromText("https://jorgelillo7.github.io/t/#1abc"))
        assertNull(PackShare.fromText("hola"))
    }
}
