package com.jorgelillo.leaguecards.domain

import kotlinx.serialization.Serializable
import kotlin.math.roundToInt
import kotlin.random.Random

/** The player's id in the table. */
const val YOU = "you"

/** A line under the card after a choice: who it concerns and what happened. */
@Serializable
data class Outcome(val character: String, val text: String)

/** What happened when a matchday was played: shown as the group chat before the next one. */
@Serializable
data class Report(
    val matchday: Int,
    val yourPoints: Int,
    /** Every team's points this matchday, by character id ([YOU] for the player). */
    val scores: Map<String, Int>,
    val events: List<Outcome>,
    val chat: List<Outcome>,
)

@Serializable
data class Ending(val text: String, val meter: Meter? = null, val high: Boolean = false, val position: Int? = null) {
    val champion: Boolean get() = position == 1

    /** Which of the league's endings this is, for the collection. */
    val key: String get() = meter?.let { endingKey(it, high) } ?: if (champion) "champion" else "finished"
}

/**
 * A season in progress. Every random draw comes from [seed] plus where it happens (matchday,
 * card), so a saved season resumes exactly and tests are reproducible.
 */
@Serializable
data class Season(
    val seed: Long,
    val matchday: Int = 1,
    val meters: Meters = Meters(),
    /** Card ids dealt this matchday and how many are already answered. */
    val hand: List<String> = emptyList(),
    val handIndex: Int = 0,
    /** Points scored by this matchday's choices (a captain's ‑16…). */
    val matchdayPoints: Int = 0,
    val followUps: List<String> = emptyList(),
    val seen: Map<String, Int> = emptyMap(),
    val table: Map<String, Int> = emptyMap(),
    /** Per-rival bonus decided at the start of the season (all-or-nothing rivals). */
    val form: Map<String, Int> = emptyMap(),
    val jokerUsed: Boolean = false,
    val lastOutcome: Outcome? = null,
    val report: Report? = null,
    val reportPending: Boolean = false,
    val ending: Ending? = null,
    /** How many times each achievement's choices were made this season. */
    val progress: Map<String, Int> = emptyMap(),
    /** Achievements unlocked this season, in order. */
    val unlocked: List<String> = emptyList(),
    /** Unlocked by the last move: shown once. */
    val justUnlocked: List<String> = emptyList(),
) {
    val currentCard: String? get() = if (ending == null && !reportPending) hand.getOrNull(handIndex) else null
    val over: Boolean get() = ending != null
}

object Game {

    fun newSeason(league: League, seed: Long): Season {
        val rng = rng(seed, 0, 0)
        val form = league.rivals.associate { r ->
            r.character to if (r.allOrNothing) (if (rng.nextBoolean()) 15 else -30) else 0
        }
        val table = (listOf(YOU) + league.rivals.map { it.character }).associateWith { 0 }
        val season = Season(seed = seed, table = table, form = form)
        return season.copy(hand = deal(league, season))
    }

    /** Answers the card on screen: [right] is a swipe right. */
    fun choose(league: League, s: Season, right: Boolean): Season {
        val card = s.currentCard?.let(league::card) ?: return s
        val choice = if (right) card.right else card.left
        val gamble = choice.risk?.takeIf { rng(s.seed, s.matchday, 100 + s.handIndex).nextDouble() < it.chance }
        val effects = gamble?.effects ?: choice.effects
        val outcome = (gamble?.outcome ?: choice.outcome).takeIf { it.isNotBlank() }
        val achievement = if (choice.risk != null && gamble == null) choice.achievement else gamble?.achievement ?: choice.achievement
        val next = s.copy(
            meters = s.meters + effects,
            matchdayPoints = s.matchdayPoints + effects.points,
            followUps = s.followUps + listOfNotNull(choice.followUp),
            seen = s.seen + (card.id to (s.seen[card.id] ?: 0) + 1),
            lastOutcome = outcome?.let { Outcome(card.character, it) },
            handIndex = s.handIndex + 1,
            justUnlocked = emptyList(),
        ).count(league, achievement)
        return afterCard(league, next)
    }

    /** One more step towards [id]; unlocks it when its count is reached. */
    private fun Season.count(league: League, id: String?): Season {
        val def = id?.let(league::achievement) ?: return this
        val done = (progress[id] ?: 0) + 1
        return copy(progress = progress + (id to done)).let { if (done >= def.count) it.unlock(id) else it }
    }

    private fun Season.unlock(id: String): Season =
        if (id in unlocked) this else copy(unlocked = unlocked + id, justUnlocked = justUnlocked + id)

    /** The built-in achievements earned by how the season ended. */
    private fun Season.finish(league: League): Season {
        val position = ending?.position ?: return this
        return listOfNotNull(
            "survivor",
            "podium".takeIf { position <= 3 },
            "champion".takeIf { position == 1 },
            "champion_no_joker".takeIf { position == 1 && !jokerUsed },
        ).filter { league.achievement(it) != null }.fold(this) { s, id -> s.unlock(id) }
    }

    fun canUseJoker(league: League, s: Season): Boolean = league.joker != null && !s.jokerUsed && s.currentCard != null

