package com.jorgelillo.core.designsystem

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.core.screenshottesting.captureScreenWithSheet
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The About sheet over a page, with an app's widget card under its text (the tallest it gets):
 * upright, and on short landscape screens (a phone turned sideways, a 16:9 handheld) at the top
 * and scrolled to its last row.
 */
@RunWith(org.robolectric.RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7, application = Application::class)
class AboutSheetScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun about(name: String, scrolledToEnd: Boolean = false) = compose.captureScreenWithSheet(name, {
        if (scrolledToEnd) onNodeWithTag("privacy").performScrollTo()
    }) {
        CompositionLocalProvider(LocalLilloThemeMode provides remember { mutableStateOf(LilloThemeMode.System) }) {
            LilloTheme(lightColorScheme()) {
                LilloPage(title = "App", onBack = null) {}
                LilloAboutSheet(
                    icon = ColorPainter(Color(0xFF1B3A5C)),
                    iconBackground = Color(0xFF1B3A5C),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    texts = AboutTexts(
                        title = "Busy day? Metropolitano",
                        version = "Version 1.3",
                        body = "Tells the neighbours whether tomorrow there is a match, a concert or a final, " +
                            "with a reminder the evening before. No ads, no account, no location.",
                        rate = "Rate on Google Play",
                        privacy = "Privacy policy"
                    ),
                    privacyUrl = "https://example.org/privacy",
                    playStoreUrl = "https://play.google.com/store/apps/details?id=x",
                    onDismiss = {},
                    extra = { LilloWidgetCard("App", pinnable = true, onAdd = {}) }
                )
            }
        }
    }

    @Test fun portrait() = about("about_sheet")

    @Test
    @Config(qualifiers = "+land")
    fun landscape() = about("about_sheet_landscape")

    @Test
    @Config(qualifiers = "+land")
    fun landscapeScrolledToTheEnd() = about("about_sheet_landscape_end", scrolledToEnd = true)

    /** A 16:9 handheld turned sideways (1920×1080 at 400 dpi). */
    @Test
    @Config(qualifiers = "w768dp-h432dp-land")
    fun handheldScrolledToTheEnd() = about("about_sheet_handheld_end", scrolledToEnd = true)
}
