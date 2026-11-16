package com.jorgelillo.whoslying.domain

import java.text.Normalizer
import kotlin.random.Random
import kotlinx.serialization.Serializable

object Rules {
    const val MIN_PLAYERS = 3
    const val MAX_PLAYERS = 24

    /** Impostors and journalists together: at most half the group (3 → 1, 4 → 2, 24 → 12). */
    fun maxInfiltrators(players: Int): Int = players / 2

    /** About one impostor per five players: what Chaos deals when it doesn't strike (3–7 → 1, 8–12 → 2, 24 → 5). */
    fun recommendedImpostors(players: Int): Int =
        ((players + 2) / 5).coerceIn(1, maxInfiltrators(players).coerceAtLeast(1))

    /** Discussion timer choices in seconds: off, then 1 to 24 minutes. */
    val TIMER_OPTIONS = listOf(0) + (1..24).map { it * 60 }

    /** About a minute of discussion per player. */
    fun recommendedTimer(players: Int): Int = players.coerceIn(1, 24) * 60

    /** Fewest impostors the mode allows with these settings: none in Mysterious when a journalist plays. */
    fun minImpostors(settings: GameSettings): Int =
        if (settings.mode == GameMode.MYSTERIOUS && settings.journalists > 0) 0 else 1

    /** [settings] with its impostor and journalist counts brought within what [players] allow. */
    fun fit(settings: GameSettings, players: Int): GameSettings {
        val cap = maxInfiltrators(players).coerceAtLeast(1)
        return when (settings.mode) {
            GameMode.MYSTERIOUS -> {
                val journalists = settings.journalists.coerceIn(0, cap)
                val impostors = settings.impostors.coerceIn(0, cap - journalists)
                settings.copy(
                    journalists = journalists,
                    impostors = if (impostors + journalists == 0) 1 else impostors
                )
            }

            else -> settings.copy(impostors = settings.impostors.coerceIn(1, cap), journalists = 0)
        }
    }

    fun canStart(players: Int, settings: GameSettings): Boolean {
        if (players !in MIN_PLAYERS..MAX_PLAYERS) return false
        if (settings.mode == GameMode.CHAOS) return true
        val journalists = if (settings.mode == GameMode.MYSTERIOUS) settings.journalists else 0
        return settings.impostors >= minImpostors(settings) && journalists >= 0 &&
            settings.impostors + journalists in 1..maxInfiltrators(players)
    }

    /** Lower case, no accents, no surrounding spaces: "Café " matches "cafe". */
    fun normalize(text: String): String =
        Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
}

@Serializable
sealed interface Outcome {
    @Serializable
    data object CiviliansWin : Outcome

    /** Impostors and/or journalists survived until at most one civilian was left. */
    @Serializable
    data class InfiltratorsWin(val roles: Set<Role>) : Outcome

    @Serializable
    data object JournalistGuessed : Outcome

    /** Chaos game where everyone was an impostor: nobody wins, the fun is finding out. */
    @Serializable
    data object NoCivilians : Outcome

    /** The group ended the game before anyone won. */
    @Serializable
    data object EndedEarly : Outcome
}

/** Result of voting someone out. */
@Serializable
data class Elimination(val player: String, val role: Role, val journalistMustGuess: Boolean)

/**
 * A game in progress. Immutable: every move returns the next state, so the UI (and tests) always
 * hold a consistent snapshot and Compose sees each change. Serializable so the app can bring a game
 * back after Android stops it in the background; it holds every secret, so it is never shared.
 */
@Serializable
class Game private constructor(
    val cards: List<Card>,
    val civilianWord: String,
    val category: String,
    val starter: String,
    /** Chaos games are played for fun: they never score. */
    val scored: Boolean = true,
    private val eliminated: Set<String> = emptySet(),
    val outcome: Outcome? = null,
    /** The eliminated journalist who still gets a guess at the word. */
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
        val mustGuess = card.role == Role.JOURNALIST
        val next = copy(eliminated = eliminated + player, lastElimination = Elimination(player, card.role, mustGuess))
        return if (mustGuess) next.copy(pendingGuess = player) else next.copy(outcome = next.evaluate())
    }

    /** The eliminated journalist's last chance; [journalistGuessed] tells whether it was the word. */
    fun guess(text: String): Game {
        checkNotNull(pendingGuess) { "No journalist is guessing" }
        val right = Rules.normalize(text) == Rules.normalize(civilianWord)
        return copy(pendingGuess = null, outcome = if (right) Outcome.JournalistGuessed else evaluate())
    }

    val journalistGuessed: Boolean get() = outcome == Outcome.JournalistGuessed

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
    ) = Game(cards, civilianWord, category, starter, scored, eliminated, outcome, pendingGuess, lastElimination)

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
        /** Chance that a chaos game strikes: more impostors than usual, some without a hint. */
        const val CHAOS_PROBABILITY = 1.0 / 3

        fun deal(players: List<String>, settings: GameSettings, pack: WordPack, entry: Entry, random: Random): Game {
            require(Rules.canStart(players.size, settings)) { "Invalid setup" }
            val chaosStrikes = settings.mode == GameMode.CHAOS && random.nextDouble() < CHAOS_PROBABILITY
            val usual = Rules.recommendedImpostors(players.size)
            val impostors = when {
                chaosStrikes -> random.nextInt(usual + 1, players.size + 1)
                settings.mode == GameMode.CHAOS -> usual
                else -> settings.impostors
            }
            val journalists = if (settings.mode == GameMode.MYSTERIOUS) settings.journalists else 0
            val shuffled = players.indices.shuffled(random)
            val impostorIdx = shuffled.take(impostors).toSet()
            val journalistIdx = shuffled.drop(impostors).take(journalists).toSet()
            val decoy =
                entry.decoys.randomOrNull(random)
                    ?: pack.entries.map { it.word }.filter { it != entry.word }.randomOrNull(random)

            val cards = players.mapIndexed { i, name ->
                val role = when (i) {
                    in impostorIdx -> Role.IMPOSTOR
                    in journalistIdx -> Role.JOURNALIST
                    else -> Role.CIVILIAN
                }
                when {
                    role == Role.CIVILIAN -> Card(
                        name,
                        role,
                        entry.word,
                        hint = null,
                        knowsRole =
                            settings.mode != GameMode.MYSTERIOUS
                    )

                    role == Role.JOURNALIST -> Card(name, role, word = null, hint = null, knowsRole = true)

                    // Drawing the similar word would give a Mysterious impostor away: they draw blind, and know it.
                    settings.mode == GameMode.MYSTERIOUS ->
                        if (settings.drawing) {
                            Card(name, role, word = null, hint = null, knowsRole = true)
                        } else {
                            Card(name, role, decoy, hint = null, knowsRole = false)
                        }

                    else -> {
                        val withHint = when {
                            chaosStrikes -> random.nextBoolean()
                            settings.mode == GameMode.CHAOS -> true
                            else -> settings.impostorHint
                        }
                        Card(name, role, word = null, hint = decoy.takeIf { withHint }, knowsRole = true)
                    }
                }
            }
            val starter = cards.filter { it.role != Role.JOURNALIST }.random(random).player
            return Game(cards, entry.word, pack.name, starter, scored = settings.mode != GameMode.CHAOS)
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
