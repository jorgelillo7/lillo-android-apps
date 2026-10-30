package com.jorgelillo.tournaments.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * "Arena" palette (apps/tournaments/DESIGN.md): a night arena from the seed #2B3FD8 (Material 3
 * Vibrant, see docs/design.md), cream text, gold only for what is won, lilac for what is going on.
 */
object Arena {
    val Ink = Color(0xFF0C072E)
    val InkTop = Color(0xFF1E184A)
    val Raised = Color(0xFF181241)
    val High = Color(0xFF241D54)
    val Cream = Color(0xFFE7E2FF)
    val Muted = Color(0xFFABA6D5)
    val Faint = Color(0x33E7E2FF)

    /** Brand fill: the primary button, the champion, finished tournaments, the winners' path. */
    val Gold = Color(0xFFFFC83D)

    /** The harmonised accent: in progress, the selected tab, "tap to score". */
    val Accent = Color(0xFF9DA7FF)
    val Coral = Color(0xFFFF6B6B)

    // As text, softer tones: saturated colours vibrate as small text on a dark background.
    val GoldText = Color(0xFFF5CF6E)
    val AccentText = Color(0xFF9DA7FF)
    val CoralText = Color(0xFFEA8080)

    val Background = Brush.verticalGradient(listOf(InkTop, Ink))

    /** The home summary's gradient: electric blue into violet, white text on top. */
    val Hero = listOf(Color(0xFF2B3FD8), Color(0xFF7B3FE4))
    val Confetti = listOf(Gold, Accent, Coral, Cream)
}

val ArenaColorScheme = darkColorScheme(
    primary = Arena.Gold,
    onPrimary = Arena.Ink,
    secondary = Arena.Accent,
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
