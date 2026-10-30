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
import com.jorgelillo.leaguecards.domain.Outcome
import com.jorgelillo.leaguecards.domain.Saved
import com.jorgelillo.leaguecards.domain.Season
import com.jorgelillo.leaguecards.domain.SeasonRecord
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

    /**
     * A tester's phone mid-season: a card as long as the league's longest (~140 characters; the
     * sample deck's are shorter) under a chat message. The card's text must show whole.
     */
    private fun playLongCardWithChat(name: String) = shot(name) {
        val card = league.cards.first { !it.followUpOnly }.copy(
            text = "Soy huevito, es mi primera temporada: no me líes. ¿Me vendes a tu portero titular por " +
                "lo que vale en el mercado o me lo quitas de las manos?"
        )
        val withLong = league.copy(cards = listOf(card) + league.cards.filterNot { it.id == card.id })
        val outcome = Outcome(league.characters.last().id, "«Por Gibraltar y las Malvinas». Te gana un amigo.")
        PlayScreen(
            withLong,
            season.copy(hand = listOf(card.id), handIndex = 0, lastOutcome = outcome, matchdayPoints = 18),
            {},
            {},
            {},
            {}
        )
    }

    // A smaller phone with a larger font, as the tester's.
    @Test
    @Config(qualifiers = "w360dp-h700dp-xxhdpi", fontScale = 1.3f)
    fun playLongCardWithChat() = playLongCardWithChat("play_long_card_chat")

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

    /** Tablet in landscape: meters and the chat beside the card, after one answer so the chat shows. */
    @Test
    @Config(qualifiers = RobolectricDeviceQualifiers.MediumTablet)
    fun playTablet() = shot("play_tablet") {
        PlayScreen(league, Game.choose(league, season, right = true), {}, {}, {}, {})
    }

    /** Tablet upright: the phone layout at reading width. */
    @Test
    @Config(qualifiers = "w800dp-h1280dp-xlarge-port-xhdpi")
    fun playTabletUpright() = play("play_tablet_upright")

    @Test fun reportPortrait() = report("report")

    @Test
    @Config(qualifiers = "+land")
    fun reportLandscape() = report("report_landscape")

    @Test fun trophiesPortrait() = shot("trophies") { TrophiesScreen(league, Saved()) {} }

    /** Some unlocked (gold edge) next to locked ones; a won league in the history. */
    @Test
    fun trophiesSomeUnlocked() = shot("trophies_unlocked") {
        val saved = Saved(
            seasonsPlayed = 2,
            titles = 1,
            bestPosition = 1,
            endingsSeen = league.endings.keys.take(2).toSet(),
            achievements = league.achievements.take(3).map { it.id }.toSet(),
            history = listOf(SeasonRecord(20_400, 19, 1, 812, "champion"), SeasonRecord(20_380, 19, 6, 233, "finished"))
        )
        TrophiesScreen(league, saved) {}
    }
}
