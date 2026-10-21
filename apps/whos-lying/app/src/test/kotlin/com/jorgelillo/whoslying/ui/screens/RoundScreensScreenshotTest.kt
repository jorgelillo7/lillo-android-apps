package com.jorgelillo.whoslying.ui.screens

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.core.screenshottesting.captureScreenshot
import com.jorgelillo.whoslying.domain.Entry
import com.jorgelillo.whoslying.domain.Game
import com.jorgelillo.whoslying.domain.GameMode
import com.jorgelillo.whoslying.domain.GameSettings
import com.jorgelillo.whoslying.domain.Role
import com.jorgelillo.whoslying.domain.WordPack
import com.jorgelillo.whoslying.ui.theme.NeonColorScheme
import kotlin.random.Random
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The game's key screens, upright and on a short landscape screen (phones turned sideways,
 * handhelds like the AYN Thor): what used to fall below the fold shows up here in seconds.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// A bare Application: the app's own onCreate (WorkManager, Firebase…) has no place in a screenshot.
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7, application = Application::class)
class RoundScreensScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val players = listOf("Ana", "Bea", "Carlos", "Dani", "Eva")
    private val pack = WordPack("places", "Places", "🗺️", listOf(Entry("Beach", listOf("Pool"))))

    private fun deal(mode: GameMode) = Game.deal(players, GameSettings(mode), pack, pack.entries.first(), Random(3))

    private fun shot(name: String, beforeCapture: () -> Unit = {}, content: @Composable () -> Unit) = compose.captureScreenshot(name, { beforeCapture() }) { LilloTheme(NeonColorScheme) { content() } }

    private fun voting(name: String) = shot(name) {
        val game = deal(GameMode.CLASSIC)
        VotingScreen(game, game.alive.map { it.player }.toSet(), forced = false, onConfirm = {}, onBack = {})
    }

    private fun lastChance(name: String) {
        val game = deal(GameMode.DRIFTER).let { it.eliminate(it.cards.single { card -> card.role == Role.DRIFTER }.player) }
        // Past the "exposed in 3, 2, 1…" countdown.
        shot(name, { compose.mainClock.advanceTimeBy(3_000) }) {
            ExposeScreen(game, game.lastElimination!!, onGuess = { false }, onContinue = {}, onGameOver = {})
        }
    }

    private fun result(name: String) = shot(name) {
        val game = deal(GameMode.CLASSIC).let { it.eliminate(it.cards.single { card -> card.role == Role.IMPOSTOR }.player) }
        ResultScreen(game, game.lastElimination, emptyList(), {}, roundsLeft = 2, {}, onReport = {}, onContinue = {}, onHome = {})
    }

    private fun ranking(name: String) = shot(name) {
        RankingScreen(players, mapOf("Ana" to 15, "Bea" to 10, "Carlos" to 5), mapOf("Ana" to 5), roundsLeft = 1, roundsPlayed = 2, {}, {}, {})
    }

    private fun podium(name: String) = shot(name) {
        PodiumScreen(players, mapOf("Ana" to 25, "Bea" to 15, "Carlos" to 10, "Dani" to 5), {}, {})
    }

    @Test fun votingPortrait() = voting("voting")

    @Test
    @Config(qualifiers = "+land")
    fun votingLandscape() = voting("voting_landscape")

    @Test fun drifterLastChancePortrait() = lastChance("drifter_last_chance")

    @Test
    @Config(qualifiers = "+land")
    fun drifterLastChanceLandscape() = lastChance("drifter_last_chance_landscape")

    @Test fun resultPortrait() = result("result")

    @Test
    @Config(qualifiers = "+land")
    fun resultLandscape() = result("result_landscape")

    @Test fun rankingPortrait() = ranking("ranking")

    @Test
    @Config(qualifiers = "+land")
    fun rankingLandscape() = ranking("ranking_landscape")

    @Test fun podiumPortrait() = podium("podium")

    @Test
    @Config(qualifiers = "+land")
    fun podiumLandscape() = podium("podium_landscape")
}
