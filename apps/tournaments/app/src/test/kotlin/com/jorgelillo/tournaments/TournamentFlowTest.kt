package com.jorgelillo.tournaments

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.tournaments.domain.Library
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * A whole 4-player knockout through the real MainActivity on the JVM, like tools/smoke_test.sh on
 * an emulator but in seconds: new tournament, three results, champion. One test per class: the
 * app's DataStore file can only be open once per process.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class TournamentFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun tap(tag: String) {
        compose.waitUntilExactlyOneExists(hasTestTag(tag), TIMEOUT)
        compose.onNodeWithTag(tag).performClick()
    }

    private fun saved(): Library =
        runBlocking { (compose.activity.application as TournamentsApplication).repository.state.first() }

    /** Seed order for 4 players: slot 0 = seeds 1-4, slot 1 = seeds 2-3. */
    private fun result(match: String, winner: String) {
        tap(match)
        tap(winner)
        tap("score_1")
        tap("save")
    }

    @Test
    fun aKnockoutFromNewTournamentToChampion() {
        tap("new")
        compose.onNodeWithTag("name").performTextInput("Smoke cup")
        compose.onNodeWithTag("player").performTextInput("Ann,Bob,Cat,Dan")
        compose.onNodeWithTag("player").performImeAction()
        tap("start")

        tap("tab_matches")
        result("match_elimination_1_0", "winner_a")
        result("match_elimination_1_1", "winner_b")
        result("match_elimination_2_0", "winner_a")

        compose.waitUntilExactlyOneExists(hasTestTag("champion"), TIMEOUT)
        val cup = saved().tournaments.single()
        assertEquals("Smoke cup", cup.name)
        assertEquals(listOf("Ann", "Bob", "Cat", "Dan"), cup.players.map { it.name })
        assertTrue(cup.isOver)
    }

    private companion object {
        const val TIMEOUT = 10_000L
    }
}
