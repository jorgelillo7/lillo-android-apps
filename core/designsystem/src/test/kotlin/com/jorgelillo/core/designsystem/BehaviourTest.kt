package com.jorgelillo.core.designsystem

import android.app.Application
import android.app.LocaleManager
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Motion, confetti, the About sheet and the language dialog, beyond what the catalogue shows. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = Application::class)
class BehaviourTest {
    @get:Rule
    val compose = createComposeRule()

    private val context: Application = ApplicationProvider.getApplicationContext()

    private fun removeAnimations(on: Boolean) {
        Settings.Global.putFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, if (on) 0f else 1f)
    }

    @Test
    fun animationsFollowThePhonesSetting() {
        var enabled: Boolean? = null
        removeAnimations(on = true)
        compose.setContent { enabled = animationsEnabled() }
        compose.waitForIdle()
        assertEquals(false, enabled)
    }

    @Test
    fun animationsAreOnByDefault() {
        var enabled: Boolean? = null
        removeAnimations(on = false)
        compose.setContent { enabled = animationsEnabled() }
        compose.waitForIdle()
        assertEquals(true, enabled)
    }

    // The clock stands still: otherwise the test waits the rain out and finds nothing left.
    private fun confetti() = compose.apply { mainClock.autoAdvance = false }.setContent {
        Box {
            ConfettiBurst("win", listOf(Color.Red), Modifier.testTag("burst"))
            ConfettiRain(listOf(Color.Red), Modifier.testTag("rain"))
        }
    }

    @Test
    fun noConfettiWithAnimationsRemoved() {
        removeAnimations(on = true)
        confetti()
        compose.onNodeWithTag("burst").assertDoesNotExist()
        compose.onNodeWithTag("rain").assertDoesNotExist()
    }

    @Test
    fun confettiWithAnimationsOn() {
        removeAnimations(on = false)
        confetti()
        compose.onNodeWithTag("burst").assertExists()
        compose.onNodeWithTag("rain").assertExists()
    }

    /** A landscape phone: short, so the actions go beside the content and a title-less page drops its header. */
    @Test
    @Config(qualifiers = "w891dp-h411dp-land")
    fun aShortPageWithoutHeaderKeepsItsActionsInTheSideColumn() {
        compose.setContent {
            LilloPage(
                title = null,
                onBack = null,
                sideActionsWhenShort = true,
                actions = { Text("About", Modifier.testTag("about")) },
                bottom = { Text("Play", Modifier.testTag("play")) }
            ) { Text("Content") }
        }
        compose.onNodeWithTag("about").assertIsDisplayed()
        compose.onNodeWithTag("play").assertIsDisplayed()
    }

    private val opened = mutableListOf<String>()

    private fun about(playStoreUrl: String?, extra: (@Composable () -> Unit)? = null) = compose.setContent {
        MaterialTheme(lightColorScheme()) {
            CompositionLocalProvider(
                LocalUriHandler provides object : UriHandler {
                    override fun openUri(uri: String) {
                        opened += uri
                    }
                },
                LocalLilloThemeMode provides remember { mutableStateOf(LilloThemeMode.System) }
            ) {
                LilloAboutSheet(
                    icon = ColorPainter(Color.Red),
                    iconBackground = Color.White,
                    containerColor = Color.White,
                    texts = AboutTexts("App", "Version 1", "Body", rate = "Rate", privacy = "Privacy"),
                    privacyUrl = "https://example.org/privacy",
                    playStoreUrl = playStoreUrl,
                    onDismiss = {},
                    extra = extra
                )
            }
        }
    }

    @Test
    fun theAboutSheetOpensThePrivacyPolicyAndRatesOnlyOncePublished() {
        about(playStoreUrl = null)
        compose.onNodeWithTag("rate").assertDoesNotExist()
        compose.onNodeWithTag("language").assertExists()
        compose.onNodeWithTag("privacy").performClick()
        assertEquals(listOf("https://example.org/privacy"), opened)
    }

    @Test
    fun aPublishedAppOffersToRateIt() {
        about(playStoreUrl = "https://play.google.com/store/apps/details?id=x")
        compose.onNodeWithTag("rate").performClick()
        assertEquals(listOf("https://play.google.com/store/apps/details?id=x"), opened)
    }

    /**
     * A landscape phone, with an app's extra block (a widget card) under the text: the sheet opens
     * expanded and scrolls, so the look, the language, rating and privacy can all be reached.
     */
    @Test
    @Config(qualifiers = "w891dp-h411dp-land")
    fun everyAboutRowIsReachableOnAShortScreen() {
        about(playStoreUrl = "https://play.google.com/store/apps/details?id=x") {
            Box(Modifier.fillMaxWidth().height(240.dp).testTag("extra"))
        }
        listOf("theme_dark", "language", "rate", "privacy").forEach { tag ->
            compose.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
        }
        compose.onNodeWithTag("theme_dark").assertHeightIsAtLeast(40.dp)
        compose.onNodeWithTag("privacy").performClick()
        assertEquals(listOf("https://example.org/privacy"), opened)
    }

    @Test
    fun theLanguageDialogSetsTheAppsLanguageOrGoesBackToThePhones() {
        val locales = context.getSystemService(LocaleManager::class.java)
        var open by mutableStateOf(true)
        compose.setContent { if (open) LilloLanguageDialog(onDismiss = { open = false }) }
        compose.onNodeWithTag("language_es").performClick()
        assertEquals("es", locales.applicationLocales.toLanguageTags())
        assertEquals(false, open)

        open = true
        compose.onNodeWithTag("language_system").performClick()
        assertEquals("", locales.applicationLocales.toLanguageTags())
    }
}
