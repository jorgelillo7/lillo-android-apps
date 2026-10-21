package com.jorgelillo.tournaments.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.domain.Library
import com.jorgelillo.tournaments.ui.home.AboutSheet
import com.jorgelillo.tournaments.ui.home.HomeScreen
import com.jorgelillo.tournaments.ui.home.ImportDialog
import com.jorgelillo.tournaments.ui.setup.NewTournamentScreen
import com.jorgelillo.tournaments.ui.stats.StatsScreen
import com.jorgelillo.tournaments.ui.tournament.TournamentScreen
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

private const val MISSING_GRACE_MILLIS = 600L

// Destinations, as type-safe routes (Navigation Compose + kotlinx.serialization). Internal, not
// private: the serializer of a private object can't be reached by reflection on the JVM (tests).
@Serializable
internal data object Home

@Serializable
internal data object NewTournament

@Serializable
internal data class TournamentRoute(val id: String)

@Serializable
internal data object Stats

@Composable
internal fun TournamentsApp(vm: TournamentsViewModel) {
    val library by vm.library.collectAsStateWithLifecycle()
    val current = library ?: return // first disk read in progress (a few ms)
    val nav = rememberNavController()
    val context = LocalContext.current
    var showImport by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.imports.collect { event ->
            val message = when (event.result) {
                null -> R.string.import_invalid
                Library.ImportResult.ADDED -> R.string.import_added
                Library.ImportResult.UPDATED -> R.string.import_updated
                Library.ImportResult.ALREADY_UP_TO_DATE -> R.string.import_same
                Library.ImportResult.NEWER_HERE -> R.string.import_newer_here
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            if (event.id != null) {
                showImport = false
                nav.navigate(TournamentRoute(event.id)) { popUpTo<Home>() }
            }
        }
    }

    NavHost(nav, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                library = current,
                onOpen = { nav.navigate(TournamentRoute(it.id)) },
                onNew = { nav.navigate(NewTournament) },
                onImport = { showImport = true },
                onStats = { nav.navigate(Stats) },
                onAbout = { showAbout = true }
            )
        }
        composable<NewTournament> {
            var saving by remember { mutableStateOf(false) }
            NewTournamentScreen(
                library = current,
                newId = vm::newId,
                onCreate = { t ->
                    if (saving) return@NewTournamentScreen // a second tap while the first save lands
                    saving = true
                    vm.save(t) {
                        nav.popBackStack()
                        nav.navigate(TournamentRoute(t.id))
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable<TournamentRoute> { entry ->
            val t = current.tournament(entry.toRoute<TournamentRoute>().id)
            if (t == null) {
                // Deleted (or a link to one that is gone): wait briefly, then leave.
                LaunchedEffect(Unit) {
                    delay(MISSING_GRACE_MILLIS)
                    nav.popBackStack()
                }
                return@composable
            }
            TournamentScreen(
                t = t,
                onChange = { change -> vm.change(t.id, change) },
                onDelete = {
                    nav.popBackStack()
                    vm.delete(t.id)
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable<Stats> {
            StatsScreen(library = current, onBack = { nav.popBackStack() })
        }
    }

    if (showImport) ImportDialog(onImport = vm::importText, onDismiss = { showImport = false })
    if (showAbout) AboutSheet(onDismiss = { showAbout = false })
}
