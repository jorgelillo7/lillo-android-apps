package com.jorgelillo.grouppolls.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jorgelillo.grouppolls.domain.Limits
import com.jorgelillo.grouppolls.domain.Poll
import com.jorgelillo.grouppolls.domain.Side
import com.jorgelillo.grouppolls.domain.Visibility
import com.jorgelillo.grouppolls.domain.Vote
import java.util.Date
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

/**
 * Polls in Firestore. The schema and every invariant (one vote per user, counters +1, closing
 * time, who may read names) are enforced by the security rules in
 * `lillorepo/packages/group_polls/firestore.rules`; this class only follows them.
 *
 * ```
 * polls/{code}                 visibility, question, red, blue, creatorId, creatorName, language,
 *                              createdAt (server time), durationDays (1 | 7 | 0 = no limit),
 *                              closedAt?, redVotes, blueVotes, reports, hidden
 * polls/{code}/votes/{uid}     voterId, side, prediction, votedAt, name (private polls only)
 * polls/{code}/reports/{uid}   at
 * ```
 */
class FirestorePollRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val now: () -> Long = System::currentTimeMillis
) : PollRepository {

    private val signIn = Mutex()

    override suspend fun userId(): String = signIn.withLock {
        auth.currentUser?.uid ?: auth.signInAnonymously().await().user!!.uid
    }

    private val polls get() = db.collection("polls")

    override fun feed(): Flow<List<Poll>> = callbackFlow {
        userId()
        val since = Timestamp(Date(now() - Poll.FEED_WINDOW))
        val registration = polls
            .whereEqualTo("visibility", "public")
            .whereEqualTo("hidden", false)
            .whereGreaterThan("createdAt", since)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(FEED_LIMIT)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                trySend(snapshot?.documents.orEmpty().mapNotNull(::toPoll))
            }
        awaitClose { registration.remove() }
    }

    override fun poll(code: String): Flow<Poll?> = callbackFlow {
        userId()
        val registration = polls.document(code).addSnapshotListener { snapshot, error ->
            trySend(if (error != null) null else snapshot?.let(::toPoll))
        }
        awaitClose { registration.remove() }
    }

    override fun votes(code: String): Flow<List<Vote>> = callbackFlow {
        userId()
        // The rules only allow listing votes of private polls; a public poll's query fails quietly.
        val registration = polls.document(code).collection("votes").orderBy("votedAt").addSnapshotListener {
                snapshot,
                error
            ->
            trySend(if (error != null) emptyList() else snapshot?.documents.orEmpty().mapNotNull(::toVote))
        }
        awaitClose { registration.remove() }
    }

    override suspend fun create(poll: Poll) {
        val uid = userId()
        polls.document(poll.code).set(
            mapOf(
                "visibility" to poll.visibility.name.lowercase(),
                "question" to poll.question,
                "red" to poll.red,
                "blue" to poll.blue,
                "creatorId" to uid,
                "creatorName" to poll.creatorName,
                "language" to poll.language,
                "createdAt" to FieldValue.serverTimestamp(),
                "durationDays" to durationDays(poll),
                "closedAt" to null,
                "redVotes" to 0,
                "blueVotes" to 0,
                "reports" to 0,
                "hidden" to false
            )
        ).await()
    }

    override suspend fun vote(code: String, vote: Vote) {
        val uid = userId()
        val poll = polls.document(code)
        val private = poll.get().await().getString("visibility") == "private"
        db.batch()
            .set(
                poll.collection("votes").document(uid),
                buildMap {
                    put("voterId", uid)
                    put("side", vote.side.name.lowercase())
                    put("prediction", vote.prediction.name.lowercase())
                    put("votedAt", FieldValue.serverTimestamp())
                    if (private) put("name", vote.voterName.orEmpty())
                }
            )
            .update(poll, if (vote.side == Side.RED) "redVotes" else "blueVotes", FieldValue.increment(1))
            .commit()
            .await()
    }

    override suspend fun close(code: String) {
        userId()
        polls.document(code).update("closedAt", FieldValue.serverTimestamp()).await()
    }

    override suspend fun report(code: String) {
        val uid = userId()
        val poll = polls.document(code)
        db.runTransaction { tx ->
            val reports = (tx.get(poll).getLong("reports") ?: 0) + 1
            tx.set(poll.collection("reports").document(uid), mapOf("at" to FieldValue.serverTimestamp()))
            tx.update(poll, mapOf("reports" to reports, "hidden" to (reports >= Limits.REPORTS_TO_HIDE)))
        }.await()
    }

    override suspend fun anonymize() {
        val uid = userId()
        val mine = polls.whereEqualTo("creatorId", uid).get().await().documents
        val myVotes = db.collectionGroup("votes").whereEqualTo("voterId", uid).get().await().documents
        (
            mine.map { it.reference to "creatorName" } +
                myVotes.filter { it.contains("name") }.map { it.reference to "name" }
            )
            .chunked(BATCH_LIMIT)
            .forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { (ref, field) -> batch.update(ref, field, "") }
                batch.commit().await()
            }
    }

    private fun durationDays(poll: Poll): Long = poll.closesAt?.let { (it - poll.createdAt) / DAY } ?: 0

    private fun toPoll(doc: DocumentSnapshot): Poll? {
        if (!doc.exists()) return null
        // Until the server timestamp lands, a just-created poll reads its local estimate.
        val created =
            doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()?.time
                ?: return null
        val days = doc.getLong("durationDays") ?: 0
        val closedAt = doc.getTimestamp("closedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()?.time
        return Poll(
            code = doc.id,
            visibility = if (doc.getString("visibility") == "private") Visibility.PRIVATE else Visibility.PUBLIC,
            question = doc.getString("question").orEmpty(),
            red = doc.getString("red").orEmpty(),
            blue = doc.getString("blue").orEmpty(),
            creatorId = doc.getString("creatorId").orEmpty(),
            creatorName = doc.getString("creatorName").orEmpty(),
            language = doc.getString("language").orEmpty(),
            createdAt = created,
            closesAt = closedAt ?: if (days > 0) created + days * DAY else null,
            redVotes = (doc.getLong("redVotes") ?: 0).toInt(),
            blueVotes = (doc.getLong("blueVotes") ?: 0).toInt(),
            hidden = doc.getBoolean("hidden") == true
        )
    }

    private fun toVote(doc: DocumentSnapshot): Vote? {
        val side = doc.getString("side")?.let(::side) ?: return null
        return Vote(
            voterId = doc.id,
            voterName = doc.getString("name"),
            side = side,
            prediction = doc.getString("prediction")?.let(::side) ?: side,
            votedAt =
                doc.getTimestamp("votedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.toDate()?.time ?: 0
        )
    }

    private fun side(value: String) = if (value == "red") Side.RED else Side.BLUE

    companion object {
        private const val DAY = 24L * 60 * 60 * 1000
        private const val FEED_LIMIT = 200L
        private const val BATCH_LIMIT = 400
    }
}
