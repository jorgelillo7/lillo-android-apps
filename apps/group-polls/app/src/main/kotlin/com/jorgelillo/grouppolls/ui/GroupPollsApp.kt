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
import com.jorgelillo.grouppolls.R
import com.jorgelillo.grouppolls.ui.screens.CreateScreen
import com.jorgelillo.grouppolls.ui.screens.HomeScreen
import com.jorgelillo.grouppolls.ui.screens.PollScreen
import com.jorgelillo.grouppolls.ui.screens.SettingsScreen
import com.jorgelillo.grouppolls.ui.screens.WelcomeScreen

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
            nav.navigate("poll/$it")
            vm.consumeOpenCode()
        }
    }

    Box(Modifier.fillMaxSize()) {
        NavHost(nav, startDestination = "home") {
            composable("home") {
                val feed by vm.feed.collectAsStateWithLifecycle()
                HomeScreen(
                    feed = feed,
                    local = state,
                    pollFlow = vm::poll,
                    onOpen = { nav.navigate("poll/$it") },
                    onCreate = { nav.navigate("create") },
                    onSettings = { nav.navigate("settings") }
                )
            }
            composable("poll/{code}?share={share}") { entry ->
                val code = entry.arguments?.getString("code").orEmpty()
                val share = entry.arguments?.getString("share") == "true"
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
            composable("create") {
                CreateScreen(
                    acceptedTerms = state.acceptedTerms,
                    onAcceptTerms = vm::acceptTerms,
                    onCreate = vm::create,
                    onCreated = { code -> nav.navigate("poll/$code?share=true") { popUpTo("home") } },
                    onBack = { nav.back() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    local = state,
                    onRename = vm::setName,
                    onDeleteData = { vm.deleteMyData { nav.popBackStack("home", inclusive = false) } },
                    onBack = { nav.back() }
                )
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding())
    }
}

private fun NavHostController.back() {
    if (!popBackStack()) navigate("home")
}
