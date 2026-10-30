package com.jorgelillo.decisionwheel.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** "Sunset" palette: deep night background, six warm-to-cool accents of similar intensity. */
object WheelColors {
    val Night = Color(0xFF1F1B2E)
    val NightRaised = Color(0xFF2A2540)
    val NightHigh = Color(0xFF363052)
    val Cream = Color(0xFFF7F3EE)
    val Muted = Color(0xFFB9B2C9)

    val Coral = Color(0xFFFF6B6B)
    val Orange = Color(0xFFFFB347)
    val Sun = Color(0xFFFFD93D)

    /** Sun for text: a little softer, so it doesn't vibrate on the dark background. */
    val SunText = Color(0xFFF3D56A)

    /** Coral for text: a little softer, so it doesn't vibrate on the dark background. */
    val CoralText = Color(0xFFEA8080)
    val Teal = Color(0xFF4ECDC4)
    val Blue = Color(0xFF6C8EF5)
    val Lilac = Color(0xFFC792EA)

    /** The home summary's gradient: raspberry into deep violet, white text on top. */
    val Hero = listOf(Color(0xFFC2185B), Color(0xFF4527A0))

    /** Order matters: neighbours in this list contrast well. */
    val Segments = listOf(Coral, Orange, Sun, Teal, Blue, Lilac)

    /** Dark ink on light accents, cream on darker ones. */
    fun onSegment(color: Color): Color = if (color.luminance() > 0.45f) Night else Cream
}

val WheelColorScheme = darkColorScheme(
    primary = WheelColors.Sun,
    onPrimary = WheelColors.Night,
    secondary = WheelColors.Teal,
    onSecondary = WheelColors.Night,
    tertiary = WheelColors.Coral,
    background = WheelColors.Night,
    onBackground = WheelColors.Cream,
    surface = WheelColors.Night,
    onSurface = WheelColors.Cream,
    onSurfaceVariant = WheelColors.Muted,
    surfaceContainerLow = WheelColors.NightRaised,
    surfaceContainer = WheelColors.NightRaised,
    surfaceContainerHigh = WheelColors.NightHigh,
    outline = Color(0x55F7F3EE)
)
