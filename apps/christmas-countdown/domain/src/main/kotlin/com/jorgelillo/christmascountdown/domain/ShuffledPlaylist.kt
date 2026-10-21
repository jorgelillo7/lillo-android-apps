package com.jorgelillo.christmascountdown.domain

import kotlin.random.Random

/**
 * Endless shuffled playlist: every item plays once per round in random order, and a new round
 * never starts with the item that ended the previous one.
 */
class ShuffledPlaylist<T>(private val items: List<T>, private val random: Random = Random.Default) {

    private var round: List<T> = emptyList()
    private var position = 0

    init {
        require(items.isNotEmpty()) { "Playlist needs at least one item" }
    }

    fun next(): T {
        if (position == round.size) {
            val previous = round.lastOrNull()
            round = items.shuffled(random).let { shuffled ->
                if (shuffled.size > 1 && shuffled.first() == previous) shuffled.drop(1) + shuffled.first() else shuffled
            }
            position = 0
        }
        return round[position++]
    }
}
