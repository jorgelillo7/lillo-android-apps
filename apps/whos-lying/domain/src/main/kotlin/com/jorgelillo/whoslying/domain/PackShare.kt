package com.jorgelillo.whoslying.domain

import com.jorgelillo.core.sharelink.ShareLink
import java.util.UUID

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
     * The link to send, or null when the receiving phone would refuse it (too many or too long
     * words, decoys or name): a link is offered only if this app reads it back.
     */
    fun sendableLink(pack: WordPack): String? = link(pack).takeIf { fromText(it) != null }

    /**
     * The pack in a link, a chat message with one, or a bare code, ready to save as one of the
     * user's packs under a new id of its own (a link can't overwrite a pack the user has); null if
     * there is none, it isn't playable (no name, fewer than 3 words) or it is larger than any pack
     * the app makes.
     */
    fun fromText(text: String, newId: () -> String = { "custom-" + UUID.randomUUID() }): WordPack? =
        links.fromText(text)?.takeIf(::isWellFormed)?.copy(id = newId(), custom = true)

    const val MIN_WORDS = 3
    const val MAX_WORDS = 500
    const val MAX_NAME = 40
    const val MAX_EMOJI = 16
    const val MAX_WORD = 60
    const val MAX_DECOYS = 10

    private fun isWellFormed(pack: WordPack): Boolean = pack.name.isNotBlank() &&
        pack.name.length <= MAX_NAME &&
        pack.emoji.length <= MAX_EMOJI &&
        pack.entries.size in MIN_WORDS..MAX_WORDS &&
        pack.entries.all { e ->
            e.word.isNotBlank() && e.word.length <= MAX_WORD &&
                e.decoys.size <= MAX_DECOYS && e.decoys.all { it.length <= MAX_WORD }
        }
}
