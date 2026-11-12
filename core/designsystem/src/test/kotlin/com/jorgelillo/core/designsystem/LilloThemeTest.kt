package com.jorgelillo.core.designsystem

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = Application::class)
class LilloThemeTest {
    @get:Rule
    val compose = createComposeRule()

    private val light = LilloStyle(lightColorScheme(primary = Color.Red))
    private val dark = LilloStyle(darkColorScheme(primary = Color.Blue))

    @Test
    fun theChosenLookWinsOverThePhone() {
        val mode = mutableStateOf(LilloThemeMode.Dark)
        compose.setContent {
            CompositionLocalProvider(LocalLilloThemeMode provides mode) {
                LilloTheme(light, dark) {
                    val name = if (MaterialTheme.colorScheme.primary == Color.Blue) "dark" else "light"
                    Text(name, Modifier.testTag("look"))
                }
            }
        }
        compose.onNodeWithTag("look").assertTextEquals("dark")
        mode.value = LilloThemeMode.Light
        compose.onNodeWithTag("look").assertTextEquals("light")
    }

    @Test
    fun withNoChoiceTheLookFollowsThePhone() {
        // Robolectric's default configuration is not in night mode.
        compose.setContent {
            LilloTheme(light, dark) {
                Text(if (lilloIsDark()) "dark" else "light", Modifier.testTag("look"))
            }
        }
        compose.onNodeWithTag("look").assertTextEquals("light")
    }

    @Test
    fun theChoiceIsStoredAndReadBack() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        assertEquals(LilloThemeMode.System, LilloThemeModes.read(context))
        LilloThemeModes.write(context, LilloThemeMode.Dark)
        assertEquals(LilloThemeMode.Dark, LilloThemeModes.read(context))
    }

    @Test
    fun aSchemeAloneGivesAPaletteFromItsRoles() {
        val palette = LilloPalette.from(lightColorScheme(primary = Color.Red, onPrimary = Color.White))
        assertEquals(Color.Red, palette.brand)
        assertEquals(Color.White, palette.onBrand)
    }
}