    /** Skips the card on screen, once a season. */
    fun useJoker(league: League, s: Season): Season {
        val joker = league.joker ?: return s
        if (!canUseJoker(league, s)) return s
        val next = s.copy(
            meters = s.meters + joker.effects,
            matchdayPoints = s.matchdayPoints + joker.effects.points,
            jokerUsed = true,
            lastOutcome = Outcome(joker.character, joker.text),
            handIndex = s.handIndex + 1,
            justUnlocked = emptyList(),
        )
        return afterCard(league, next)
    }

    /** The group chat has been read: on to the next matchday's cards. */
    fun dismissReport(s: Season): Season = s.copy(reportPending = false, lastOutcome = null, justUnlocked = emptyList())

    /** Table rows, best first: (character id or [YOU], points). */
    fun standings(s: Season): List<Pair<String, Int>> =
        s.table.entries.sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key != YOU })
            .map { it.key to it.value }

    fun position(s: Season): Int = standings(s).indexOfFirst { it.first == YOU } + 1

    private fun afterCard(league: League, s: Season): Season {
        s.meters.broken()?.let { (meter, high) ->
            return s.copy(ending = Ending(league.endings.of(meter, high), meter, high))
        }
        return if (s.handIndex >= s.hand.size) playMatchday(league, s) else s
    }

    private fun playMatchday(league: League, s: Season): Season {
        val rng = rng(s.seed, s.matchday, 1)
        var meters = s.meters
        var yours = 40 + ((meters.squad - 50) * 0.4).roundToInt() + rng.nextInt(-15, 16) + s.matchdayPoints

        val events = league.events.shuffled(rng).filter { rng.nextDouble() < it.chance }.take(2)
        events.forEach { meters += it.effects }
        if (events.any { it.noPoints }) yours = 0
        yours = yours.coerceAtLeast(-20)

        val scores = buildMap {
            put(YOU, yours)
            league.rivals.forEach { r ->
                val invalid = rng.nextDouble() < r.invalidLineupChance
                val points = r.skill + (s.form[r.character] ?: 0) + rng.nextInt(-r.swing, r.swing + 1)
                put(r.character, if (invalid) 0 else points.coerceAtLeast(-10))
            }
        }
        val good = scores.values.count { it > yours } < scores.size / 2
        // Points pay (like the real game), and results move the tears: a bad matchday makes you
        // moan, a good one calms you down.
        meters += Effects(money = yours.coerceAtLeast(0) / 10, tears = if (good) -6 else 4)
        // And the league forgets: every meter drifts back towards the middle a little.
        meters = meters.settle()

        val chat = league.chat
            .filter { it.mood == Mood.ANY || it.mood == (if (good) Mood.GOOD else Mood.BAD) }
            .shuffled(rng)
            .distinctBy { it.character }
            .take(3)
            .map { Outcome(it.character, it.text.replace("{points}", yours.toString())) }

        val report = Report(
            matchday = s.matchday,
            yourPoints = yours,
            scores = scores,
            events = events.map { Outcome(it.character ?: "", it.text) },
            chat = chat,
        )
        val table = s.table.mapValues { (id, total) -> total + (scores[id] ?: 0) }
        var next = s.copy(
            meters = meters,
            table = table,
            report = report,
            reportPending = true,
            matchday = s.matchday + 1,
            matchdayPoints = 0,
            handIndex = 0,
            hand = emptyList(),
        )
        meters.broken()?.let { (meter, high) ->
            return next.copy(ending = Ending(league.endings.of(meter, high), meter, high))
        }
        if (next.matchday > league.matchdays) {
            val position = position(next)
            val text = if (position == 1) league.endings.champion else league.endings.notChampion
            return next.copy(ending = Ending(text.replace("{position}", position.toString()), position = position)).finish(league)
        }
        next = next.copy(hand = deal(league, next), followUps = emptyList())
        return next
    }

    /**
     * This matchday's cards: pending follow-ups first, then a weighted draw that prefers cards seen
     * less often, never the same card twice in a hand, never a once-a-season card again.
     */
    internal fun deal(league: League, s: Season): List<String> {
        val rng = rng(s.seed, s.matchday, 2)
        val hand = s.followUps.filter { league.card(it) != null }.toMutableList()
        val pool = league.cards.filter { c ->
            !c.followUpOnly && s.matchday in c.fromMatchday..c.toMatchday && !(c.once && (s.seen[c.id] ?: 0) > 0)
        }.toMutableList()
        pool.removeAll { it.id in hand }
        while (hand.size < league.cardsPerMatchday && pool.isNotEmpty()) {
            val weights = pool.map { it.weight.toDouble() / (1 + 2 * (s.seen[it.id] ?: 0)) }
            var pick = rng.nextDouble() * weights.sum()
            val index = weights.indexOfFirst { w -> (pick - w).also { pick = it } < 0 }.takeIf { it >= 0 } ?: pool.lastIndex
            hand += pool.removeAt(index).id
        }
        return hand
    }

    private fun rng(seed: Long, matchday: Int, salt: Int) = Random(seed * 1_000_003 + matchday * 7_919L + salt)
}
