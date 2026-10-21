package com.jorgelillo.whoslying.domain

import java.text.Normalizer
import kotlin.random.Random

object Rules {
    const val MIN_PLAYERS = 3
    const val MAX_PLAYERS = 24

    fun drifters(mode: GameMode): Int = if (mode == GameMode.DRIFTER) 1 else 0

    /** Civilians must outnumber impostors at the start: impostors < (players - drifters) / 2. */
    fun maxImpostors(players: Int, mode: GameMode): Int = ((players - drifters(mode) - 1) / 2).coerceAtLeast(0)

    /** Discussion timer choices in seconds: off, then 1 to 24 minutes. */
    val TIMER_OPTIONS = listOf(0) + (1..24).map { it * 60 }

    /** About a minute of discussion per player. */
    fun recommendedTimer(players: Int): Int = players.coerceIn(1, 24) * 60

    fun minPlayers(mode: GameMode): Int = if (mode == GameMode.DRIFTER) 4 else MIN_PLAYERS

    fun canStart(players: Int, settings: GameSettings): Boolean = players in minPlayers(settings.mode)..MAX_PLAYERS &&
        settings.impostors in 1..maxImpostors(players, settings.mode)

    /** Lower case, no accents, no surrounding spaces: "Café " matches "cafe". */
    fun normalize(text: String): String =
        Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
}

sealed interface Outcome {
    data object CiviliansWin : Outcome

    /** Impostors and/or the drifter survived until at most one civilian was left. */
    data class InfiltratorsWin(val roles: Set<Role>) : Outcome

    data object DrifterGuessed : Outcome

    /** Chaos game where everyone was an impostor: nobody wins, the fun is finding out. */
    data object NoCivilians : Outcome

    /** The group ended the game before anyone won. */
    data object EndedEarly : Outcome
}

/** Result of voting someone out. */
data class Elimination(val player: String, val role: Role, val drifterMustGuess: Boolean)

/**
 * A game in progress. Immutable: every move returns the next state, so the UI (and tests) always
 * hold a consistent snapshot and Compose sees each change.
 */
