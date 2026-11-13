package com.jorgelillo.leaguecards.ui

import android.app.Application
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.leaguecards.domain.League
import com.jorgelillo.leaguecards.domain.Saved
import com.jorgelillo.leaguecards.ui.theme.ChatColorScheme
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** The trophies screen shows an ending only once it has been seen. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = Application::class)
class TrophiesScreenTest {
    @get:Rule
    val compose = createComposeRule()

    private val league = League.parse(File("src/main/assets/league.json").readText())

    @Test
    fun anEndingStaysLockedUntilSeen() {
        val (seen, unseen) = league.endings.keys.toList().let { it[0] to it[1] }
        compose.setContent {
            LilloTheme(ChatColorScheme) { TrophiesScreen(league, Saved(endingsSeen = setOf(seen))) {} }
        }
        compose.onNode(hasTestTag("ending_$seen") and hasAnyDescendant(hasText("🔒"))).assertDoesNotExist()
        compose.onNode(hasTestTag("ending_$unseen") and hasAnyDescendant(hasText("🔒"))).assertExists()
        compose.onNode(hasTestTag("ending_$unseen") and hasAnyDescendant(hasText("???"))).assertExists()
    }
}
