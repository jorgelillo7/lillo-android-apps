package com.jorgelillo.leaguecards.ui

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.leaguecards.domain.Game
import com.jorgelillo.leaguecards.domain.League
import com.jorgelillo.leaguecards.domain.Saved
import com.jorgelillo.leaguecards.ui.theme.ChatDark
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** The rules are open for a new player and folded for a returning one, until asked for. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = Application::class)
class HomeRulesTest {
    @get:Rule
    val compose = createComposeRule()

    private val league = League.parse(File("src/main/assets/league.json").readText())

    private fun home(saved: Saved) = compose.setContent {
        LilloTheme(ChatDark) { HomeScreen(league, saved, onContinue = {}, onNew = {}, onTrophies = {}) }
    }

    @Test
    fun aNewPlayerSeesTheRules() {
        home(Saved())
        compose.onNodeWithTag("how_to_play").assertExists()
        compose.onNodeWithTag("show_rules").assertDoesNotExist()
    }

    @Test
    fun aReturningPlayerOpensTheRulesWhenAsked() {
        home(Saved().with(league, Game.newSeason(league, seed = 7), today = 0))
        compose.onNodeWithTag("how_to_play").assertDoesNotExist()
        compose.onNodeWithTag("show_rules").performScrollTo().performClick()
        compose.onNodeWithTag("how_to_play").assertExists()
    }
}
