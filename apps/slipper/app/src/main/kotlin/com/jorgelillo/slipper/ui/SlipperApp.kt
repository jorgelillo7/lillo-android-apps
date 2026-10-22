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
import com.jorgelillo.slipper.ui.game.GameScreen
import com.jorgelillo.slipper.ui.home.AboutSheet
import com.jorgelillo.slipper.ui.home.HomeScreen
import com.jorgelillo.slipper.ui.over.GameOverScreen
import kotlinx.serialization.Serializable

// Destinations, as type-safe routes (Navigation Compose + kotlinx.serialization). Internal, not
// private: the serializer of a private object can't be reached by reflection on the JVM (tests).
@Serializable
internal data object Home

@Serializable
internal data class Run(val seed: Long)

@Serializable
internal data class Result(val seed: Long, val score: Int, val previousBest: Int)

@Composable
internal fun SlipperApp(vm: SlipperViewModel) {
    val saved by vm.saved.collectAsStateWithLifecycle()
    val challenge by vm.challenge.collectAsStateWithLifecycle()
    val invalidLink by vm.invalidLink.collectAsStateWithLifecycle()
    val state = saved ?: return // first disk read in progress (a few ms)
    val nav = rememberNavController()
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
                best = state.best,
                challenge = challenge?.score,
                onPlay = { nav.navigate(Run(vm.nextSeed())) },
                onDismissChallenge = vm::dismissChallenge,
                onAbout = { showAbout = true }
            )
        }
        composable<Run> { entry ->
            val run = entry.toRoute<Run>()
            GameScreen(seed = run.seed, target = challenge?.score) { world ->
                val previousBest = state.best
                vm.finish(world.score, world.eaten)
                nav.navigate(Result(run.seed, world.score, previousBest)) { popUpTo<Home>() }
            }
        }
        composable<Result> { entry ->
            val result = entry.toRoute<Result>()
            GameOverScreen(
                score = result.score,
                previousBest = result.previousBest,
                challenge = challenge?.score,
                onRetry = { nav.navigate(Run(vm.nextSeed())) { popUpTo<Home>() } },
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