class Game private constructor(
    val cards: List<Card>,
    val civilianWord: String,
    val category: String,
    val starter: String,
    private val eliminated: Set<String> = emptySet(),
    val outcome: Outcome? = null,
    /** The eliminated drifter who still gets a guess at the word. */
    val pendingGuess: String? = null,
    /** Who the last vote sent out, and what they were. */
    val lastElimination: Elimination? = null
) {
    val alive: List<Card> get() = cards.filter { it.player !in eliminated }
    val isOver: Boolean get() = outcome != null

    fun cardOf(player: String): Card = cards.first { it.player == player }

    fun eliminate(player: String): Game {
        check(!isOver && pendingGuess == null) { "Game is not waiting for a vote" }
        val card = cardOf(player)
        val mustGuess = card.role == Role.DRIFTER
        val next = copy(eliminated = eliminated + player, lastElimination = Elimination(player, card.role, mustGuess))
        return if (mustGuess) next.copy(pendingGuess = player) else next.copy(outcome = next.evaluate())
    }

    /** The eliminated drifter's last chance; [drifterGuessed] tells whether it was the word. */
    fun guess(text: String): Game {
        checkNotNull(pendingGuess) { "No drifter is guessing" }
        val right = Rules.normalize(text) == Rules.normalize(civilianWord)
        return copy(pendingGuess = null, outcome = if (right) Outcome.DrifterGuessed else evaluate())
    }

    val drifterGuessed: Boolean get() = outcome == Outcome.DrifterGuessed

    /** Ends the game now and shows who was who. */
    fun reveal(): Game {
        if (outcome != null) return this
        val end = when {
            cards.none { it.role == Role.CIVILIAN } -> Outcome.NoCivilians
            else -> evaluate() ?: Outcome.EndedEarly
        }
        return copy(pendingGuess = null, outcome = end)
    }

    private fun copy(
        eliminated: Set<String> = this.eliminated,
        outcome: Outcome? = this.outcome,
        pendingGuess: String? = this.pendingGuess,
        lastElimination: Elimination? = this.lastElimination
    ) = Game(cards, civilianWord, category, starter, eliminated, outcome, pendingGuess, lastElimination)

    private fun evaluate(): Outcome? {
        val civilians = alive.count { it.role == Role.CIVILIAN }
        val infiltrators = alive.filter { it.role != Role.CIVILIAN }.map { it.role }.toSet()
        // Everyone an impostor (chaos): it ends when the votes leave one player, or on reveal.
        if (cards.none { it.role == Role.CIVILIAN }) return if (alive.size <= 1) Outcome.NoCivilians else null
        return when {
            infiltrators.isEmpty() -> Outcome.CiviliansWin
            civilians <= 1 -> Outcome.InfiltratorsWin(infiltrators)
            else -> null
        }
    }

    companion object {
        /** Chance that a chaos game ignores the configured impostor count. */
        const val CHAOS_PROBABILITY = 1.0 / 3

        fun deal(players: List<String>, settings: GameSettings, pack: WordPack, entry: Entry, random: Random): Game {
            require(Rules.canStart(players.size, settings)) { "Invalid setup" }
            val impostors = impostorCount(players.size, settings, random)
            val drifters = if (impostors == players.size) 0 else Rules.drifters(settings.mode)
            val shuffled = players.indices.shuffled(random)
            val impostorIdx = shuffled.take(impostors).toSet()
            val drifterIdx = shuffled.drop(impostors).take(drifters).toSet()
            val decoy =
                entry.decoys.randomOrNull(random)
                    ?: pack.entries.map { it.word }.filter { it != entry.word }.randomOrNull(random)

            val cards = players.mapIndexed { i, name ->
                val role = when (i) {
                    in impostorIdx -> Role.IMPOSTOR
                    in drifterIdx -> Role.DRIFTER
                    else -> Role.CIVILIAN
                }
                val word = when {
                    role == Role.CIVILIAN -> entry.word

                    // Drawing a similar word would give impostors away, so they draw blind.
                    role == Role.IMPOSTOR && settings.mode != GameMode.BLIND && !settings.drawing -> decoy

                    else -> null
                }
                val hint = if (word == null && settings.categoryHint) pack.name else null
                Card(name, role, word, hint)
            }
            val starter = cards.filter { it.role != Role.DRIFTER }.random(random).player
            return Game(cards, entry.word, pack.name, starter)
        }

        private fun impostorCount(players: Int, settings: GameSettings, random: Random): Int {
            if (!settings.chaos || random.nextDouble() >= CHAOS_PROBABILITY) return settings.impostors
            val options = (1..Rules.maxImpostors(players, settings.mode)) + players
            return options.random(random)
        }
    }
}

/**
 * Picks the next secret word. A word doesn't come back until about 90% of the chosen packs have
 * been played; after that, the ones played longest ago come first.
 */
object WordPicker {
    /** Played words remembered across games, newest first. */
    const val RECENT_MEMORY = 3000

    /** Share of the pool kept available once almost everything was played. */
    private const val FRESH_SHARE = 10

    fun pick(packs: List<WordPack>, recent: List<String>, random: Random): Pair<WordPack, Entry> {
        val all = packs.flatMap { pack -> pack.entries.map { pack to it } }
        require(all.isNotEmpty()) { "No words" }
        val avoid = avoided(all.map { it.second.word }, recent)
        return all.filterNot { (_, entry) -> entry.word in avoid }.ifEmpty { all }.random(random)
    }

    /** How many words of [packs] haven't been played yet, out of how many. */
    fun unplayed(packs: List<WordPack>, recent: List<String>): Pair<Int, Int> {
        val words = packs.flatMap { pack -> pack.entries.map { it.word } }.toSet()
        return (words - recent.toSet()).size to words.size
    }

    private fun avoided(pool: List<String>, recent: List<String>): Set<String> {
        val inPool = pool.toSet()
        val window = inPool.size - (inPool.size / FRESH_SHARE).coerceAtLeast(1)
        return recent.filter { it in inPool }.distinct().take(window).toSet()
    }
}
