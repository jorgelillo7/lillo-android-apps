package com.jorgelillo.whoslying.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** "Neon" palette: midnight blue, violet actions, turquoise for the word, pink for impostors. */
object Neon {
    val Night = Color(0xFF100E20)
    val Card = Color(0xFF1E1A3A)
    val CardHigh = Color(0xFF2A2550)
    val Ink = Color(0xFFF0EEFF)
    val Muted = Color(0xFF9692BE)
    val Violet = Color(0xFF7C5CFF)
    val Turquoise = Color(0xFF00E0B8)
    val Pink = Color(0xFFFF4D94)
    val Amber = Color(0xFFFFC94D)

    val Backdrop = Brush.verticalGradient(listOf(Color(0xFF2B1B6B), Night, Night))
    val RevealBackdrop = Brush.verticalGradient(listOf(Color(0xFF7C5CFF), Color(0xFF4B2BD6)))

    // Result screens: who won sets the mood.
    val CiviliansBackdrop = Brush.verticalGradient(listOf(Color(0xFF00B4FF), Color(0xFF2346E8)))
    val ImpostorsBackdrop = Brush.verticalGradient(listOf(Color(0xFFFF8A3D), Color(0xFFD7263D)))
    val DrifterBackdrop = Brush.verticalGradient(listOf(Color(0xFFFFB13D), Color(0xFFE0367A)))
}

val NeonColorScheme = darkColorScheme(
    primary = Neon.Violet,
    onPrimary = Color.White,
    secondary = Neon.Turquoise,
    onSecondary = Neon.Night,
    tertiary = Neon.Pink,
    background = Neon.Night,
    onBackground = Neon.Ink,
    surface = Neon.Night,
    onSurface = Neon.Ink,
    onSurfaceVariant = Neon.Muted,
    surfaceContainerLow = Neon.Card,
    surfaceContainer = Neon.Card,
    surfaceContainerHigh = Neon.CardHigh,
    surfaceContainerHighest = Neon.CardHigh,
    outline = Color(0x559692BE),
    error = Neon.Pink
)
