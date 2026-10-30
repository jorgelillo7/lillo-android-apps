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
    val Violet = Color(0xFF7756FA)

    /** The home summary's gradient: electric violet into raspberry, white text on top. */
    val Hero = listOf(Color(0xFF5B2EE6), Color(0xFFC2185B))
    val Turquoise = Color(0xFF00E0B8)
    val Pink = Color(0xFFFF4D94)
    val Amber = Color(0xFFFFC94D)

    // Lighter tones for text on coloured backdrops and cards, where the base ones fall below 4.5:1.
    val PinkText = Color(0xFFFFA3C8)
    val AmberText = Color(0xFFFFE29A)
    val VioletLight = Color(0xFF8B6EFF)

    /** "Go" actions: hide the card, vote, next round. */
    val Go = Color(0xFF00D26A)

    /** Confetti and fireworks. */
    val Confetti = listOf(Amber, Pink, Turquoise, Color.White, Color(0xFF7CFF6B), Violet)

    val Backdrop = Brush.verticalGradient(listOf(Color(0xFF2B1B6B), Night, Night))
    val RevealBackdrop = Brush.verticalGradient(listOf(Color(0xFF6A4AF0), Color(0xFF4B2BD6)))

    // Result screens: who won sets the mood. The light end of each gradient keeps white text at
    // 4.5:1 or more (WCAG AA), checked by the accessibility checks in the screenshot tests.
    val CiviliansBackdrop = Brush.verticalGradient(listOf(Color(0xFF0A72D6), Color(0xFF2346E8)))
    val ImpostorsBackdrop = Brush.verticalGradient(listOf(Color(0xFFC2410C), Color(0xFFC81E3A)))
    val DrifterBackdrop = Brush.verticalGradient(listOf(Color(0xFFB45309), Color(0xFFBE185D)))
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
