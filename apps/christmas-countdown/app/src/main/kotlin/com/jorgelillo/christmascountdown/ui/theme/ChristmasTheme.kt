package com.jorgelillo.christmascountdown.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.jorgelillo.core.designsystem.LilloPalette
import com.jorgelillo.core.designsystem.LilloStyle

/**
 * "Christmas lights": a snowy morning by day (warm snow fading into a cold blue), the wine-to-midnight
 * sky by night. Berry red is what you act on by day, gold by night; pine is a door already opened.
 */
object ChristmasColors {
    val Gold = Color(0xFFFFC857)
    val Pine = Color(0xFF2A9D5C)
    val Berry = Color(0xFFE63946)

    /** Openable doors in both looks: berry into deep red, white numbers on top. */
    val DoorOpenable = listOf(Berry, Color(0xFF9B1B30))
}

/**
 * The app's colours beyond Material's roles, for its own pieces (glass cards, doors, snow): one set
 * per look, chosen by the background the theme provides.
 */
data class ChristmasLook(
    val glass: Color,
    val glassBorder: Color,
    val snow: Color,
    val navigationBar: Color,
    val doorOpened: List<Color>,
    val doorOpenedBorder: Color,
    val doorLocked: List<Color>,
    val doorLockedBorder: Color,
    val doorLockedText: Color,
    val doorLockIcon: Color
) {
    companion object {
        val current: ChristmasLook
            @Composable
            @ReadOnlyComposable
            get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Dark else Light

        private val Light = ChristmasLook(
            glass = Color(0xC7FFFFFF),
            glassBorder = Color(0x262B1418),
            snow = Color(0xFF8FA6BF),
            navigationBar = Color(0xB3FFFFFF),
            doorOpened = listOf(Color(0x2E1E7A4A), Color(0x1A1E7A4A)),
            doorOpenedBorder = Color(0xFF1E7A4A),
            doorLocked = listOf(Color(0xFFFFFFFF), Color(0xFFF6EEEC)),
            doorLockedBorder = Color(0x332B1418),
            doorLockedText = Color(0xFF6E5A61),
            doorLockIcon = Color(0x806E5A61)
        )

        private val Dark = ChristmasLook(
            glass = Color(0x1AFFFFFF),
            glassBorder = Color(0x2EFFFFFF),
            snow = Color.White,
            navigationBar = Color(0x40000000),
            doorOpened = listOf(Color(0x332A9D5C), Color(0x222A9D5C)),
            doorOpenedBorder = ChristmasColors.Pine,
            doorLocked = listOf(Color(0x14FFFFFF), Color(0x0AFFFFFF)),
            doorLockedBorder = Color(0x22FFFFFF),
            doorLockedText = Color(0x88FFFFFF),
            doorLockIcon = Color(0x55FFFFFF)
        )
    }
}

private val SnowTop = Color(0xFFFFF7F0)
private val Ink = Color(0xFF2B1418)
private val DeepBerry = Color(0xFFB5162F)

val ChristmasLight = LilloStyle(
    lightColorScheme(
        primary = DeepBerry,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDADC),
        onPrimaryContainer = Color(0xFF5C0011),
        secondary = Color(0xFF1E7A4A),
        onSecondary = Color.White,
        secondaryContainer = Color(0x331E7A4A),
        onSecondaryContainer = Ink,
        tertiary = ChristmasColors.Berry,
        background = SnowTop,
        onBackground = Ink,
        surface = SnowTop,
        onSurface = Ink,
        onSurfaceVariant = Color(0xFF6B4F55),
        surfaceContainerLow = Color.White,
        surfaceContainer = Color.White,
        surfaceContainerHigh = Color(0xFFF8EDE9),
        outline = Color(0x4D2B1418)
    ),
    LilloPalette(
        background = Brush.verticalGradient(listOf(SnowTop, Color(0xFFFBF1EE), Color(0xFFEAF1F6))),
        hero = ChristmasColors.DoorOpenable,
        brand = DeepBerry,
        onBrand = Color.White,
        accentText = Color(0xFFA3122B)
    )
)

private val Wine = Color(0xFF4A0E1C)
private val Midnight = Color(0xFF0B1426)
private val Snow = Color(0xFFEEE3E2)

val ChristmasDark = LilloStyle(
    darkColorScheme(
        primary = ChristmasColors.Gold,
        onPrimary = Color(0xFF3A2A00),
        primaryContainer = Color(0xFF5C4300),
        onPrimaryContainer = ChristmasColors.Gold,
        secondary = ChristmasColors.Pine,
        onSecondary = Color.White,
        secondaryContainer = Color(0x332A9D5C),
        onSecondaryContainer = Snow,
        tertiary = ChristmasColors.Berry,
        background = Midnight,
        onBackground = Snow,
        surface = Color(0xFF1C1220),
        onSurface = Snow,
        onSurfaceVariant = Color(0xFFD9C8C8),
        surfaceContainerLow = Color(0xFF241826),
        surfaceContainer = Color(0xFF241826),
        surfaceContainerHigh = Color(0xFF2E2030),
        outline = Color(0x55FFFFFF)
    ),
    LilloPalette(
        // Warm wine at the top fading into a winter night.
        background = Brush.verticalGradient(listOf(Wine, Color(0xFF2A1020), Midnight)),
        hero = ChristmasColors.DoorOpenable,
        brand = ChristmasColors.Gold,
        onBrand = Color(0xFF3A2A00),
        // Gold as text: a little softer, so it doesn't vibrate on the dark background.
        accentText = Color(0xFFF5CF6E)
    )
)
