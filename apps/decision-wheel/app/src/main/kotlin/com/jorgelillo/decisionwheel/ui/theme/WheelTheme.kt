package com.jorgelillo.decisionwheel.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.jorgelillo.core.designsystem.LilloPalette
import com.jorgelillo.core.designsystem.LilloStyle

/**
 * "Sunset": the wheel's six warm-to-cool segments are the only rainbow; the app around it is warm
 * paper by day and plum wine by night, raspberry or sun yellow for what you act on.
 */
object WheelColors {
    /** Ink and cream: the wheel's own hub, pointer and segment labels, the same in both looks. */
    val Ink = Color(0xFF1E0A1B)
    val Cream = Color(0xFFFBEAF2)

    val Coral = Color(0xFFFF6B6B)
    val Orange = Color(0xFFFFB347)
    val Sun = Color(0xFFFFD93D)
    val Teal = Color(0xFF4ECDC4)
    val Blue = Color(0xFF6C8EF5)
    val Lilac = Color(0xFFC792EA)

    /** Order matters: neighbours in this list contrast well. */
    val Segments = listOf(Coral, Orange, Sun, Teal, Blue, Lilac)

    /** Dark ink on light accents, cream on darker ones. */
    fun onSegment(color: Color): Color = if (color.luminance() > 0.45f) Ink else Cream

    /** The launcher icon's background, behind the icon in the About sheet. */
    val IconBackground = Color(0xFF1F1B2E)
}

private val Raspberry = Color(0xFFC2185B)

/** The home summary's gradient in both looks: raspberry into grape, white text on top. */
private val Hero = listOf(Raspberry, Color(0xFF7B1FA2))

private val Paper = Color(0xFFFFF6EC)
private val PlumInk = Color(0xFF2A1424)

val WheelLight = LilloStyle(
    lightColorScheme(
        primary = Raspberry,
        onPrimary = Color.White,
        secondary = Color(0xFF00796B),
        onSecondary = Color.White,
        tertiary = Color(0xFFD84343),
        error = Color(0xFFB3261E),
        background = Paper,
        onBackground = PlumInk,
        surface = Paper,
        onSurface = PlumInk,
        onSurfaceVariant = Color(0xFF6E5466),
        surfaceContainerLow = Color.White,
        surfaceContainer = Color.White,
        surfaceContainerHigh = Color(0xFFFCEBDD),
        outline = Color(0x332A1424)
    ),
    LilloPalette(
        background = Brush.verticalGradient(listOf(Paper, Color(0xFFFFEBDC))),
        hero = Hero,
        brand = Raspberry,
        onBrand = Color.White,
        accentText = Color(0xFFA3124B)
    )
)

private val Wine = Color(0xFF1E0A1B)

val WheelDark = LilloStyle(
    darkColorScheme(
        primary = WheelColors.Sun,
        onPrimary = Wine,
        secondary = WheelColors.Teal,
        onSecondary = Wine,
        tertiary = WheelColors.Coral,
        error = Color(0xFFEA8080),
        background = Wine,
        onBackground = WheelColors.Cream,
        surface = Wine,
        onSurface = WheelColors.Cream,
        onSurfaceVariant = Color(0xFFD4B6C8),
        surfaceContainerLow = Color(0xFF2E1429),
        surfaceContainer = Color(0xFF2E1429),
        surfaceContainerHigh = Color(0xFF3D1D37),
        outline = Color(0x55FBEAF2)
    ),
    LilloPalette(
        background = Brush.verticalGradient(listOf(Color(0xFF2A0F25), Wine)),
        hero = Hero,
        brand = WheelColors.Sun,
        onBrand = Wine,
        // Sun as text: a little softer, so it doesn't vibrate on the dark background.
        accentText = Color(0xFFF3D56A)
    )
)
