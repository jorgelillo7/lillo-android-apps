package com.jorgelillo.leaguecards.domain

import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.serialization.Serializable

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
    val chat: List<Outcome>
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
    /** Matchdays this season; 0 (seasons saved before short seasons existed) means the league's. */
    val length: Int = 0,
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
    /** Group-chat lines already sent this season (indexes in [League.chat]): no repeats until they run out. */
    val chatUsed: Set<Int> = emptySet()
) {
    val currentCard: String? get() = if (ending == null && !reportPending) hand.getOrNull(handIndex) else null
    val over: Boolean get() = ending != null

    fun matchdays(league: League): Int = if (length > 0) length else league.matchdays

    /**
     * Where this matchday falls in the deck's own calendar: a short season runs through the whole
     * year (winter cards, the draft…) faster, so no part of the deck is left out.
     */
    fun calendarMatchday(league: League): Int {
        val total = matchdays(league)
        return if (total == league.matchdays) {
            matchday
        } else {
            ((matchday * league.matchdays + total - 1) / total).coerceAtMost(league.matchdays)
        }
    }
}

object Game {

    /** A short season: half the league's matchdays, rounded up. */
    fun shortLength(league: League): Int = (league.matchdays + 1) / 2

    fun newSeason(league: League, seed: Long, length: Int = league.matchdays): Season {
        val rng = rng(seed, 0, 0)
        val form = league.rivals.associate { r ->
            r.character to if (r.allOrNothing) (if (rng.nextBoolean()) 15 else -30) else 0
        }
        val table = (listOf(YOU) + league.rivals.map { it.character }).associateWith { 0 }
        val season = Season(
            seed = seed,
            length = length.coerceIn(1, league.matchdays),
            table = table,
            form = form
        )
        return season.copy(hand = deal(league, season))
    }

    /** Answers the card on screen: [right] is a swipe right. */
    fun choose(league: League, s: Season, right: Boolean): Season {
        val card = s.currentCard?.let(league::card) ?: return s
        val choice = if (right) card.right else card.left
        val gamble = choice.risk?.takeIf { rng(s.seed, s.matchday, 100 + s.handIndex).nextDouble() < it.chance }
        val effects = gamble?.effects ?: choice.effects
        val outcome = (gamble?.outcome ?: choice.outcome).takeIf { it.isNotBlank() }
        val achievement = if (choice.risk != null &&
            gamble == null
        ) {
            choice.achievement
        } else {
            gamble?.achievement ?: choice.achievement
        }
        // A story that goes on: its next card is dealt straight after this one.
        val hand = choice.next?.let { s.hand.take(s.handIndex + 1) + it + s.hand.drop(s.handIndex + 1) } ?: s.hand
        val next = s.copy(
            hand = hand,
            meters = s.meters + effects,
            matchdayPoints = s.matchdayPoints + effects.points,
            followUps = s.followUps + listOfNotNull(choice.followUp),
            seen = s.seen + (card.id to (s.seen[card.id] ?: 0) + 1),
            lastOutcome = outcome?.let { Outcome(card.character, it) },
            handIndex = s.handIndex + 1,
            justUnlocked = emptyList()
        ).count(league, achievement)
        return afterCard(league, next)
    }

    /** One more step towards [id]; unlocks it when its count is reached. */
    private fun Season.count(league: League, id: String?): Season {
        val def = id?.let(league::achievement) ?: return this
        val done = (progress[id] ?: 0) + 1
        return copy(progress = progress + (id to done)).let { if (done >= def.count) it.unlock(id) else it }
    }

    private fun Season.unlock(id: String): Season = if (id in
        unlocked
    ) {
        this
    } else {
        copy(unlocked = unlocked + id, justUnlocked = justUnlocked + id)
    }

    /** The built-in achievements earned by how the season ended. */
    private fun Season.finish(league: League): Season {
        val position = ending?.position ?: return this
        return listOfNotNull(
            "survivor",
            "podium".takeIf { position <= 3 },
            "champion".takeIf { position == 1 },
            "champion_no_joker".takeIf { position == 1 && !jokerUsed }
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
            justUnlocked = emptyList()
        )
        return afterCard(league, next)
    }

    /** The group chat has been read: on to the next matchday's cards. */
    fun dismissReport(s: Season): Season = s.copy(reportPending = false, lastOutcome = null, justUnlocked = emptyList())

