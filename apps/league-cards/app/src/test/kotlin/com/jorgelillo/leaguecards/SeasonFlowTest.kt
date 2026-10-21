package com.jorgelillo.leaguecards

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.leaguecards.domain.Saved
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * A whole season of the sample deck through the real MainActivity on the JVM, like
 * tools/smoke_test.sh on an emulator but in seconds: new season, the joker, every card and matchday
 * report, the ending and the trophies. One test per class: the app's DataStore file can only be
 * open once per process.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class SeasonFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun shown(tag: String) = compose.onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty()

    private fun tap(tag: String) {
        compose.waitUntilExactlyOneExists(hasTestTag(tag), TIMEOUT)
        compose.onNodeWithTag(tag).performClick()
    }

    private fun saved(): Saved =
        runBlocking { (compose.activity.application as LeagueCardsApplication).store.state.first() }

    @Test
    fun aWholeSeasonEndsAndIsRecorded() {
        tap("new_season")
        tap("joker")

        // Each move is saved to disk, which the UI test doesn't wait for: after a tap, wait for the
        // saved season to change before reading the screen again.
        var moves = 0
        while (!shown("ending")) {
            check(moves++ < MAX_MOVES) { "the season never ended" }
            compose.waitForIdle()
            val target = when {
                shown("next") -> "next"
                shown("choose_right") -> if (moves % 2 == 0) "choose_left" else "choose_right"
                else -> continue
            }
            val before = saved().season
            compose.onAllNodes(hasTestTag(target)).onFirst().performClick()
            compose.waitUntil(TIMEOUT) { saved().season != before }
        }

        compose.waitUntil(TIMEOUT) { saved().seasonsPlayed == 1 }
        assertEquals(1, saved().history.size)
        tap("trophies")
        assertTrue(shown("ending_${saved().history.single().ending}"))
    }

    private companion object {
        const val TIMEOUT = 10_000L
        const val MAX_MOVES = 300
    }
}
