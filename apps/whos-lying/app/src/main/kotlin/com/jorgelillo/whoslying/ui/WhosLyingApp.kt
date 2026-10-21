package com.jorgelillo.whoslying.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.WhosLyingApplication
import com.jorgelillo.whoslying.domain.Match
import com.jorgelillo.whoslying.domain.WordPack
import com.jorgelillo.whoslying.ui.screens.CountdownScreen
import com.jorgelillo.whoslying.ui.screens.DebateScreen
import com.jorgelillo.whoslying.ui.screens.DrawingScreen
import com.jorgelillo.whoslying.ui.screens.ExposeScreen
import com.jorgelillo.whoslying.ui.screens.HomeScreen
import com.jorgelillo.whoslying.ui.screens.HowToSheet
import com.jorgelillo.whoslying.ui.screens.ModesSheet
import com.jorgelillo.whoslying.ui.screens.PLAY_STORE_URL
import com.jorgelillo.whoslying.ui.screens.PackEditScreen
import com.jorgelillo.whoslying.ui.screens.PackPickerScreen
import com.jorgelillo.whoslying.ui.screens.PacksScreen
import com.jorgelillo.whoslying.ui.screens.PlayersScreen
import com.jorgelillo.whoslying.ui.screens.PodiumScreen
import com.jorgelillo.whoslying.ui.screens.RankingScreen
import com.jorgelillo.whoslying.ui.screens.ReportSheet
import com.jorgelillo.whoslying.ui.screens.ResultScreen
import com.jorgelillo.whoslying.ui.screens.RevealScreen
import com.jorgelillo.whoslying.ui.screens.SettingsSheet
import com.jorgelillo.whoslying.ui.screens.SetupScreen
import com.jorgelillo.whoslying.ui.screens.TimeUpScreen
import com.jorgelillo.whoslying.ui.screens.VotingScreen
import com.jorgelillo.whoslying.ui.screens.WelcomeDialog
import kotlinx.serialization.Serializable

// Destinations, as type-safe routes (Navigation Compose + kotlinx.serialization).
@Serializable
private data object Home

@Serializable
private data object Players

@Serializable
private data object Setup

@Serializable
private data object Reveal

@Serializable
private data object Countdown

@Serializable
private data object Debate

@Serializable
private data object TimeUp

@Serializable
private data object Vote

@Serializable
private data object Expose

@Serializable
private data object Result

@Serializable
private data object Ranking

@Serializable
private data object Podium

@Serializable
private data object PickPacks

@Serializable
private data object Packs

/** Edit a custom pack ([id]), start a new one (both null) or a copy of a built-in one ([copyOf]). */
@Serializable
private data class PackEdit(val id: String? = null, val copyOf: String? = null)

