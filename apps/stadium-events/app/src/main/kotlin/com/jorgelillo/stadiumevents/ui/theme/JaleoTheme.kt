package com.jorgelillo.stadiumevents.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Light by default (dark text on cream reads best for the 50–70 year olds the app is for); the dark
 * variant only when the phone is in dark mode. The answer cards keep dark text in both.
 */
@Immutable
data class JaleoPalette(
    val background: Color,
    val surface: Color,
    val text: Color,
    val secondaryText: Color,
    val accent: Color,
    val busy: Color,
    val max: Color,
    val calm: Color,
    val onCard: Color = Ink
)

val Ink = Color(0xFF1E1B18)

val LightPalette = JaleoPalette(
    background = Color(0xFFFBF7F1),
    surface = Color(0xFFFFFFFF),
    text = Ink,
    secondaryText = Color(0xFF3F3A35),
    accent = Color(0xFF2F5D8A),
    busy = Color(0xFFF2B544),
    max = Color(0xFFE8705F),
    calm = Color(0xFF8CC7A4)
)

val DarkPalette = JaleoPalette(
    background = Color(0xFF141B2D),
    surface = Color(0xFF1F2940),
    text = Color(0xFFF4F1EA),
    secondaryText = Color(0xFFD3D8E2),
    accent = Color(0xFF8FB8F2),
    busy = Color(0xFFF6C453),
    max = Color(0xFFF08A7E),
    calm = Color(0xFF93D3AE)
)

val JaleoLightScheme = lightColorScheme(
    primary = LightPalette.accent,
    onPrimary = Color.White,
    background = LightPalette.background,
    onBackground = LightPalette.text,
    surface = LightPalette.surface,
    onSurface = LightPalette.text,
    onSurfaceVariant = LightPalette.secondaryText,
    surfaceContainerLow = LightPalette.surface,
    surfaceContainer = LightPalette.surface
)

val JaleoDarkScheme = darkColorScheme(
    primary = DarkPalette.accent,
    onPrimary = DarkPalette.background,
    background = DarkPalette.background,
    onBackground = DarkPalette.text,
    surface = DarkPalette.surface,
    onSurface = DarkPalette.text,
    onSurfaceVariant = DarkPalette.secondaryText,
    surfaceContainerLow = DarkPalette.surface,
    surfaceContainer = DarkPalette.surface
)
