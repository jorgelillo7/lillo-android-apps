package com.jorgelillo.whoslying.domain

import com.jorgelillo.core.sharelink.ShareLink

/**
 * A custom word pack inside a link (`https://jorgelillo7.github.io/p/#…`), to pass it to another
 * phone by chat or QR code with no account or server. Format: core/sharelink.
 */
object PackShare {
    const val PATH = "/p/"
    const val QR_MAX_CHARS = ShareLink.QR_MAX_CHARS

    private val links = ShareLink(WordPack.serializer(), PATH)

    fun link(pack: WordPack): String = links.link(pack.copy(custom = false))

    /**
     * The pack in a link, a chat message with one, or a bare code, ready to save as one of the
     * user's packs; null if there is none or it isn't playable (no name, fewer than 3 words).
     */
    fun fromText(text: String): WordPack? = links.fromText(text)?.takeIf {
        it.name.isNotBlank() &&
            it.entries.size >= MIN_WORDS
    }?.copy(custom = true)

    const val MIN_WORDS = 3
}
