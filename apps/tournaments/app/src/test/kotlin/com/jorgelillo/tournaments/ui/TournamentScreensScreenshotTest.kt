package com.jorgelillo.tournaments.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.core.screenshottesting.captureScreenshot
import com.jorgelillo.tournaments.domain.Format
import com.jorgelillo.tournaments.domain.Library
import com.jorgelillo.tournaments.domain.Tournament
import com.jorgelillo.tournaments.domain.Tournaments
import com.jorgelillo.tournaments.ui.home.HomeScreen
import com.jorgelillo.tournaments.ui.stats.StatsScreen
import com.jorgelillo.tournaments.ui.theme.ArenaColorScheme
import com.jorgelillo.tournaments.ui.tournament.TournamentScreen
import kotlin.random.Random
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class TournamentScreensScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    /** An 8-player knockout played to the end: the first player listed wins every match 2-1. */
    private val cup: Tournament = run {
        var t = Tournaments.create("cup", "Friday Magic", 20_400, listOf("Alex", "Sam", "Jordan", "Taylor", "Morgan", "Casey", "Riley", "Jamie"), Format.ELIMINATION, random = Random(1))
        while (Tournaments.pending(t).isNotEmpty()) t = Tournaments.record(t, Tournaments.pending(t).first(), 2, 1)
        t
    }
    private val library = Library(listOf(cup))

    private fun shot(name: String, content: @Composable () -> Unit) = compose.captureScreenshot(name) { LilloTheme(ArenaColorScheme) { content() } }

    private fun home(name: String) = shot(name) { HomeScreen(library, {}, {}, {}, {}, {}) }

    private fun tournament(name: String) = shot(name) { TournamentScreen(cup, {}, {}, {}) }

    private fun stats(name: String) = shot(name) { StatsScreen(library) {} }

    @Test fun homePortrait() = home("home")

    @Test
    @Config(qualifiers = "+land")
    fun homeLandscape() = home("home_landscape")

    @Test fun tournamentPortrait() = tournament("tournament")

    @Test
    @Config(qualifiers = "+land")
    fun tournamentLandscape() = tournament("tournament_landscape")

    @Test fun statsPortrait() = stats("stats")

    @Test
    @Config(qualifiers = "+land")
    fun statsLandscape() = stats("stats_landscape")
}
