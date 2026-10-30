package com.jorgelillo.christmascountdown.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

object ChristmasColors {
    val Gold = Color(0xFFFFC857)

    /** Gold for text: a little softer, so it doesn't vibrate on the dark background. */
    val GoldText = Color(0xFFF5CF6E)
    val Pine = Color(0xFF2A9D5C)
    val Berry = Color(0xFFE63946)
    val Snow = Color(0xFFEEE3E2)
    val Wine = Color(0xFF4A0E1C)
    val Midnight = Color(0xFF0B1426)

    /** Background gradient: warm wine at the top fading into a winter night. */
    val Background = listOf(Wine, Color(0xFF2A1020), Midnight)
}

val ChristmasColorScheme = darkColorScheme(
    primary = ChristmasColors.Gold,
    onPrimary = Color(0xFF3A2A00),
    primaryContainer = Color(0xFF5C4300),
    onPrimaryContainer = ChristmasColors.Gold,
    secondary = ChristmasColors.Pine,
    onSecondary = Color.White,
    secondaryContainer = Color(0x332A9D5C),
    onSecondaryContainer = ChristmasColors.Snow,
    tertiary = ChristmasColors.Berry,
    background = ChristmasColors.Midnight,
    surface = Color(0xFF1C1220),
    onSurface = ChristmasColors.Snow,
    onSurfaceVariant = Color(0xFFD9C8C8),
    surfaceContainerLow = Color(0xFF241826),
    outline = Color(0x55FFFFFF)
)
