package com.jorgelillo.whoslying

import androidx.compose.ui.test.ComposeTimeoutException
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToString
import androidx.compose.ui.test.swipeUp
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.whoslying.domain.Outcome
import com.jorgelillo.whoslying.domain.Role
import com.jorgelillo.whoslying.ui.GameViewModel
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * One whole game through the real app on the JVM, like tools/smoke_test.sh on an emulator but in
 * seconds: welcome, players, setup, the pass-the-phone reveal, countdown, debate, vote, exposure,
 * result. Taps go by test tag, as on the emulator. One test per class: the app's DataStore file
 * can only be open once per process.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class GameFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private fun tap(tag: String) {
        try {
            compose.waitUntilExactlyOneExists(hasTestTag(tag), TIMEOUT)
        } catch (e: ComposeTimeoutException) {
            // Say which step and what was on screen instead: a bare timeout tells nothing on CI.
            val screen = compose.onAllNodes(isRoot(), useUnmergedTree = true).printToString()
            throw AssertionError("'$tag' never showed up. On screen:\n$screen", e)
        }
        compose.onNodeWithTag(tag).performClick()
    }

    /** The app's own ViewModel, to vote for whoever got the impostor card. */
    private fun viewModel(): GameViewModel {
        val activity = compose.activity
        val factory = GameViewModel.factory((activity.application as WhosLyingApplication).repository, "en")
        return ViewModelProvider(activity, factory)["game-en", GameViewModel::class.java]
    }

    @Test
    fun aWholeGameEndsWithTheImpostorCaught() {
        tap("welcome_ok")
        tap("play")
        listOf("Ana", "Bea", "Carlos").forEach { name ->
            compose.onNodeWithTag("player_name").performTextInput(name)
            compose.onNodeWithTag("player_name").performImeAction()
        }
        // "Continue" turns on once the third player is saved, off the UI thread: tapping it before
        // that lands taps a disabled button and the game never starts (the CI flake of 2026-10).
        compose.waitUntil(TIMEOUT) { viewModel().saved.value?.players?.size == 3 }
        tap("continue")
        tap("start")

        // Pass the phone: each player slides the curtain up, reads the card and hides it.
        repeat(3) {
            compose.waitUntilExactlyOneExists(hasTestTag("tap_to_reveal"), TIMEOUT)
            compose.onNodeWithTag("tap_to_reveal").performTouchInput { swipeUp() }
            tap("hide")
        }

        // The 3-2-1 countdown, then the debate.
        tap("go_vote")
        val impostor = viewModel().game.value!!.cards.single { it.role == Role.IMPOSTOR }.player
        tap("vote_$impostor")
        tap("confirm_vote")

        // "Exposed in 3, 2, 1…", then straight to the result: catching the only impostor ends the game.
        compose.waitUntilExactlyOneExists(hasTestTag("result_title"), TIMEOUT)
        assertEquals(Outcome.CiviliansWin, viewModel().game.value!!.outcome)
    }

    private companion object {
        // Generous: on a busy CI runner the first DataStore read and screen can take over 10 s.
        const val TIMEOUT = 30_000L
    }
}
