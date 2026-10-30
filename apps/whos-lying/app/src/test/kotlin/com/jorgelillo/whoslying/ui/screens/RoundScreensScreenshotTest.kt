package com.jorgelillo.whoslying.ui.screens

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
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
import com.jorgelillo.whoslying.domain.WordPacks
import com.jorgelillo.whoslying.ui.DrawStroke
import com.jorgelillo.whoslying.ui.InkColors
import com.jorgelillo.whoslying.ui.theme.Neon
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

    private fun shot(
        name: String,
        beforeCapture: () -> Unit = {
        },
        content: @Composable () -> Unit
    ) = compose.captureScreenshot(name, {
        beforeCapture()
    }) { LilloTheme(NeonColorScheme) { content() } }

    /** Home with a group mid-match (summary header), and a first launch (no group yet). */
    private fun home(name: String, group: HomeGroup?) = shot(name) { HomeScreen(group, {}, {}, {}, {}, {}) }

    private val group = HomeGroup(
        players,
        GameSettings(GameMode.CLASSIC, rounds = 10, drawing = true),
        scores = mapOf("Bea" to 12, "Ana" to 7),
        roundsPlayed = 3
    )

    @Test fun homeWithGroup() = home("home_group", group)

    @Test
    @Config(qualifiers = "+land")
    fun homeWithGroupLandscape() = home("home_group_landscape", group)

    @Test fun homeFirstLaunch() = home("home", null)

    private fun voting(name: String) = shot(name) {
        val game = deal(GameMode.CLASSIC)
        VotingScreen(game, game.alive.map { it.player }.toSet(), forced = false, onConfirm = {}, onBack = {})
    }

    private fun lastChance(name: String) {
        val game = deal(GameMode.DRIFTER).let {
            it.eliminate(it.cards.single { card -> card.role == Role.DRIFTER }.player)
        }
        // Past the "exposed in 3, 2, 1…" countdown.
        shot(name, { compose.mainClock.advanceTimeBy(3_000) }) {
            ExposeScreen(game, game.lastElimination!!, onGuess = { false }, onContinue = {}, onGameOver = {})
        }
    }

    private fun result(name: String) = shot(name) {
        // A long decoy next to a long name: the name must stay whole (a tester saw "Fr / an / co").
        val longPack = WordPack("games", "Games", "🎮", listOf(Entry("Illidan", listOf("Demon hunter of Outland"))))
        val names = listOf("Francisco Javier", "Bea", "Carlos", "Dani", "Eva")
        val game = Game.deal(names, GameSettings(), longPack, longPack.entries.first(), Random(3)).let {
            it.eliminate(it.cards.single { card -> card.role == Role.IMPOSTOR }.player)
        }
        ResultScreen(game, game.lastElimination, emptyList(), {
        }, roundsLeft = 2, {}, onReport = {}, onContinue = {}, onHome = {})
    }

    private fun ranking(name: String) = shot(name) {
        RankingScreen(
            players,
            mapOf(
                "Ana" to 15,
                "Bea" to 10,
                "Carlos" to 5
            ),
            mapOf("Ana" to 5),
            roundsLeft = 1,
            roundsPlayed = 2,
            {
            },
            {},
            {}
        )
    }

    private fun podium(name: String) = shot(name) {
        PodiumScreen(players, mapOf("Ana" to 25, "Bea" to 15, "Carlos" to 10, "Dani" to 5), {}, {})
    }

    /** A full list: no name field, just the "remove someone" notice. */
    private fun playersFull(name: String) = shot(name) {
        PlayersScreen((1..24).map { "Player $it" }, onAdd = {}, onRemove = {}, onContinue = {}, onBack = {})
    }

    private fun drawing(name: String) = shot(name) {
        val game = Game.deal(players, GameSettings(drawing = true), pack, pack.entries.first(), Random(3))
        val strokes = remember {
            mutableStateListOf(
                DrawStroke(InkColors[4]).apply {
                    points +=
                        listOf(Offset(0.2f, 0.7f), Offset(0.5f, 0.3f), Offset(0.8f, 0.7f))
                }
            )
        }
        DrawingScreen(game, game.alive.map { it.player }.toSet(), strokes, 0, {}, {}, {}, {})
    }

    private fun colorWheel(name: String) = shot(name) {
        // The dialog's content (a dialog is a second window, out of reach of a root capture).
        Box(Modifier.background(Neon.Card).padding(24.dp)) { ColorWheel(Color(0xFF2F6BFF), onChange = {}) }
    }

    /** Choosing packs: every pack ticked, with "clear all" and the long-press hint. */
    private fun packPicker(name: String) = shot(name) {
        PackPickerScreen(WordPacks.builtIn("en"), selected = emptySet(), onNewPack = {}, onDone = {})
    }

    @Test fun packPickerPortrait() = packPicker("pack_picker")

    @Test
    @Config(qualifiers = "+land")
    fun packPickerLandscape() = packPicker("pack_picker_landscape")

    @Test fun playersFullPortrait() = playersFull("players_full")

    @Test
    @Config(qualifiers = "+land")
    fun playersFullLandscape() = playersFull("players_full_landscape")

    @Test fun drawingPortrait() = drawing("drawing")

    @Test
    @Config(qualifiers = "+land")
    fun drawingLandscape() = drawing("drawing_landscape")

    @Test fun colorWheelPortrait() = colorWheel("color_wheel")

    @Test
    @Config(qualifiers = "+land")
    fun colorWheelLandscape() = colorWheel("color_wheel_landscape")

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
