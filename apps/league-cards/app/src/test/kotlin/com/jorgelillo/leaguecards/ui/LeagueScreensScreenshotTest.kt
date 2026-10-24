package com.jorgelillo.leaguecards.ui

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.core.screenshottesting.captureScreenshot
import com.jorgelillo.leaguecards.domain.Game
import com.jorgelillo.leaguecards.domain.League
import com.jorgelillo.leaguecards.domain.Meters
import com.jorgelillo.leaguecards.domain.Saved
import com.jorgelillo.leaguecards.domain.Season
import com.jorgelillo.leaguecards.ui.theme.ChatColorScheme
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The public sample deck only (the league's own deck never goes into this repository). */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// A bare Application: the app's own onCreate (WorkManager, Firebase…) has no place in a screenshot.
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7, application = Application::class)
class LeagueScreensScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val league = League.parse(File("src/main/assets/league.json").readText())
    private val season = Game.newSeason(league, seed = 7)

    /** Swipes right until the first matchday report shows up. */
    private val firstReport: Season = generateSequence(season) {
        Game.choose(league, it, right = true)
    }.first { it.reportPending }

    private fun shot(name: String, content: @Composable () -> Unit) = compose.captureScreenshot(name) {
        LilloTheme(ChatColorScheme) { content() }
    }

    private fun play(name: String) = shot(name) { PlayScreen(league, season, {}, {}, {}, {}) }

    // The chat messages come in one by one (700 ms apart): capture once they are all there.
    private fun report(name: String) = compose.captureScreenshot(name, { mainClock.advanceTimeBy(15_000) }) {
        LilloTheme(ChatColorScheme) {
            ReportScreen(league, firstReport.report!!, unlocked = emptyList(), seasonOver = false, onNext = {
            }, onTable = {}, onBack = {})
        }
    }

    /**
     * The longest card on a handheld with a large font (a tester's AYN-style console): the answers
     * and the whole text must still show, not be clipped.
     */
    private fun playLongCardLargeFont(name: String) = shot(name) {
        val longest = league.cards.filterNot { it.followUpOnly }.maxBy { it.text.length + it.left.label.length }
        // Meters at different levels, one in danger: each level must read at a glance.
        val meters = Meters(money = 80, squad = 30, patience = 10, tears = 60)
        PlayScreen(league, season.copy(hand = listOf(longest.id), handIndex = 0, meters = meters), {}, {}, {}, {})
    }

    /** Home with a season in progress: continue, the autosave note, a full or a short season. */
    private fun home(name: String) = shot(name) {
        HomeScreen(league, Saved().with(league, season, today = 0), onContinue = {}, onNew = {}, onTrophies = {})
    }

    @Test fun homePortrait() = home("home")

    @Test
    @Config(qualifiers = "+land")
    fun homeLandscape() = home("home_landscape")

    @Test fun playPortrait() = play("play")

    @Test
    @Config(qualifiers = "+land", fontScale = 1.3f)
    fun playLandscapeLargeFont() = playLongCardLargeFont("play_landscape_large_font")

    @Test
    @Config(fontScale = 1.3f)
    fun playPortraitLargeFont() = playLongCardLargeFont("play_large_font")

    @Test
    @Config(qualifiers = "+land")
    fun playLandscape() = play("play_landscape")

    @Test fun reportPortrait() = report("report")

    @Test
    @Config(qualifiers = "+land")
    fun reportLandscape() = report("report_landscape")

    @Test fun trophiesPortrait() = shot("trophies") { TrophiesScreen(league, Saved()) {} }
}
