package com.jorgelillo.grouppolls.data

import android.content.Context
import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.grouppolls.domain.Side
import kotlinx.serialization.Serializable

/** What this phone remembers: who you are, which polls you know and how you voted. */
@Serializable
data class LocalState(
    val name: String = "",
    val acceptedTerms: Boolean = false,
    /** Polls you created or opened from a link, newest first. */
    val myPolls: List<String> = emptyList(),
    /** Your vote and prediction per poll code. */
    val votes: Map<String, MyVote> = emptyMap(),
    /** Prediction results already counted (poll codes), so each counts once. */
    val resolvedPredictions: Set<String> = emptySet(),
    val predictionHits: Int = 0,
    val predictionsResolved: Int = 0,
    /** Creators whose polls you chose not to see. */
    val hiddenCreators: Set<String> = emptySet(),
    val reported: Set<String> = emptySet()
)

@Serializable
data class MyVote(val side: Side, val prediction: Side)

/** The whole local state as one JSON document. */
typealias LocalStore = JsonStore<LocalState>

/** The app's single store: DataStore file "group_polls", as it has always been (users keep their data). */
fun localStore(context: Context): LocalStore = JsonStore(context, "group_polls", LocalState.serializer()) { LocalState() }
