package com.jorgelillo.leaguecards.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

/**
 * Everything that makes a league funny: its people, their cards, the matchday events and the
 * group-chat lines. Pure data, read from a JSON file, so new cards need no code. The app ships a
 * generic sample; a league's own deck (real people) lives outside this repository.
 */
@Serializable
data class League(
    val name: String,
    val matchdays: Int = 38,
    val cardsPerMatchday: Int = 3,
    val characters: List<Character>,
    val rivals: List<Rival>,
    val cards: List<Card>,
    val events: List<MatchdayEvent> = emptyList(),
    val chat: List<ChatLine> = emptyList(),
    val joker: Joker? = null,
    val endings: Endings,
    val achievements: List<Achievement> = emptyList(),
) {
    fun character(id: String): Character? = characters.firstOrNull { it.id == id }
    fun card(id: String): Card? = cards.firstOrNull { it.id == id }
    fun achievement(id: String): Achievement? = achievements.firstOrNull { it.id == id }

    /** Everything wrong with the deck, so a broken card is caught by a test, not by a player. */
    fun problems(): List<String> = buildList {
        val ids = characters.map { it.id }.toSet()
        val cardIds = cards.map { it.id }
        fun checkCharacter(where: String, id: String) {
            if (id !in ids) add("$where: unknown character '$id'")
        }
        if (characters.map { it.id }.toSet().size != characters.size) add("duplicate character ids")
        if (cardIds.toSet().size != cardIds.size) add("duplicate card ids: ${cardIds.groupBy { it }.filter { it.value.size > 1 }.keys}")
        if (rivals.isEmpty()) add("no rivals")
        rivals.forEach { checkCharacter("rival", it.character) }
        cards.forEach { card ->
            checkCharacter("card ${card.id}", card.character)
            if (card.fromMatchday > card.toMatchday) add("card ${card.id}: matchday range is empty")
            listOf(card.left, card.right).forEach { choice ->
                choice.followUp?.let { if (it !in cardIds) add("card ${card.id}: unknown follow-up '$it'") }
                choice.risk?.let { if (it.chance !in 0.0..1.0) add("card ${card.id}: risk chance out of 0..1") }
                listOfNotNull(choice.achievement, choice.risk?.achievement).forEach {
                    if (achievement(it) == null) add("card ${card.id}: unknown achievement '$it'")
                }
            }
        }
        if (cards.none { !it.followUpOnly }) add("no cards to draw")
        events.forEach { e ->
            e.character?.let { checkCharacter("event ${e.id}", it) }
            if (e.chance !in 0.0..1.0) add("event ${e.id}: chance out of 0..1")
        }
        chat.forEach { checkCharacter("chat line '${it.text.take(20)}'", it.character) }
        joker?.let { checkCharacter("joker", it.character) }
        if (achievements.map { it.id }.toSet().size != achievements.size) add("duplicate achievement ids")
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        fun parse(text: String): League = json.decodeFromString(serializer(), text)
    }
}

/** A member of the league: name and the colour their name shows in the group chat. */
@Serializable
data class Character(
    val id: String,
    val name: String,
    /** ARGB, e.g. "#FFE86F9C". */
    val color: String,
    /** One line shown under the name on their cards ("The jinx", "Inventor of cups"). */
    val tagline: String = "",
    /** Shown in their avatar instead of the initial. */
    val emoji: String = "",
)

/**
 * A computer-run rival for the table. Average points per matchday [skill] ± [swing];
 * [invalidLineupChance] of scoring nothing; [allOrNothing] decides at the start of each season
 * whether they go all in or abandon it.
 */
@Serializable
data class Rival(
    val character: String,
    val skill: Int = 40,
    val swing: Int = 20,
    val invalidLineupChance: Double = 0.02,
    val allOrNothing: Boolean = false,
)

/** The four meters, 0..100. Any of them at 0 or 100 ends the season (like Reigns). */
@Serializable
data class Meters(
    val money: Int = 50,
    val squad: Int = 50,
    val patience: Int = 50,
    val tears: Int = 50,
) {
    operator fun plus(e: Effects) = Meters(
        (money + e.money).coerceIn(0, 100),
        (squad + e.squad).coerceIn(0, 100),
        (patience + e.patience).coerceIn(0, 100),
        (tears + e.tears).coerceIn(0, 100),
    )

    /** A [share] of the way back to 50, at least 1 point, never past it (meters at 0 or 100 stay). */
    fun settle(share: Double = 0.2): Meters {
        fun Int.back() = if (this == 0 || this == 100 || this == 50) this
        else this + ((50 - this) * share).roundToInt().let { if (it == 0) (if (this < 50) 1 else -1) else it }
        return Meters(money.back(), squad.back(), patience.back(), tears.back())
    }

    fun broken(): Pair<Meter, Boolean>? = Meter.entries.firstNotNullOfOrNull { m ->
        when (get(m)) {
            0 -> m to false
            100 -> m to true
            else -> null
        }
    }

    operator fun get(m: Meter): Int = when (m) {
        Meter.MONEY -> money
        Meter.SQUAD -> squad
        Meter.PATIENCE -> patience
        Meter.TEARS -> tears
    }
}

