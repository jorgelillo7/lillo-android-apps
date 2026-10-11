package com.jorgelillo.tournaments.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorgelillo.tournaments.data.LibraryRepository
import com.jorgelillo.tournaments.domain.Library
import com.jorgelillo.tournaments.domain.Share
import com.jorgelillo.tournaments.domain.Tournament
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Outcome of opening a shared link: [id] is the tournament to show, null if the link was broken. */
data class ImportEvent(val result: Library.ImportResult?, val id: String?)

/** Single ViewModel for the whole (small) app: exposes the library and every change. */
class TournamentsViewModel(private val repository: LibraryRepository) : ViewModel() {

    /** Null until the first read from disk, so screens don't flash empty. */
    val library: StateFlow<Library?> = repository.state.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val importEvents = Channel<ImportEvent>(Channel.BUFFERED)
    val imports: Flow<ImportEvent> = importEvents.receiveAsFlow()

    /** Short ids keep shared links small. */
    fun newId(): String = (1..10).map { ID_ALPHABET[Random.nextInt(ID_ALPHABET.length)] }.joinToString("")

    fun save(t: Tournament) = launchUpdate { it.save(t) }

    /** Applies [change] to the stored tournament (the latest copy, not a stale one from the screen). */
    fun change(id: String, change: (Tournament) -> Tournament) = launchUpdate { lib ->
        lib.tournament(id)?.let { lib.save(change(it)) } ?: lib
    }

    fun delete(id: String) = launchUpdate { it.delete(id) }

    /** A link, a bare code or a chat message containing a link. */
    fun importText(text: String) {
        val tournament = Share.codeFromLink(text)?.let(Share::decode)
        viewModelScope.launch {
            if (tournament == null) {
                importEvents.send(ImportEvent(null, null))
                return@launch
            }
            val result = repository.updateAndGet { it.import(tournament) }
            importEvents.send(ImportEvent(result, tournament.id))
        }
    }


    private fun launchUpdate(transform: (Library) -> Library) {
        viewModelScope.launch { repository.update(transform) }
    }

    companion object {
        private const val ID_ALPHABET = "abcdefghijkmnpqrstuvwxyz23456789"

        fun factory(repository: LibraryRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { TournamentsViewModel(repository) }
        }
    }
}
