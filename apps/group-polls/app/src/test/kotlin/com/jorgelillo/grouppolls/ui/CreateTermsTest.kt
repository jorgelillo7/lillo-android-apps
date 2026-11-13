package com.jorgelillo.grouppolls.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.grouppolls.domain.Visibility
import com.jorgelillo.grouppolls.ui.screens.CreateScreen
import com.jorgelillo.grouppolls.ui.theme.PollsLight
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** The terms are asked before the first public poll, once, and the poll is then created. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = Application::class)
class CreateTermsTest {
    @get:Rule
    val compose = createComposeRule()

    private val created = mutableListOf<Visibility>()
    private var accepted by mutableStateOf(false)

    private fun draft(visibility: String) {
        compose.setContent {
            LilloTheme(PollsLight) {
                CreateScreen(
                    acceptedTerms = accepted,
                    onAcceptTerms = { accepted = true },
                    creating = false,
                    onCreate = { _, _, _, v, _ -> created += v },
                    onBack = {}
                )
            }
        }
        compose.onNodeWithTag("question_input").performTextInput("Pizza or burger tonight?")
        compose.onNodeWithTag("red_input").performTextInput("Pizza")
        compose.onNodeWithTag("blue_input").performTextInput("Burger")
        compose.onNodeWithTag(visibility).performScrollTo().performClick()
        compose.onNodeWithTag("submit").performClick()
    }

    @Test
    fun aPublicPollAsksForTheTermsOnceThenIsCreated() {
        draft("visibility_public")
        assertEquals(emptyList<Visibility>(), created)
        compose.onNodeWithTag("accept_terms").performClick()
        compose.waitForIdle()
        assertEquals(true, accepted)
        assertEquals(listOf(Visibility.PUBLIC), created)
        compose.onNodeWithTag("accept_terms").assertDoesNotExist()
    }

    @Test
    fun aPrivatePollAsksNothing() {
        draft("visibility_private")
        compose.onNodeWithTag("accept_terms").assertDoesNotExist()
        assertEquals(listOf(Visibility.PRIVATE), created)
        assertEquals(false, accepted)
    }
}
