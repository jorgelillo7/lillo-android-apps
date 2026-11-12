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

    /**
     * The tournament in [code], or null if it isn't one of ours (truncated, other version…) or isn't
     * one the app could have made ([isWellFormed]): a link is untrusted input, and what it holds is
     * stored and drawn as is.
     */
    fun decode(code: String): Tournament? = links.decode(code)?.takeIf(::isWellFormed)

    /** Longest texts the app lets anyone type: a link may carry nothing longer. */
    const val MAX_ID = 64
    const val MAX_NAME = 60
    const val MAX_GAME = 40
    const val MAX_PLAYER_NAME = 200
    const val MAX_COMMENT = 80

    /** Every match points at players of the tournament, sizes and texts within what the app creates. */
    fun isWellFormed(t: Tournament): Boolean {
        val ids = t.players.map { it.id }.toSet()
        val maxRounds = Tournaments.MAX_PLAYERS
        val maxMatches = Tournaments.MAX_PLAYERS * (maxRounds + 1)
        return t.id.length in 1..MAX_ID &&
            t.name.length <= MAX_NAME &&
            t.game.length <= MAX_GAME &&
            t.players.size in Tournaments.MIN_PLAYERS..Tournaments.MAX_PLAYERS &&
            ids.size == t.players.size &&
            t.players.all { it.name.isNotBlank() && it.name.length <= MAX_PLAYER_NAME } &&
            t.bestOf in Tournaments.BEST_OF &&
            t.topCut in Tournaments.TOP_CUTS &&
            t.swissRounds in 0..t.players.size &&
            t.revision >= 0 &&
            t.matches.size <= maxMatches &&
            t.matches.all { m ->
                (m.a == null || m.a in ids) &&
                    (m.b == null || m.b in ids) &&
                    (m.a == null || m.a != m.b) &&
                    m.round in 1..maxRounds &&
                    m.slot in 0 until Tournaments.MAX_PLAYERS &&
                    m.winsA in 0..Match.needed(t.bestOf) &&
                    m.winsB in 0..Match.needed(t.bestOf) &&
                    m.comment.length <= MAX_COMMENT
            }
    }

    fun link(t: Tournament): String = links.link(t)

    /** The code in a shared link, the website's `?d=` link, a chat message or a bare code. */
    fun codeFromLink(link: String): String? = links.codeFromLink(link)
}
