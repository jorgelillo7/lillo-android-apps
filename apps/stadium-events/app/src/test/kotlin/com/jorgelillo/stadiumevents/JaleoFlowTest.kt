package com.jorgelillo.stadiumevents

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * The app's one journey through the real MainActivity on the JVM, like tools/smoke_test.sh on an
 * emulator but in seconds: first open downloads the events and answers tomorrow (a sample match),
 * lists the next busy days, and the About sheet opens and closes. One test per class: the app's
 * DataStore file can only be open once per process.
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
@Config(
    sdk = [36],
    qualifiers = RobolectricDeviceQualifiers.Pixel7,
    application = TestStadiumEventsApplication::class
)
class JaleoFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val app get() = compose.activity.application as StadiumEventsApplication

    private fun string(id: Int) = compose.activity.getString(id)

    private fun waitFor(matcher: androidx.compose.ui.test.SemanticsMatcher) =
        compose.waitUntilAtLeastOneExists(matcher, TIMEOUT)

    @Test
    fun firstOpenAnswersTomorrowAndShowsWhatsNext() {
        waitFor(hasText(string(R.string.answer_busy)))
        waitFor(hasText("Atlético – Viking FK (Champions)"))
        waitFor(hasTestTag("upcoming_atletico-vs-betis"))
        waitFor(hasTestTag("upcoming_2027-uefa-champions-league-final"))
        compose.waitUntil(TIMEOUT) { runBlocking { app.repository.state.first().fetchedAt != null } }

        compose.onAllNodes(hasContentDescription(string(R.string.action_about))).onFirst().performClick()
        waitFor(hasText(string(R.string.about_title)))
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        waitFor(hasText(string(R.string.answer_busy)))
    }

    private companion object {
        const val TIMEOUT = 10_000L
    }
}
