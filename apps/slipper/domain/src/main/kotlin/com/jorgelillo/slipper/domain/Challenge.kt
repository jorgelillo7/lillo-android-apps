package com.jorgelillo.slipper.domain

import com.jorgelillo.core.sharelink.ShareLink
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** "Can you beat this?": the run's [seed] (a friend gets the very same slippers) and its [score]. */
@Serializable
data class Challenge(@SerialName("s") val seed: Long, @SerialName("p") val score: Int)

object Challenges {
    const val PATH = "/c/"

    private val links = ShareLink(Challenge.serializer(), PATH)

    fun link(c: Challenge): String = links.link(c)

    /** The challenge in a link, a chat message containing one, or a bare code; null if none. */
    fun fromText(text: String): Challenge? = links.codeFromLink(text)?.let(links::decode)
}
