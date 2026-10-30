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
    val Blue = Color(0xFF6C8EF5)

    val Background = Brush.verticalGradient(listOf(InkTop, Ink))

    /** The home summary's gradient: night blue into pitch green, white text on top. */
    val Hero = listOf(Color(0xFF3346A8), Color(0xFF166558))
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
