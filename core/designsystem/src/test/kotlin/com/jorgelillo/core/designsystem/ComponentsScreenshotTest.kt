package com.jorgelillo.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.jorgelillo.core.screenshottesting.captureScreenshot
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** What the core components look like; `./gradlew recordRoborazziDebug` after a deliberate change. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class ComponentsScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val night = Color(0xFF14102B)
    private val card = Color(0xFF2A2350)
    private val muted = Color(0xFFB7B0D9)

    private fun shot(name: String, content: @Composable () -> Unit) = compose.captureScreenshot(name) { LilloTheme { content() } }

    @Test
    fun avatars() = shot("avatars") {
        Row(Modifier.background(night).padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LilloAvatar("Ana", Color(0xFFFF4D8D), 56.dp)
            LilloAvatar("álvaro", Color(0xFF00D2B4), 56.dp, ring = Color(0xFFFFC83D))
            LilloAvatar("Bea", Color(0xFF7C5CFF), 56.dp, style = AvatarStyle.Tinted)
            LilloAvatar("🦁 Leones", Color(0xFFFFC83D), 56.dp, style = AvatarStyle.Tinted)
            LilloAvatar("👨‍🍳 Chef", Color(0xFF3DDC97), 56.dp)
        }
    }

    @Test
    fun settingRowsAndSectionTitle() = shot("setting_rows") {
        // Inside an app's LilloPage, which provides the content colour.
        CompositionLocalProvider(LocalContentColor provides Color.White) { SettingRows() }
    }

    @Composable
    private fun SettingRows() {
        Column(Modifier.background(night).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LilloSectionTitle("Game", muted, uppercase = true)
            LilloSettingRow("Language", card, subtitle = "English", subtitleColor = muted, leading = "🌐", onClick = {}) {
                Text("›", color = muted)
            }
            LilloSettingRow("Chaos", card, subtitle = "Some games get a random number of impostors", subtitleColor = muted) {
                Switch(checked = true, onCheckedChange = {})
            }
            LilloSettingRow("Rename", card, subtitle = "Ana", subtitleColor = muted, borderColor = muted.copy(alpha = 0.3f), onClick = {})
        }
    }

    @Composable
    private fun SamplePage(sideActions: Boolean) = LilloPage(
        title = null,
        onBack = null,
        background = Brush.verticalGradient(listOf(Color(0xFF00B4FF), Color(0xFF2448E0))),
        contentColor = Color.White,
        sideActionsWhenShort = sideActions,
        bottom = { LilloBigButton("Continue", {}, Color(0xFF00D26A), Color.Black) }
    ) {
        Text("You caught the impostor!", color = Color.White)
        repeat(6) { Text("Content line ${it + 1}", color = Color.White, modifier = Modifier.padding(top = 8.dp)) }
    }

    @Test
    fun pagePortrait() = shot("page_portrait") { SamplePage(sideActions = true) }

    /** Short screen (landscape phone, handheld): the actions move beside the content. */
    @Test
    @Config(qualifiers = "+land")
    fun pageLandscapeSideActions() = shot("page_landscape_side_actions") { SamplePage(sideActions = true) }

    /** Without the opt-in the bar stays below, as before. */
    @Test
    @Config(qualifiers = "+land")
    fun pageLandscapeDefault() = shot("page_landscape_default") { SamplePage(sideActions = false) }
}
