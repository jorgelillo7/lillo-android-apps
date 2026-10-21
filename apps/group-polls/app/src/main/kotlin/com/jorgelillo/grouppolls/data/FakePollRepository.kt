package com.jorgelillo.grouppolls.data

import com.jorgelillo.grouppolls.domain.Duration
import com.jorgelillo.grouppolls.domain.InviteCodes
import com.jorgelillo.grouppolls.domain.Limits
import com.jorgelillo.grouppolls.domain.Poll
import com.jorgelillo.grouppolls.domain.Side
import com.jorgelillo.grouppolls.domain.Visibility
import com.jorgelillo.grouppolls.domain.Vote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory backend with a few sample polls, so the whole app runs on the emulator before a
 * Firebase project exists. It enforces the same rules the Firestore security rules will.
 */
class FakePollRepository(private val now: () -> Long = System::currentTimeMillis) : PollRepository {

    private val me = "local-user"
    private val polls = MutableStateFlow(samplePolls() + samplePrivate())
    private val votes = MutableStateFlow(sampleVotes())
    private val reports = mutableMapOf<String, MutableSet<String>>()

    override suspend fun userId(): String = me

    override fun feed(): Flow<List<Poll>> = polls.map { all ->
        all.values.filter { it.inFeed(now()) }.sortedByDescending { it.createdAt }
    }

    override fun poll(code: String): Flow<Poll?> = polls.map { it[code] }

    override fun votes(code: String): Flow<List<Vote>> = votes.map { all ->
        if (polls.value[code]?.visibility ==
            Visibility.PRIVATE
        ) {
            all[code].orEmpty().values.sortedBy { it.votedAt }
        } else {
            emptyList()
        }
    }

    override suspend fun create(poll: Poll) {
        if (polls.value.containsKey(poll.code)) throw PollException("code taken")
        polls.update { it + (poll.code to poll) }
    }

    override suspend fun vote(code: String, vote: Vote) {
        val poll = polls.value[code] ?: throw PollException("no poll")
        if (!poll.isOpen(now())) throw PollException("closed")
        if (votes.value[code]?.containsKey(vote.voterId) == true) throw PollException("already voted")
        // Public polls never store names.
        val stored = if (poll.visibility == Visibility.PUBLIC) vote.copy(voterName = null) else vote
        votes.update { it + (code to (it[code].orEmpty() + (vote.voterId to stored))) }
        polls.update {
            it +
                (
                    code to
                        if (vote.side ==
                            Side.RED
                        ) {
                            poll.copy(redVotes = poll.redVotes + 1)
                        } else {
                            poll.copy(blueVotes = poll.blueVotes + 1)
                        }
                    )
        }
    }

    override suspend fun close(code: String) {
        val poll = polls.value[code] ?: return
        if (poll.creatorId != me || !poll.isOpen(now())) return
        polls.update { it + (code to poll.copy(closesAt = now())) }
    }

    override suspend fun report(code: String) {
        val reporters = reports.getOrPut(code) { mutableSetOf() }.apply { add(me) }
        if (reporters.size >=
            Limits.REPORTS_TO_HIDE
        ) {
            polls.update { all ->
                all[code]?.let { all + (code to it.copy(hidden = true)) }
                    ?: all
            }
        }
    }

    override suspend fun anonymize() {
        polls.update { all -> all.mapValues { (_, p) -> if (p.creatorId == me) p.copy(creatorName = "") else p } }
        votes.update { all ->
            all.mapValues { (_, byVoter) ->
                byVoter.mapValues { (id, v) ->
                    if (id ==
                        me
                    ) {
                        v.copy(voterName = null)
                    } else {
                        v
                    }
                }
            }
        }
    }

    /** A private poll with friends' votes, opened by link (store screenshots, manual checks). */
    private fun samplePrivate(): Map<String, Poll> {
        val t = now()
        val spanish = java.util.Locale.getDefault().language == "es"
        return mapOf(
            SAMPLE_PRIVATE to Poll(
                SAMPLE_PRIVATE,
                Visibility.PRIVATE,
                if (spanish) "¿Cena del viernes?" else "Friday dinner?",
                "Pizza",
                "Sushi",
                "sample-Lucía", "Lucía", if (spanish) "es" else "en",
                t - 3 * 60 * 60 * 1000L, Duration.ONE_DAY.closesAt(t - 3 * 60 * 60 * 1000L), redVotes = 2, blueVotes = 1
            )
        )
    }

    private fun sampleVotes(): Map<String, Map<String, Vote>> {
        val t = now()
        val friends = listOf("Lucía" to Side.RED, "Dani" to Side.RED, "Marta" to Side.BLUE)
        return mapOf(
            SAMPLE_PRIVATE to friends.mapIndexed { i, (name, side) ->
                "sample-$name" to Vote(
                    "sample-$name",
                    name,
                    side,
                    Side.RED,
                    t - (3 - i) * 600_000L
                )
            }.toMap()
        )
    }

    private fun samplePolls(): Map<String, Poll> {
        val t = now()
        val hour = 60L * 60 * 1000
        fun sample(
            lang: String,
            q: String,
            red: String,
            blue: String,
            r: Int,
            b: Int,
            age: Long,
            by: String,
            d: Duration = Duration.SEVEN_DAYS
        ) = Poll(
            InviteCodes.generate(),
            Visibility.PUBLIC,
            q,
            red,
            blue,
            "sample-$by",
            by,
            lang,
            t - age,
            d.closesAt(t - age),
            r,
            b
        )
        return listOf(
            sample(
                "es",
                "¿Tortilla de patatas con o sin cebolla?",
                "Con cebolla",
                "Sin cebolla",
                812,
                534,
                2 * hour,
                "Lucía"
            ),
            sample("es", "¿Vacaciones en la playa o en la montaña?", "Playa", "Montaña", 403, 377, 5 * hour, "Dani"),
            sample("es", "¿Piña en la pizza?", "Sí, claro", "Jamás", 221, 689, 26 * hour, "Marta", Duration.ONE_DAY),
            sample("en", "Cats or dogs?", "Cats", "Dogs", 1044, 1201, 3 * hour, "Sam"),
            sample(
                "en", "Coffee or tea in the morning?", "Coffee", "Tea", 640, 288, 30 * hour, "Alex", Duration.NO_LIMIT
            )
        ).associateBy { it.code }
    }

    companion object {
        /** Fixed code so a link can open the sample private poll. */
        const val SAMPLE_PRIVATE = "DemPrivateX2"
    }
}
