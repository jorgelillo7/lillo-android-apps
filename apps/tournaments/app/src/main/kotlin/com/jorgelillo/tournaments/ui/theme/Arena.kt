package com.jorgelillo.tournaments.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** "Arena" palette: deep blue night, cream text, gold for winners (the victory line). */
object Arena {
    val Ink = Color(0xFF12202D)
    val InkTop = Color(0xFF1E3A52)
    val Raised = Color(0xFF1B2E3E)
    val High = Color(0xFF25405A)
    val Cream = Color(0xFFF4F1EA)
    val Muted = Color(0xFFA9B6C2)
    val Faint = Color(0x33F4F1EA)

    val Gold = Color(0xFFFFC83D)
    val Teal = Color(0xFF3DDC97)
    val Coral = Color(0xFFFF6B6B)

    /** Gold for text: a little softer, so it doesn't vibrate on the dark background. */
    val GoldText = Color(0xFFF5CF6E)

    /** Teal for text: a little softer, so it doesn't vibrate on the dark background. */
    val TealText = Color(0xFF6CE4B0)

    /** Coral for text: a little softer, so it doesn't vibrate on the dark background. */
    val CoralText = Color(0xFFEA8080)
    val Blue = Color(0xFF6C8EF5)

    val Background = Brush.verticalGradient(listOf(InkTop, Ink))

    /** The home summary's gradient: electric blue into emerald, white text on top. */
    val Hero = listOf(Color(0xFF2B3FD8), Color(0xFF00695C))
    val Confetti = listOf(Gold, Teal, Coral, Blue, Cream)
}

val ArenaColorScheme = darkColorScheme(
    primary = Arena.Gold,
    onPrimary = Arena.Ink,
    secondary = Arena.Teal,
    onSecondary = Arena.Ink,
    tertiary = Arena.Coral,
    background = Arena.Ink,
    onBackground = Arena.Cream,
    surface = Arena.Ink,
    onSurface = Arena.Cream,
    onSurfaceVariant = Arena.Muted,
    surfaceContainerLow = Arena.Raised,
    surfaceContainer = Arena.Raised,
    surfaceContainerHigh = Arena.High,
    surfaceContainerHighest = Arena.High,
    secondaryContainer = Arena.High,
    onSecondaryContainer = Arena.Cream,
    outline = Arena.Faint,
    outlineVariant = Arena.Faint
)
