package com.jorgelillo.whoslying.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PackShareTest {

    private val pack = WordPack(
        id = "custom-1234",
        name = "Clase de 3ºB",
        emoji = "🏫",
        entries = listOf(
            Entry("Pizarra", listOf("Tiza")),
            Entry("Recreo"),
            Entry("Mochila", listOf("Estuche")),
            Entry("Profe")
        ),
        custom = true
    )

    @Test
    fun aPackTravelsInAShortLink() {
        val link = PackShare.link(pack)
        assertTrue(link.startsWith("https://jorgelillo7.github.io/p/#1"))
        assertTrue(link.length < 300, "${link.length}")
        val imported = PackShare.fromText("Mirad este paquete para el impostor: $link")!!
        assertEquals(pack.copy(id = imported.id), imported)
    }

    /** Every built-in pack, as large as packs get in practice, fits in the QR code. */
    @Test
    fun builtInPacksFitInAQrCode() {
        val packs = WordPacks.builtIn("es") + WordPacks.builtIn("en")
        packs.forEach { pack ->
            val link = PackShare.link(pack)
            assertTrue(link.length <= PackShare.QR_MAX_CHARS, "${pack.id}: ${link.length}")
        }
    }

    /** A pack with the most words a link may carry is too big for a QR code, but travels as a link. */
    @Test
    fun aFullPackTravelsByLinkOnly() {
        val entries = (1..PackShare.MAX_WORDS).map { i -> Entry("Palabra número $i", listOf("Pista $i")) }
        val full = pack.copy(entries = entries)
        val link = PackShare.link(full)
        assertTrue(link.length > PackShare.QR_MAX_CHARS, "${link.length}")
        val back = assertNotNull(PackShare.fromText(link))
        assertEquals(full.entries, back.entries)
    }

    /** Only a link this app reads back is offered: a pack past any limit gets none. */
    @Test
    fun onlyAPackTheOtherPhoneAcceptsCanBeSent() {
        (WordPacks.builtIn("es") + WordPacks.builtIn("en")).forEach { assertNotNull(PackShare.sendableLink(it), it.id) }
        assertNotNull(
            PackShare.sendableLink(
                pack.copy(
                    entries = (1..PackShare.MAX_WORDS).map {
                        Entry("Palabra $it")
                    }
                )
            )
        )
        assertNull(
            PackShare.sendableLink(
                pack.copy(
                    entries = (1..PackShare.MAX_WORDS + 1).map {
                        Entry("Palabra $it")
                    }
                )
            )
        )
        assertNull(PackShare.sendableLink(pack.copy(name = "N".repeat(PackShare.MAX_NAME + 1))))
        // Within every count limit, but the code is past what a phone accepts (ShareLink.MAX_CODE_CHARS).
        val dense = (1..PackShare.MAX_WORDS).map { i ->
            Entry(
                "w$i-" + "x".repeat(PackShare.MAX_WORD - 10 - i.toString().length) + i.toString(36).padStart(4, 'z'),
                (1..PackShare.MAX_DECOYS).map { d -> "d$i.$d-${(i * 31 + d * 17).toString(36)}" }
            )
        }
        assertNull(PackShare.sendableLink(pack.copy(entries = dense)))
    }

    @Test
    fun unplayableOrForeignLinksAreIgnored() {
        assertNull(PackShare.fromText(PackShare.link(pack.copy(entries = pack.entries.take(2)))))
        assertNull(PackShare.fromText(PackShare.link(pack.copy(name = " "))))
        assertNull(PackShare.fromText("https://jorgelillo7.github.io/t/#1abc"))
        assertNull(PackShare.fromText("hola"))
    }

    @Test
    fun anImportedPackGetsAnIdOfItsOwn() {
        val imported = PackShare.fromText(PackShare.link(pack), newId = { "custom-new" })
        assertEquals("custom-new", imported?.id)
        // Two imports of one link are two packs: a link can never replace a pack already saved.
        assertTrue(PackShare.fromText(PackShare.link(pack))!!.id != PackShare.fromText(PackShare.link(pack))!!.id)
    }

    @Test
    fun aPackLargerThanTheAppMakesIsRefused() {
        val tooManyWords = pack.copy(entries = List(PackShare.MAX_WORDS + 1) { Entry("p$it") })
        val longWord = pack.copy(entries = pack.entries + Entry("x".repeat(PackShare.MAX_WORD + 1)))
        val longName = pack.copy(name = "n".repeat(PackShare.MAX_NAME + 1))
        val manyDecoys = pack.copy(entries = pack.entries + Entry("Lápiz", List(PackShare.MAX_DECOYS + 1) { "d$it" }))
        listOf(tooManyWords, longWord, longName, manyDecoys).forEach {
            assertNull(PackShare.fromText(PackShare.link(it)))
        }
    }

    @Test
    fun everyBuiltInPackFitsTheLimits() {
        (WordPacks.builtIn("es") + WordPacks.builtIn("en")).forEach {
            assertNotNull(PackShare.fromText(PackShare.link(it)), it.id)
        }
    }
}
