package com.jorgelillo.tournaments.ui

import android.app.Application
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
import com.jorgelillo.tournaments.ui.theme.ArenaDark
import com.jorgelillo.tournaments.ui.theme.ArenaLight
import com.jorgelillo.tournaments.ui.tournament.TournamentScreen
import kotlin.random.Random
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// A bare Application: the app's own onCreate (WorkManager, Firebase…) has no place in a screenshot.
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7, application = Application::class)
class TournamentScreensScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    /** An 8-player knockout played to the end: the first player listed wins every match 2-1. */
    private val cup: Tournament = run {
        var t = Tournaments.create(
            "cup",
            "Friday Magic",
            20_400,
            listOf("Alex", "Sam", "Jordan", "Taylor", "Morgan", "Casey", "Riley", "Jamie"),
            Format.ELIMINATION,
            random = Random(1)
        )
        while (Tournaments.pending(t).isNotEmpty()) t = Tournaments.record(t, Tournaments.pending(t).first(), 2, 1)
        t
    }

    /** A Swiss league with one result in: "in progress" next to the finished cup on the home screen. */
    private val league: Tournament = Tournaments.create(
        "league",
        "Office ping-pong",
        20_410,
        listOf("Chris", "Robin", "Max", "Kim", "Lee", "Pat"),
        Format.SWISS,
        random = Random(2)
    ).let { Tournaments.record(it, Tournaments.pending(it).first(), 2, 0) }
    private val library = Library(listOf(cup, league))

    /** Each screen in the dark look under its name, and in the light look with `_light` added. */
    private fun shot(name: String, dark: Boolean = true, content: @Composable () -> Unit) =
        compose.captureScreenshot(if (dark) name else "${name}_light") {
            LilloTheme(if (dark) ArenaDark else ArenaLight) { content() }
        }

    private fun home(name: String, dark: Boolean = true) = shot(name, dark) { HomeScreen(library, {}, {}, {}, {}, {}) }

    private fun tournament(name: String, dark: Boolean = true, t: Tournament = cup) =
        shot(name, dark) { TournamentScreen(t, {}, {}, {}) }

    private fun stats(name: String, dark: Boolean = true) = shot(name, dark) { StatsScreen(library) {} }

    /** Eight players, the first round half played: a phone opens the bracket on that round. */
    private val openCup: Tournament = Tournaments.create(
        "open",
        "Friday Magic",
        20_400,
        listOf("Ann", "Bob", "Cat", "Dan", "Eve", "Fay", "Gus", "Hal"),
        Format.ELIMINATION,
        random = Random(1)
    ).let { t -> Tournaments.pending(t).take(2).fold(t) { acc, m -> Tournaments.record(acc, m, 2, 1) } }

    @Test
    fun homePortraitLight() = home("home", dark = false)

    @Test
    @Config(qualifiers = "+land")
    fun homeLandscapeLight() = home("home_landscape", dark = false)

    @Test
    fun tournamentPortraitLight() = tournament("tournament", dark = false)

    @Test
    @Config(qualifiers = "+land")
    fun tournamentLandscapeLight() = tournament("tournament_landscape", dark = false)

    @Test
    fun bracketRoundByRound() = tournament("bracket_rounds", t = openCup)

    @Test
    fun bracketRoundByRoundLight() = tournament("bracket_rounds", dark = false, t = openCup)

    @Test
    @Config(qualifiers = "+land")
    fun bracketFittedInLandscape() = tournament("bracket_fit_landscape", dark = false, t = openCup)

    @Test
    fun statsPortraitLight() = stats("stats", dark = false)

    @Test fun homePortrait() = home("home")

    /** First launch: the summary invites to make the first tournament. */
    @Test fun homeEmpty() = shot("home_empty") { HomeScreen(Library(emptyList()), {}, {}, {}, {}, {}) }

    @Test
    @Config(qualifiers = "+land")
    fun homeLandscape() = home("home_landscape")

    @Test fun tournamentPortrait() = tournament("tournament")

    @Test
    @Config(qualifiers = "+land")
    fun tournamentLandscape() = tournament("tournament_landscape")

    /** Tablet in landscape: the matches and the bracket side by side. */
    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.MediumTablet)
    fun tournamentTablet() = tournament("tournament_tablet")

    /** Tablet upright: not wide enough for two panes, the phone layout. */
    @Test
    @Config(qualifiers = TABLET_UPRIGHT)
    fun tournamentTabletUpright() = tournament("tournament_tablet_upright")

    @Test fun statsPortrait() = stats("stats")

    @Test
    @Config(qualifiers = "+land")
    fun statsLandscape() = stats("stats_landscape")
}

/** A 10-inch tablet held upright (800dp wide: below the two-pane width). */
private const val TABLET_UPRIGHT = "w800dp-h1280dp-xlarge-port-xhdpi"
