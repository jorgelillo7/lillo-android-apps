package com.jorgelillo.grouppolls.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorgelillo.grouppolls.data.LocalState
import com.jorgelillo.grouppolls.data.LocalStore
import com.jorgelillo.grouppolls.data.MyVote
import com.jorgelillo.grouppolls.data.PollRepository
import com.jorgelillo.grouppolls.domain.Drafts
import com.jorgelillo.grouppolls.domain.Duration
import com.jorgelillo.grouppolls.domain.InviteCodes
import com.jorgelillo.grouppolls.domain.Poll
import com.jorgelillo.grouppolls.domain.PredictionRecord
import com.jorgelillo.grouppolls.domain.Side
import com.jorgelillo.grouppolls.domain.Visibility
import com.jorgelillo.grouppolls.domain.Vote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Feed, the polls you know, your votes and predictions, and every action on a poll. */
class PollsViewModel(
    private val repository: PollRepository,
    private val store: LocalStore,
    private val language: String,
    private val now: () -> Long = System::currentTimeMillis
) : ViewModel() {

    val local: StateFlow<LocalState?> = store.state.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Public feed split by language, yours first, skipping creators you hid. */
    val feed: StateFlow<List<Pair<String, List<Poll>>>> = combine(repository.feed(), store.state) { polls, state ->
        polls.filterNot { it.creatorId in state.hiddenCreators }
            .groupBy { it.language }
            .toList()
            .sortedWith(compareBy({ it.first != language }, { it.first }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** A code from an opened link, waiting for the UI to show it. */
    private val _openCode = MutableStateFlow<String?>(null)
    val openCode: StateFlow<String?> = _openCode.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun poll(code: String): Flow<Poll?> = repository.poll(code)

    fun votes(code: String) = repository.votes(code)

    suspend fun userId(): String = repository.userId()

    fun open(link: String) {
        val code = InviteCodes.fromLink(link) ?: return
        _openCode.value = code
        update { it.copy(myPolls = (listOf(code) + (it.myPolls - code)).take(MAX_MY_POLLS)) }
    }

    fun consumeOpenCode() {
        _openCode.value = null
    }

    fun clearError() {
        _error.value = null
    }

    fun setName(name: String) = update { it.copy(name = Drafts.cleanName(name)) }

    fun acceptTerms() = update { it.copy(acceptedTerms = true) }

    /** Creates a poll and returns its code (null on failure). */
    suspend fun create(
        question: String,
        red: String,
        blue: String,
        visibility: Visibility,
        duration: Duration
    ): String? {
        val state = store.state.first()
        val created = now()
        val poll = Poll(
            code = InviteCodes.generate(),
            visibility = visibility,
            question = question.trim(),
            red = red.trim(),
            blue = blue.trim(),
            creatorId = repository.userId(),
            creatorName = state.name,
            language = language,
            createdAt = created,
            closesAt = duration.closesAt(created)
        )
        return runCatching { repository.create(poll) }.fold(
            onSuccess = {
                store.update { it.copy(myPolls = (listOf(poll.code) + it.myPolls).take(MAX_MY_POLLS)) }
                poll.code
            },
            onFailure = {
                _error.value = it.message
                null
            }
        )
    }

    fun vote(poll: Poll, side: Side, prediction: Side) = viewModelScope.launch {
        val state = store.state.first()
        val vote =
            Vote(
                repository.userId(),
                state.name.takeIf {
                    poll.visibility == Visibility.PRIVATE
                },
                side,
                prediction,
                now()
            )
        runCatching { repository.vote(poll.code, vote) }
            .onSuccess {
                store.update { s ->
                    s.copy(
                        votes = s.votes + (poll.code to MyVote(side, prediction)),
                        myPolls = if (poll.visibility ==
                            Visibility.PRIVATE
                        ) {
                            (listOf(poll.code) + (s.myPolls - poll.code)).take(MAX_MY_POLLS)
                        } else {
                            s.myPolls
                        }
                    )
                }
            }
            .onFailure { _error.value = it.message }
    }

    fun close(code: String) = viewModelScope.launch {
        runCatching { repository.close(code) }.onFailure {
            _error.value =
                it.message
        }
    }

    fun report(poll: Poll, hideCreator: Boolean) = viewModelScope.launch {
        runCatching { repository.report(poll.code) }
        update {
            it.copy(
                reported = it.reported + poll.code,
                hiddenCreators = if (hideCreator) {
                    it.hiddenCreators +
                        poll.creatorId
                } else {
                    it.hiddenCreators
                }
            )
        }
    }

    /** Counts a prediction once its poll has closed with a winner. */
    fun resolvePrediction(poll: Poll) {
        val state = local.value ?: return
        val mine = state.votes[poll.code] ?: return
        if (poll.code in state.resolvedPredictions) return
        val before = PredictionRecord(state.predictionHits, state.predictionsResolved)
        val after = before.add(mine.prediction, poll, now())
        if (after == before) return
        update {
            it.copy(
                predictionHits = after.hits,
                predictionsResolved = after.resolved,
                resolvedPredictions =
                    it.resolvedPredictions + poll.code
            )
        }
    }

    /** "Delete my data": anonymise on the server, forget everything on this phone. */
    fun deleteMyData(onDone: () -> Unit) = viewModelScope.launch {
        runCatching { repository.anonymize() }
            .onSuccess {
                store.update { LocalState() }
                onDone()
            }
            .onFailure { _error.value = it.message }
    }

    private fun update(transform: (LocalState) -> LocalState) {
        viewModelScope.launch { store.update(transform) }
    }

    companion object {
        const val MAX_MY_POLLS = 200

        fun factory(repository: PollRepository, store: LocalStore, language: String): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { PollsViewModel(repository, store, language) }
            }
    }
}
