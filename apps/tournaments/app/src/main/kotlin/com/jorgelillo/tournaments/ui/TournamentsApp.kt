package com.jorgelillo.tournaments.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.domain.Library
import com.jorgelillo.tournaments.ui.home.AboutSheet
import com.jorgelillo.tournaments.ui.home.HomeScreen
import com.jorgelillo.tournaments.ui.home.ImportDialog
import com.jorgelillo.tournaments.ui.setup.NewTournamentScreen
import com.jorgelillo.tournaments.ui.stats.StatsScreen
import com.jorgelillo.tournaments.ui.tournament.TournamentScreen
import kotlinx.coroutines.delay

private const val MISSING_GRACE_MILLIS = 600L

@Composable
fun TournamentsApp(vm: TournamentsViewModel) {
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
                nav.navigate("t/${event.id}") { popUpTo("home") }
            }
        }
    }

    NavHost(nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                library = current,
                onOpen = { nav.navigate("t/${it.id}") },
                onNew = { nav.navigate("new") },
                onImport = { showImport = true },
                onStats = { nav.navigate("stats") },
                onAbout = { showAbout = true },
            )
        }
        composable("new") {
            NewTournamentScreen(
                library = current,
                newId = vm::newId,
                onCreate = { t ->
                    vm.save(t)
                    nav.popBackStack()
                    nav.navigate("t/${t.id}")
                },
                onBack = { nav.popBackStack() },
            )
        }
        composable("t/{id}") { entry ->
            val t = current.tournament(entry.arguments?.getString("id").orEmpty())
            if (t == null) {
                // Either just created (the save is still landing) or deleted: wait briefly, then leave.
                LaunchedEffect(Unit) { delay(MISSING_GRACE_MILLIS); nav.popBackStack() }
                return@composable
            }
            TournamentScreen(
                t = t,
                onChange = { change -> vm.change(t.id, change) },
                onDelete = {
                    nav.popBackStack()
                    vm.delete(t.id)
                },
                onBack = { nav.popBackStack() },
            )
        }
        composable("stats") {
            StatsScreen(library = current, onBack = { nav.popBackStack() })
        }
    }

    if (showImport) ImportDialog(onImport = vm::importText, onDismiss = { showImport = false })
    if (showAbout) AboutSheet(onDismiss = { showAbout = false })
}
