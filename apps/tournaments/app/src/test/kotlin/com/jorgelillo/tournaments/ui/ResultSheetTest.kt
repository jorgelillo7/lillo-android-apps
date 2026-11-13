package com.jorgelillo.tournaments.ui

import android.app.Application
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.tournaments.domain.Format
import com.jorgelillo.tournaments.domain.Tournament
import com.jorgelillo.tournaments.domain.Tournaments
import com.jorgelillo.tournaments.ui.theme.ArenaDark
import com.jorgelillo.tournaments.ui.tournament.ResultSheet
import kotlin.random.Random
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** The result sheet offers only the scores a best-of length allows. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = Application::class)
class ResultSheetTest {
    @get:Rule
    val compose = createComposeRule()

    private fun open(t: Tournament) {
        compose.setContent {
            LilloTheme(ArenaDark) { ResultSheet(t, Tournaments.pending(t).first(), { _, _, _, _ -> }, {}, {}) }
        }
    }

    private fun knockout(bestOf: Int) =
        Tournaments.create("t", "Cup", 0, listOf("Ann", "Bob"), Format.ELIMINATION, bestOf = bestOf, random = Random(1))

    /** Each chip is tagged with the loser's games: `score_k` reads [labels]`[k]`, and no chip follows. */
    private fun assertScores(vararg labels: String) {
        labels.forEachIndexed { k, label ->
            compose.onNodeWithTag("score_$k").assertTextEquals(label)
        }
        compose.onNodeWithTag("score_${labels.size}").assertDoesNotExist()
    }

    @Test
    fun theScoresOfferedFollowTheBestOf() {
        open(knockout(5))
        compose.onNodeWithTag("winner_a").performClick()
        assertScores("3 - 0", "3 - 1", "3 - 2")
        compose.onNodeWithTag("draw").assertDoesNotExist()
    }

    @Test
    fun bestOfThreeOffersTwoScores() {
        open(knockout(3))
        compose.onNodeWithTag("winner_b").performClick()
        assertScores("2 - 0", "2 - 1")
    }

    @Test
    fun bestOfOneHasNoScoreToPick() {
        open(knockout(1))
        compose.onNodeWithTag("winner_a").performClick()
        compose.onNodeWithTag("score_0").assertDoesNotExist()
    }

    @Test
    fun aSwissDrawOffersLevelScoresOnly() {
        val league = Tournaments.create(
            "s",
            "League",
            0,
            listOf("Ann", "Bob", "Cat", "Dan"),
            Format.SWISS,
            bestOf = 3,
            random = Random(1)
        )
        open(league)
        compose.onNodeWithTag("draw").performClick()
        assertScores("0 - 0", "1 - 1")
    }
}