enum class Meter { MONEY, SQUAD, PATIENCE, TEARS }

/** What a choice changes: the meters, plus [points] scored straight away this matchday. */
@Serializable
data class Effects(
    val money: Int = 0,
    val squad: Int = 0,
    val patience: Int = 0,
    val tears: Int = 0,
    val points: Int = 0,
)

/** A gamble: with [chance], [effects] and [outcome] replace the choice's own. */
@Serializable
data class Risk(val chance: Double, val effects: Effects, val outcome: String, val achievement: String? = null)

@Serializable
data class Choice(
    val label: String,
    val effects: Effects = Effects(),
    /** Shown after choosing ("Isquito, injured for 5 months"). */
    val outcome: String = "",
    /** A card to deal at the start of the next matchday. */
    val followUp: String? = null,
    val risk: Risk? = null,
    /** Counts towards this achievement when chosen (and the gamble, if any, didn't go the other way). */
    val achievement: String? = null,
)

@Serializable
data class Card(
    val id: String,
    val character: String,
    val text: String,
    /** Swipe left. */
    val left: Choice,
    /** Swipe right. */
    val right: Choice,
    val weight: Int = 1,
    val fromMatchday: Int = 1,
    val toMatchday: Int = 99,
    /** At most once a season. */
    val once: Boolean = false,
    /** Only dealt as someone's follow-up. */
    val followUpOnly: Boolean = false,
)

/** Something that may happen when the matchday is played ("recálculo", "corralito"…). */
@Serializable
data class MatchdayEvent(
    val id: String,
    val text: String,
    val chance: Double,
    val character: String? = null,
    val effects: Effects = Effects(),
    /** An invalid line-up: you score nothing this matchday. */
    val noPoints: Boolean = false,
)

enum class Mood { GOOD, BAD, ANY }

/** A group-chat message after a matchday: [mood] GOOD after a good matchday of yours, BAD after a bad one. */
@Serializable
data class ChatLine(val character: String, val text: String, val mood: Mood = Mood.ANY)

/** Once a season: skip the card on screen, at a price. */
@Serializable
data class Joker(val character: String, val label: String, val text: String, val effects: Effects = Effects())

/**
 * Something to collect across seasons: unlocked after [count] choices that point to it, or, for
 * the built-in ids, by how the season ends: "survivor" (reach the last matchday), "podium",
 * "champion" and "champion_no_joker".
 */
@Serializable
data class Achievement(val id: String, val title: String, val description: String, val emoji: String = "🏅", val count: Int = 1)

/** How a season ends: by a meter at 0 or 100, or after the last matchday. */
@Serializable
data class Endings(
    val moneyLow: String,
    val moneyHigh: String,
    val squadLow: String,
    val squadHigh: String,
    val patienceLow: String,
    val patienceHigh: String,
    val tearsLow: String,
    val tearsHigh: String,
    val champion: String,
    val notChampion: String,
) {
    /** Every ending's key, in display order (see [Ending.key]). */
    val keys: List<String> get() = listOf("champion", "finished") + Meter.entries.flatMap { listOf(key(it, false), key(it, true)) }

    fun text(key: String): String = when (key) {
        "champion" -> champion
        "finished" -> notChampion
        else -> Meter.entries.firstNotNullOf { m -> listOf(false, true).firstOrNull { key(m, it) == key }?.let { of(m, it) } }
    }

    fun of(meter: Meter, high: Boolean): String = when (meter) {
        Meter.MONEY -> if (high) moneyHigh else moneyLow
        Meter.SQUAD -> if (high) squadHigh else squadLow
        Meter.PATIENCE -> if (high) patienceHigh else patienceLow
        Meter.TEARS -> if (high) tearsHigh else tearsLow
    }
}

fun endingKey(meter: Meter, high: Boolean): String = meter.name.lowercase() + if (high) "_high" else "_low"

private fun key(meter: Meter, high: Boolean) = endingKey(meter, high)
