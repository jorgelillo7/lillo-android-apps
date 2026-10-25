package com.jorgelillo.slipper.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.jorgelillo.core.platform.shareText
import com.jorgelillo.slipper.R
import com.jorgelillo.slipper.domain.SlipperSkin
import com.jorgelillo.slipper.ui.game.GameScreen
import com.jorgelillo.slipper.ui.game.rememberSounds
import com.jorgelillo.slipper.ui.home.AboutSheet
import com.jorgelillo.slipper.ui.home.HomeScreen
import com.jorgelillo.slipper.ui.over.GameOverScreen
import com.jorgelillo.slipper.ui.over.RunResult
import kotlinx.serialization.Serializable

// Destinations, as type-safe routes (Navigation Compose + kotlinx.serialization). Internal, not
// private: the serializer of a private object can't be reached by reflection on the JVM (tests).
@Serializable
internal data object Home

@Serializable
internal data class Run(val seed: Long, val daily: Boolean = false)

@Serializable
internal data class Result(
    val seed: Long,
    val score: Int,
    val previousBest: Int,
    val daily: Boolean = false,
    val croquetas: Int = 0,
    val nearMisses: Int = 0,
    /** The skin this run unlocked, by name. */
    val unlocked: String? = null
)

@Composable
internal fun SlipperApp(vm: SlipperViewModel) {
    val saved by vm.saved.collectAsStateWithLifecycle()
    val challenge by vm.challenge.collectAsStateWithLifecycle()
    val invalidLink by vm.invalidLink.collectAsStateWithLifecycle()
    val state = saved ?: return // first disk read in progress (a few ms)
    val nav = rememberNavController()
    val sounds = rememberSounds()
    var showAbout by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val resources = LocalResources.current

    LaunchedEffect(invalidLink) {
        if (invalidLink) {
            Toast.makeText(context, resources.getString(R.string.challenge_invalid), Toast.LENGTH_LONG).show()
            vm.dismissInvalidLink()
        }
    }
    // A challenge opened while playing waits on the home screen.
    LaunchedEffect(challenge) { if (challenge != null) nav.popBackStack(Home, inclusive = false) }

    NavHost(nav, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                saved = state,
                today = vm.today(),
                challenge = challenge?.score,
                challengeSameRun = challenge?.sameRun ?: true,
                onPlay = { nav.navigate(Run(vm.nextSeed())) },
                onDaily = { nav.navigate(Run(vm.dailySeed(), daily = true)) },
                onDismissChallenge = vm::dismissChallenge,
                onSkin = vm::chooseSkin,
                onToggleSound = vm::toggleSound,
                onAbout = { showAbout = true }
            )
        }
        composable<Run> { entry ->
            val run = entry.toRoute<Run>()
            val target = if (run.daily) null else challenge?.score
            GameScreen(
                seed = run.seed,
                target = target,
                daily = run.daily,
                skin = state.skin,
                muted = state.muted,
                sounds = sounds
            ) { world ->
                val previousBest = if (run.daily) state.dailyBest(vm.today()) else state.best
                val unlocked = vm.finish(world.score, world.eaten, run.daily)
                nav.navigate(
                    Result(
                        run.seed,
                        world.score,
                        previousBest,
                        run.daily,
                        world.eaten,
                        world.nearMisses,
                        unlocked?.name
                    )
                ) { popUpTo<Home>() }
            }
        }
        composable<Result> { entry ->
            val result = entry.toRoute<Result>()
            GameOverScreen(
                result = RunResult(
                    score = result.score,
                    previousBest = result.previousBest,
                    croquetas = result.croquetas,
                    nearMisses = result.nearMisses,
                    daily = result.daily,
                    unlocked = result.unlocked?.let(SlipperSkin::valueOf),
                    croquetasToNext = SlipperSkin.next(state.croquetas)?.let { it.unlockAt - state.croquetas }
                ),
                skin = state.skin,
                challenge = if (result.daily) null else challenge?.score,
                onRetry = {
                    val seed = if (result.daily) vm.dailySeed() else vm.nextSeed()
                    nav.navigate(Run(seed, result.daily)) { popUpTo<Home>() }
                },
                onChallengeFriend = {
                    val link = vm.challengeLink(result.seed, result.score)
                    context.shareText(resources.getString(R.string.share_challenge, formatScore(result.score), link))
                },
                onHome = { nav.popBackStack<Home>(inclusive = false) }
            )
        }
    }

    if (showAbout) AboutSheet(onDismiss = { showAbout = false })
}