    /** Table rows, best first: (character id or [YOU], points). */
    fun standings(s: Season): List<Pair<String, Int>> = s.table.entries.sortedWith(
        compareByDescending<Map.Entry<String, Int>> {
            it.value
        }.thenBy {
            it.key !=
                YOU
        }
    )
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

        val table = s.table.mapValues { (id, total) -> total + (scores[id] ?: 0) }
        val (chatPicked, chatUsed) = pickChat(league, s.chatUsed, good, rng)
        val placeholders = placeholders(league, s.copy(table = table), yours)
        val chat = chatPicked.map { i ->
            val line = league.chat[i]
            Outcome(line.character, placeholders.entries.fold(line.text) { t, (k, v) -> t.replace(k, v) })
        }

        val report = Report(
            matchday = s.matchday,
            yourPoints = yours,
            scores = scores,
            events = events.map { Outcome(it.character ?: "", it.text) },
            chat = chat
        )
        var next = s.copy(
            chatUsed = chatUsed,
            meters = meters,
            table = table,
            report = report,
            reportPending = true,
            matchday = s.matchday + 1,
            matchdayPoints = 0,
            handIndex = 0,
            hand = emptyList()
        )
        meters.broken()?.let { (meter, high) ->
            return next.copy(ending = Ending(league.endings.of(meter, high), meter, high))
        }
        if (next.matchday > s.matchdays(league)) {
            val position = position(next)
            val text = if (position == 1) league.endings.champion else league.endings.notChampion
            return next.copy(
                ending = Ending(text.replace("{position}", position.toString()), position = position)
            ).finish(league)
        }
        next = next.copy(hand = deal(league, next), followUps = emptyList())
        return next
    }

    /**
     * Up to three chat lines for the mood, one per character, preferring lines not sent yet this
     * season; once the mood's lines are all used they become available again. Returns the picked
     * indexes in [League.chat] and the updated used set.
     */
    private fun pickChat(league: League, used: Set<Int>, good: Boolean, rng: Random): Pair<List<Int>, Set<Int>> {
        val fits = league.chat.indices.filter {
            league.chat[it].mood == Mood.ANY || league.chat[it].mood == (if (good) Mood.GOOD else Mood.BAD)
        }
        val fresh = fits.filter { it !in used }.shuffled(rng)
        // Fresh lines first; old ones only fill the gaps, so a small deck still gets three messages.
        val picked = (fresh + (fits - fresh.toSet()).shuffled(rng))
            .distinctBy { league.chat[it].character }
            .take(CHAT_PER_MATCHDAY)
        val nowUsed = used + picked
        return picked to if (fits.all { it in nowUsed }) nowUsed - fits.toSet() + picked else nowUsed
    }

    /** What chat lines can mention: `{points}`, `{position}`, `{leader}` (the best rival) and `{last}`. */
    private fun placeholders(league: League, s: Season, points: Int): Map<String, String> {
        val rivals = standings(s).map { it.first }.filter { it != YOU }
        fun name(id: String?) = id?.let { league.character(it)?.name ?: it }.orEmpty()
        return mapOf(
            "{points}" to points.toString(),
            "{position}" to position(s).toString(),
            "{leader}" to name(rivals.firstOrNull()),
            "{last}" to name(rivals.lastOrNull())
        )
    }

    private const val CHAT_PER_MATCHDAY = 3

    /**
     * This matchday's cards: pending follow-ups first, then a weighted draw that prefers cards seen
     * less often, never the same card twice in a hand, never a once-a-season card again.
     */
    internal fun deal(league: League, s: Season): List<String> {
        val rng = rng(s.seed, s.matchday, 2)
        val hand = s.followUps.filter { league.card(it) != null }.toMutableList()
        val pool = league.cards.filter { c ->
            !c.followUpOnly && s.calendarMatchday(league) in c.fromMatchday..c.toMatchday &&
                !(c.once && (s.seen[c.id] ?: 0) > 0)
        }.toMutableList()
        pool.removeAll { it.id in hand }
        while (hand.size < league.cardsPerMatchday && pool.isNotEmpty()) {
            val weights = pool.map { it.weight.toDouble() / (1 + 2 * (s.seen[it.id] ?: 0)) }
            var pick = rng.nextDouble() * weights.sum()
            val index =
                weights.indexOfFirst { w -> (pick - w).also { pick = it } < 0 }.takeIf { it >= 0 } ?: pool.lastIndex
            hand += pool.removeAt(index).id
        }
        return hand
    }

    private fun rng(seed: Long, matchday: Int, salt: Int) = Random(seed * 1_000_003 + matchday * 7_919L + salt)
}
