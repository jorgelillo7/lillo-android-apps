package com.jorgelillo.stadiumevents.ui

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jorgelillo.core.designsystem.LilloTheme
import com.jorgelillo.core.designsystem.LilloThemeMode
import com.jorgelillo.core.designsystem.LocalLilloThemeMode
import com.jorgelillo.stadiumevents.ui.theme.DarkPalette
import com.jorgelillo.stadiumevents.ui.theme.JaleoDark
import com.jorgelillo.stadiumevents.ui.theme.JaleoLight
import com.jorgelillo.stadiumevents.ui.theme.JaleoPalette
import com.jorgelillo.stadiumevents.ui.theme.LightPalette
import com.jorgelillo.stadiumevents.ui.theme.jaleoPalette
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** The look picked in About wins over the phone's mode, for the answer colours too. */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36], application = Application::class, qualifiers = "notnight")
class JaleoThemeTest {
    @get:Rule
    val compose = createComposeRule()

    private fun paletteWith(mode: LilloThemeMode): JaleoPalette? {
        var palette: JaleoPalette? = null
        val chosen = mutableStateOf(mode)
        compose.setContent {
            CompositionLocalProvider(LocalLilloThemeMode provides chosen) {
                LilloTheme(JaleoLight, JaleoDark) { palette = jaleoPalette() }
            }
        }
        compose.waitForIdle()
        return palette
    }

    @Test
    fun theChosenDarkLookWinsOverALightPhone() = assertEquals(DarkPalette, paletteWith(LilloThemeMode.Dark))

    @Test
    fun aLightPhoneShowsTheLightLookByDefault() = assertEquals(LightPalette, paletteWith(LilloThemeMode.System))
}
