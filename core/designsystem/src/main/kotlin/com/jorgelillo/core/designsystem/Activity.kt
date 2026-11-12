package com.jorgelillo.core.designsystem

import android.content.res.Configuration
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

/**
 * Call first in `onCreate`, before `super.onCreate`: splash screen and edge-to-edge drawing.
 * [darkDesign] = the app's background is dark, so the system bar icons are light. An app with a
 * light and a dark look passes the one it starts in; [setLilloContent] keeps the bars in step.
 */
fun ComponentActivity.installLilloWindow(darkDesign: Boolean) {
    installSplashScreen()
    lilloSystemBars(darkDesign)
}

private fun ComponentActivity.lilloSystemBars(dark: Boolean) {
    val bars = if (dark) {
        SystemBarStyle.dark(Color.TRANSPARENT)
    } else {
        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    }
    enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
}

/** Whether an app with two looks starts in the dark one: the user's choice, else the phone's. */
fun ComponentActivity.startsDark(): Boolean = when (LilloThemeModes.read(this)) {
    LilloThemeMode.Light -> false

    LilloThemeMode.Dark -> true

    LilloThemeMode.System ->
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}

/**
 * The app's content in its [light] or [dark] look, with test tags exposed for scripts/tap-text.sh.
 * The look follows the phone unless the user picks one in [LilloAboutSheet]; the choice is
 * stored per app and the system bars follow it.
 */
fun ComponentActivity.setLilloContent(light: LilloStyle, dark: LilloStyle, content: @Composable () -> Unit) {
    setContent {
        val mode = remember { mutableStateOf(LilloThemeModes.read(this)) }
        CompositionLocalProvider(LocalLilloThemeMode provides mode) {
            val isDark = lilloIsDark()
            LaunchedEffect(isDark) { lilloSystemBars(isDark) }
            LaunchedEffect(mode) {
                snapshotFlow { mode.value }.collect { LilloThemeModes.write(this@setLilloContent, it) }
            }
            LilloTheme(light, dark) {
                Box(Modifier.fillMaxSize().then(TestTagsAsResourceIds)) { content() }
            }
        }
    }
}

/** The content of an app with a single look, with test tags exposed for scripts/tap-text.sh. */
fun ComponentActivity.setLilloContent(colorScheme: ColorScheme, content: @Composable () -> Unit) {
    setContent {
        LilloTheme(colorScheme = colorScheme) {
            Box(Modifier.fillMaxSize().then(TestTagsAsResourceIds)) { content() }
        }
    }
}
