package com.jorgelillo.tournaments.ui.theme

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
 * "Arena" (apps/tournaments/DESIGN.md): a trophy room by day (ivory, navy ink) and a floodlit
 * stadium by night (deep navy); gold only for what is won, blue for what is going on. Each token
 * follows the look in effect, picked by the theme's background.
 */
object Arena {
    private val look: ArenaLook
        @Composable
        @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) ArenaLook.Dark else ArenaLook.Light

    val Ink: Color
        @Composable @ReadOnlyComposable
        get() = look.ink
    val Raised: Color
        @Composable @ReadOnlyComposable
        get() = look.raised
    val High: Color
        @Composable @ReadOnlyComposable
        get() = look.high

    /** Body text. */
    val Cream: Color
        @Composable @ReadOnlyComposable
        get() = look.text
    val Muted: Color
        @Composable @ReadOnlyComposable
        get() = look.muted

    /** Dividers and bracket lines not won. */
    val Faint: Color
        @Composable @ReadOnlyComposable
        get() = look.faint

    /** Brand fill: the primary button, the champion, finished tournaments, the winners' path. */
    val Gold: Color
        @Composable @ReadOnlyComposable
        get() = look.gold

    /** Text on [Gold]. */
    val OnGold: Color
        @Composable @ReadOnlyComposable
        get() = look.onGold

    /** In progress, the selected tab, "tap to score". */
    val Accent: Color
        @Composable @ReadOnlyComposable
        get() = look.accent

    // As text, tones that keep 4.5:1 on this look's cards.
    val GoldText: Color
        @Composable @ReadOnlyComposable
        get() = look.goldText
    val AccentText: Color
        @Composable @ReadOnlyComposable
        get() = look.accent
    val CoralText: Color
        @Composable @ReadOnlyComposable
        get() = look.coralText

    val Coral = Color(0xFFFF6B6B)

    /** The home summary's gradient in both looks: electric blue into violet, white text on top. */
    val Hero = listOf(Color(0xFF2B3FD8), Color(0xFF7B3FE4))
    val Confetti = listOf(Color(0xFFFFC83D), Color(0xFF9DB0FF), Coral, Color(0xFFE4E8FF))
}

/** The exported bracket image's colours: the night look, whatever the phone's setting. */
object ArenaNight {
    val Ink = Color(0xFF0A1024)
    val InkTop = Color(0xFF18213F)
    val Raised = Color(0xFF141C38)
    val Cream = Color(0xFFE4E8FF)
    val Muted = Color(0xFFA6AECF)
    val Faint = Color(0x33E4E8FF)
    val Gold = Color(0xFFFFC83D)
}

private data class ArenaLook(
    val ink: Color,
    val raised: Color,
    val high: Color,
    val text: Color,
    val muted: Color,
    val faint: Color,
    val gold: Color,
    val onGold: Color,
    val accent: Color,
    val goldText: Color,
    val coralText: Color
) {
    companion object {
        val Light = ArenaLook(
            ink = Color(0xFFFBF8F1),
            raised = Color.White,
            high = Color(0xFFF1ECDD),
            text = Color(0xFF141A33),
            muted = Color(0xFF545B78),
            faint = Color(0x29141A33),
            gold = Color(0xFFF2B705),
            onGold = Color(0xFF141A33),
            accent = Color(0xFF2B3FD8),
            goldText = Color(0xFF7F5300),
            coralText = Color(0xFFB3261E)
        )
        val Dark = ArenaLook(
            ink = ArenaNight.Ink,
            raised = ArenaNight.Raised,
            high = Color(0xFF1F2A4D),
            text = ArenaNight.Cream,
            muted = ArenaNight.Muted,
            faint = ArenaNight.Faint,
            gold = ArenaNight.Gold,
            onGold = ArenaNight.Ink,
            accent = Color(0xFF9DB0FF),
            goldText = Color(0xFFF5CF6E),
            coralText = Color(0xFFEA8080)
        )
    }
}

private fun arenaScheme(look: ArenaLook, dark: Boolean) = (if (dark) darkColorScheme() else lightColorScheme()).copy(
    // Material's text buttons and fields take primary as text: gold reads on the night, bronze by day.
    primary = if (dark) look.gold else look.goldText,
    onPrimary = if (dark) look.onGold else Color.White,
    secondary = look.accent,
    onSecondary = if (dark) look.ink else Color.White,
    tertiary = Arena.Coral,
    background = look.ink,
    onBackground = look.text,
    surface = look.ink,
    onSurface = look.text,
    onSurfaceVariant = look.muted,
    surfaceContainerLow = look.raised,
    surfaceContainer = look.raised,
    surfaceContainerHigh = look.high,
    surfaceContainerHighest = look.high,
    secondaryContainer = look.high,
    onSecondaryContainer = look.text,
    outline = look.faint,
    outlineVariant = look.faint,
    error = look.coralText
)

val ArenaLight = LilloStyle(
    arenaScheme(ArenaLook.Light, dark = false),
    LilloPalette(
        background = Brush.verticalGradient(listOf(Color(0xFFF3EEDF), ArenaLook.Light.ink)),
        hero = Arena.Hero,
        brand = ArenaLook.Light.gold,
        onBrand = ArenaLook.Light.onGold,
        accentText = ArenaLook.Light.goldText
    )
)

val ArenaDark = LilloStyle(
    arenaScheme(ArenaLook.Dark, dark = true),
    LilloPalette(
        background = Brush.verticalGradient(listOf(ArenaNight.InkTop, ArenaNight.Ink)),
        hero = Arena.Hero,
        brand = ArenaLook.Dark.gold,
        onBrand = ArenaLook.Dark.onGold,
        accentText = ArenaLook.Dark.goldText
    )
)
