package com.jorgelillo.tournaments.domain

import com.jorgelillo.core.sharelink.ShareLink

/**
 * A whole tournament inside a link (`https://jorgelillo7.github.io/t/#…`), so it moves to another
 * phone with no account or server. A 16-player bracket with comments is well under 1 KB, small
 * enough for a QR code. Format: core/sharelink.
 */
object Share {
    const val HOST = ShareLink.HOST
    const val PATH = "/t/"
    const val QR_MAX_CHARS = ShareLink.QR_MAX_CHARS

    private val links = ShareLink(Tournament.serializer(), PATH)

    fun encode(t: Tournament): String = links.encode(t)

    /** The tournament in [code], or null if it isn't one of ours (truncated, other version…). */
    fun decode(code: String): Tournament? = links.decode(code)

    fun link(t: Tournament): String = links.link(t)

    /** The code in a shared link, the website's `?d=` link, a chat message or a bare code. */
    fun codeFromLink(link: String): String? = links.codeFromLink(link)
}