@Composable
fun WhosLyingApp(app: WhosLyingApplication, incomingText: String? = null, onIncomingHandled: () -> Unit = {}) {
    val language = LocalConfiguration.current.locales[0].language
    // Keyed by language so the built-in packs follow a language change while the app is open.
    val vm: GameViewModel = viewModel(key = "game-$language", factory = GameViewModel.factory(app.repository, language))
    val saved by vm.saved.collectAsStateWithLifecycle()
    val currentGame by vm.game.collectAsStateWithLifecycle()
    val lastElimination by vm.lastElimination.collectAsStateWithLifecycle()
    val lastPoints by vm.lastPoints.collectAsStateWithLifecycle()
    val voteForced by vm.voteForced.collectAsStateWithLifecycle()
    val state = saved ?: return // first disk read in progress (a few ms)
    val nav = rememberNavController()
    var showHowTo by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showModes by rememberSaveable { mutableStateOf(false) }
    var showReport by rememberSaveable { mutableStateOf(false) }
    // The welcome note: until "don't show again", once per app start; also from Settings.
    var welcomeClosedThisTime by rememberSaveable { mutableStateOf(false) }
    var showWelcomeAgain by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val resources = LocalResources.current
    val importPack: (String) -> Unit = { text ->
        val pack = vm.importPack(text)
        val message = if (pack != null) resources.getString(R.string.pack_imported, pack.name) else resources.getString(R.string.pack_import_invalid)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    // A pack shared from another phone: add it and show the packs.
    LaunchedEffect(incomingText) {
        if (incomingText != null) {
            importPack(incomingText)
            onIncomingHandled()
            nav.navigate(Packs) { launchSingleTop = true }
        }
    }

    NavHost(nav, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onPlay = { nav.navigate(Players) },
                onHowTo = { showHowTo = true },
                onModes = { showModes = true },
                onPacks = { nav.navigate(Packs) },
                onAbout = { showAbout = true }
            )
        }
        composable<Players> {
            PlayersScreen(state.players, vm::addPlayer, vm::removePlayer, onContinue = { nav.navigate(Setup) }, onBack = { nav.popBackStack() })
        }
        composable<Setup> {
            SetupScreen(
                playerCount = state.players.size,
                settings = state.settings,
                packs = vm.allPacks(state),
                unplayed = vm.unplayed(state),
                onChange = vm::updateSettings,
                onModesInfo = { showModes = true },
                onChoosePacks = { nav.navigate(PickPacks) },
                onStart = { if (vm.startGame()) nav.navigate(Reveal) },
                onBack = { nav.popBackStack() }
            )
        }
        composable<Reveal> {
            val game = currentGame ?: return@composable LaunchedEffect(Unit) { nav.goHome() }
            RevealScreen(game, onDone = { nav.navigate(Countdown) { popUpTo<Setup>() } }, onQuit = { nav.goHome() })
        }
        composable<Countdown> {
            val game = currentGame ?: return@composable LaunchedEffect(Unit) { nav.goHome() }
            CountdownScreen(game.starter, onDone = { nav.navigate(Debate) { popUpTo<Setup>() } })
        }
        composable<Debate> {
            val game = currentGame ?: return@composable LaunchedEffect(Unit) { nav.goHome() }
            LaunchedEffect(game.isOver) { if (game.isOver) nav.navigate(Result) { popUpTo<Setup>() } }
            val alive = remember(game) { game.alive.map { it.player }.toSet() }
            if (state.settings.drawing) {
                DrawingScreen(
                    game = game,
                    alive = alive,
                    strokes = vm.strokes,
                    discussionSeconds = state.settings.discussionSeconds,
                    onVote = { nav.navigate(Vote) },
                    onTimeUp = {
                        vm.forceVote()
                        nav.navigate(TimeUp)
                    },
                    onRevealAll = vm::revealAll,
                    onQuit = { nav.goHome() }
                )
            } else {
                DebateScreen(
                    game = game,
                    alive = alive,
                    discussionSeconds = state.settings.discussionSeconds,
                    onVote = { nav.navigate(Vote) },
                    onTimeUp = {
                        vm.forceVote()
                        nav.navigate(TimeUp)
                    },
                    onRevealAll = vm::revealAll,
                    onQuit = { nav.goHome() }
                )
            }
        }
        composable<TimeUp> {
            TimeUpScreen(onDone = { nav.navigate(Vote) { popUpTo<TimeUp> { inclusive = true } } })
        }
        composable<Vote> {
            val game = currentGame ?: return@composable LaunchedEffect(Unit) { nav.goHome() }
            VotingScreen(
                game = game,
                alive = remember(game) { game.alive.map { it.player }.toSet() },
                forced = voteForced,
                onConfirm = { player ->
                    vm.eliminate(player)
                    nav.navigate(Expose) { popUpTo<Debate>() }
                },
                onBack = { nav.popBackStack() }
            )
        }
        composable<Expose> {
            val game = currentGame ?: return@composable LaunchedEffect(Unit) { nav.goHome() }
            // Null for a moment after "keep playing" while this screen leaves.
            val elimination = lastElimination ?: return@composable
            val toResult = { nav.navigate(Result) { popUpTo<Setup>() } }
            ExposeScreen(
                game = game,
                elimination = elimination,
                onGuess = { text -> vm.guess(text).also { if (vm.game.value?.isOver == true) toResult() } },
                onContinue = {
                    vm.dismissElimination()
                    nav.popBackStack()
                },
                onGameOver = toResult
            )
        }
        composable<Result> {
            val game = currentGame ?: return@composable LaunchedEffect(Unit) { nav.goHome() }
            val context = LocalContext.current
            val caption = stringResource(R.string.drawing_share_caption, game.civilianWord, PLAY_STORE_URL)
            ResultScreen(
                game = game,
                lastElimination = lastElimination,
                drawing = vm.strokes,
                onShareDrawing = { shareDrawing(context, vm.strokes, caption) },
                roundsLeft = state.settings.rounds.takeIf { it > 0 }?.let { Match.remaining(state.roundsPlayed, it) },
                onSeeWinner = { nav.navigate(Podium) { popUpTo<Setup>() } },
                onReport = { showReport = true },
                onContinue = { nav.navigate(Ranking) },
                onHome = { nav.goHome() }
            )
        }
        composable<Ranking> {
            RankingScreen(
                players = state.players,
                scores = state.scores,
                lastPoints = lastPoints,
                roundsLeft = state.settings.rounds.takeIf { it > 0 }?.let { Match.remaining(state.roundsPlayed, it) },
                roundsPlayed = state.roundsPlayed,
                onNextRound = { if (vm.startGame()) nav.navigate(Reveal) { popUpTo<Setup>() } },
                onReset = vm::resetScores,
                onBack = { nav.popBackStack() }
            )
        }
        composable<Podium> {
            PodiumScreen(
                players = state.players,
                scores = state.scores,
                onNewMatch = {
                    vm.resetScores()
                    nav.popBackStack<Setup>(inclusive = false)
                },
                onHome = {
                    vm.resetScores()
                    nav.goHome()
                }
            )
        }
        composable<PickPacks> {
            PackPickerScreen(
                packs = vm.allPacks(state),
                selected = state.settings.packIds,
                onToggle = vm::togglePack,
                onSelectAll = vm::selectAllPacks,
                onNewPack = { nav.navigate(PackEdit()) },
                onDone = { nav.popBackStack() }
            )
        }
        composable<Packs> {
            PacksScreen(
                vm.builtInPacks,
                state.customPacks,
                onEdit = { id -> nav.navigate(PackEdit(id)) },
                onCopy = { pack -> nav.navigate(PackEdit(copyOf = pack.id)) },
                onImport = importPack,
                onBack = { nav.popBackStack() }
            )
        }
        composable<PackEdit> { entry ->
            val route = entry.toRoute<PackEdit>()
            val id = route.id
            val copyOf = route.copyOf
            val isNew = id == null
            val copyName = stringResource(R.string.pack_copy_name, vm.builtInPacks.firstOrNull { it.id == copyOf }?.name.orEmpty())
            val initial = remember(route) {
                when {
                    // A built-in pack as a starting point; nothing is saved until "Save".
                    copyOf != null -> vm.builtInPacks.firstOrNull { it.id == copyOf }
                        ?.copy(id = vm.newPackId(), name = copyName, custom = true)

                    id == null -> WordPack(vm.newPackId(), "", "📝", emptyList(), custom = true)

                    else -> state.customPacks.firstOrNull { it.id == id }
                }
            } ?: return@composable LaunchedEffect(Unit) { nav.popBackStack() }
            PackEditScreen(
                initial = initial,
                isNew = isNew,
                onSave = {
                    vm.savePack(it)
                    nav.popBackStack()
                },
                onDelete = {
                    vm.deletePack(initial.id)
                    nav.popBackStack()
                },
                onBack = { nav.popBackStack() }
            )
        }
    }

    if ((!state.welcomeSeen && !welcomeClosedThisTime) || showWelcomeAgain) {
        WelcomeDialog(showCheckbox = !state.welcomeSeen) { dontShowAgain ->
            welcomeClosedThisTime = true
            showWelcomeAgain = false
            if (dontShowAgain) vm.markWelcomeSeen()
        }
    }
    if (showHowTo) HowToSheet(onDismiss = { showHowTo = false })
    if (showAbout) {
        SettingsSheet(
            onHowTo = { showHowTo = true },
            onModes = { showModes = true },
            onWelcome = { showWelcomeAgain = true },
            onDismiss = { showAbout = false }
        )
    }
    if (showModes) ModesSheet(onDismiss = { showModes = false })
    val reported = currentGame
    if (showReport && reported != null) {
        ReportSheet(
            word = reported.civilianWord,
            decoy = reported.cards.firstNotNullOfOrNull { card -> card.word?.takeIf { it != reported.civilianWord } },
            pack = reported.category,
            language = language,
            onDismiss = { showReport = false }
        )
    }
}

private fun NavHostController.goHome() {
    popBackStack<Home>(inclusive = false)
}
