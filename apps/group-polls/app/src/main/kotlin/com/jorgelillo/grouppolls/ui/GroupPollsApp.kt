package com.jorgelillo.grouppolls.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jorgelillo.grouppolls.R
import com.jorgelillo.grouppolls.ui.screens.CreateScreen
import com.jorgelillo.grouppolls.ui.screens.HomeScreen
import com.jorgelillo.grouppolls.ui.screens.PollScreen
import com.jorgelillo.grouppolls.ui.screens.SettingsScreen
import com.jorgelillo.grouppolls.ui.screens.WelcomeScreen
import kotlinx.serialization.Serializable

// Destinations, as type-safe routes (Navigation Compose + kotlinx.serialization).
@Serializable
private data object Home

/** [share]: just created, open the share sheet once. */
@Serializable
private data class PollRoute(val code: String, val share: Boolean = false)

@Serializable
private data object Create

@Serializable
private data object Settings

@Composable
fun GroupPollsApp(vm: PollsViewModel) {
    val local by vm.local.collectAsStateWithLifecycle()
    val state = local ?: return // first disk read in progress (a few ms)
    val nav = rememberNavController()
    val openCode by vm.openCode.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val errorText = stringResource(R.string.error_generic)
    val userId by produceState("") { value = vm.userId() }

    LaunchedEffect(error) {
        if (error != null) {
            snackbar.showSnackbar(errorText)
            vm.clearError()
        }
    }

    if (state.name.isBlank()) {
        WelcomeScreen(onDone = vm::setName)
        return
    }

    // A link opened the app: show that poll on top of whatever is open.
    LaunchedEffect(openCode) {
        openCode?.let {
            nav.navigate(PollRoute(it))
            vm.consumeOpenCode()
        }
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(nav, startDestination = Home) {
            composable<Home> {
                val feed by vm.feed.collectAsStateWithLifecycle()
                HomeScreen(
                    feed = feed,
                    local = state,
                    pollFlow = vm::poll,
                    onOpen = { nav.navigate(PollRoute(it)) },
                    onCreate = { nav.navigate(Create) },
                    onSettings = { nav.navigate(Settings) }
                )
            }
            composable<PollRoute> { entry ->
                val (code, share) = entry.toRoute<PollRoute>()
                val poll by remember(code) { vm.poll(code) }.collectAsState(initial = null)
                val votes by remember(code) { vm.votes(code) }.collectAsState(initial = emptyList())
                PollScreen(
                    poll = poll,
                    votes = votes,
                    local = state,
                    userId = userId,
                    shareOnOpen = share,
                    onVote = { p, side, prediction -> vm.vote(p, side, prediction) },
                    onClose = { vm.close(it.code) },
                    onReport = vm::report,
                    onResolvePrediction = vm::resolvePrediction,
                    onBack = { nav.back() }
                )
            }
            composable<Create> {
                CreateScreen(
                    acceptedTerms = state.acceptedTerms,
                    onAcceptTerms = vm::acceptTerms,
                    onCreate = vm::create,
                    onCreated = { code -> nav.navigate(PollRoute(code, share = true)) { popUpTo<Home>() } },
                    onBack = { nav.back() }
                )
            }
            composable<Settings> {
                SettingsScreen(
                    local = state,
                    onRename = vm::setName,
                    onDeleteData = { vm.deleteMyData { nav.popBackStack<Home>(inclusive = false) } },
                    onBack = { nav.back() }
                )
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }
}

private fun NavHostController.back() {
    if (!popBackStack()) navigate(Home)
}
