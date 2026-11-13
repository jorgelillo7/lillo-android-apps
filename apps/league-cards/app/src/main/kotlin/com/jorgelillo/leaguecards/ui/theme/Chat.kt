package com.jorgelillo.leaguecards.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import com.jorgelillo.core.designsystem.LilloPalette
import com.jorgelillo.core.designsystem.LilloStyle

/** One look of the league's group chat (apps/league-cards/DESIGN.md). */
@Immutable
class ChatLook(
    /** Background, bottom and top of its gradient. */
    val ink: Color,
    val inkTop: Color,
    val bubble: Color,
    val mine: Color,
    val raised: Color,
    val text: Color,
    val muted: Color,
    val faint: Color,
    /** Fills: Continue and the squad meter, trophies and the money meter, danger. */
    val green: Color,
    val onGreen: Color,
    val gold: Color,
    val red: Color,
    /** The same colours as text, at 4.5:1 on this look's bubbles. */
    val greenText: Color,
    val goldText: Color,
    val redText: Color,
    val system: Color,
    val systemText: Color
) {
    val background: Brush = Brush.verticalGradient(listOf(inkTop, ink))
}

/** The league lives in a chat, so the game looks like one: by day, or at night. */
object Chat {
    val Night = ChatLook(
        ink = Color(0xFF00110E),
        inkTop = Color(0xFF00251F),
        bubble = Color(0xFF002C25),
        mine = Color(0xFF005C4B),
        raised = Color(0xFF001E19),
        text = Color(0xFFE5E6E3),
        muted = Color(0xFFAAABA9),
        faint = Color(0x33E9EDEF),
        green = Color(0xFF00A884),
        onGreen = Color(0xFF00110E),
        gold = Color(0xFFFFC83D),
        red = Color(0xFFF15C6D),
        // Softer than the fills, so they don't vibrate on the dark background.
        greenText = Color(0xFF5FD8BC),
        goldText = Color(0xFFF5CF6E),
        redText = Color(0xFFF15C6D),
        system = Color(0xFF001713),
        systemText = Color(0xFFFFD279)
    )

    val Day = ChatLook(
        ink = Color(0xFFECE7DC),
        inkTop = Color(0xFFF4F1EA),
        bubble = Color.White,
        mine = Color(0xFFD7F3E4),
        raised = Color.White,
        text = Color(0xFF0E1F1B),
        muted = Color(0xFF54645F),
        faint = Color(0x330E1F1B),
        // Chat green with white text is only ~3:1: a deeper green for the fill.
        green = Color(0xFF007A63),
        onGreen = Color.White,
        gold = Color(0xFFFFC83D),
        red = Color(0xFFF15C6D),
        greenText = Color(0xFF00695C),
        goldText = Color(0xFF7F5300),
        redText = Color(0xFFB3261E),
        system = Color(0xFFFFF1C7),
        systemText = Color(0xFF5C4300)
    )

    /** The look in effect, picked by the theme's background. */
    val look: ChatLook
        @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Night else Day

    val Ink: Color
        @Composable @ReadOnlyComposable
        get() = look.ink
    val InkTop: Color
        @Composable @ReadOnlyComposable
        get() = look.inkTop
    val Bubble: Color
        @Composable @ReadOnlyComposable
        get() = look.bubble
    val Mine: Color
        @Composable @ReadOnlyComposable
        get() = look.mine
    val Raised: Color
        @Composable @ReadOnlyComposable
        get() = look.raised
    val Text: Color
        @Composable @ReadOnlyComposable
        get() = look.text
    val Muted: Color
        @Composable @ReadOnlyComposable
        get() = look.muted
    val Faint: Color
        @Composable @ReadOnlyComposable
        get() = look.faint
    val Green: Color
        @Composable @ReadOnlyComposable
        get() = look.green
    val Gold: Color
        @Composable @ReadOnlyComposable
        get() = look.gold
    val Red: Color
        @Composable @ReadOnlyComposable
        get() = look.red
    val GreenText: Color
        @Composable @ReadOnlyComposable
        get() = look.greenText
    val GoldText: Color
        @Composable @ReadOnlyComposable
        get() = look.goldText
    val RedText: Color
        @Composable @ReadOnlyComposable
        get() = look.redText
    val System: Color
        @Composable @ReadOnlyComposable
        get() = look.system
    val SystemText: Color
        @Composable @ReadOnlyComposable
        get() = look.systemText
    val Background: Brush
        @Composable @ReadOnlyComposable
        get() = look.background

    /** The tears meter, the same in both looks. */
    val Tear = Color(0xFF53BDEB)

    /** The home summary's gradient: emerald into bright blue, white text on top. */
    val Hero = listOf(Color(0xFF00695C), Color(0xFF1E5BB8))
    val Confetti = listOf(Color(0xFFFFC83D), Color(0xFF00A884), Tear, Color(0xFFF15C6D), Color(0xFFE5E6E3))
}

/** WCAG contrast ratio between two opaque colours. */
internal fun contrast(a: Color, b: Color): Float {
    val (hi, lo) = listOf(a.luminance(), b.luminance()).sortedDescending()
    return (hi + 0.05f) / (lo + 0.05f)
}

/**
 * [color] as text on [surface]: unchanged when it already reads at [minimum], else moved towards
 * black (on a light surface) or white (on a dark one) until it does. The league's characters bring
 * their own colours, picked for the dark chat.
 */
internal fun readableOn(color: Color, surface: Color, minimum: Float = 4.5f): Color {
    if (contrast(color, surface) >= minimum) return color
    val target = if (surface.luminance() > 0.5f) Color.Black else Color.White
    var t = 0f
    var tone = color
    while (t < 1f && contrast(tone, surface) < minimum) {
        t += 0.05f
        tone = lerp(color, target, t)
    }
    return tone
}

private fun style(look: ChatLook, dark: Boolean) = LilloStyle(
    (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = look.green,
        onPrimary = look.onGreen,
        secondary = Chat.Tear,
        onSecondary = look.ink,
        tertiary = look.gold,
        background = look.ink,
        onBackground = look.text,
        surface = look.ink,
        onSurface = look.text,
        onSurfaceVariant = look.muted,
        surfaceContainerLow = look.raised,
        surfaceContainer = look.raised,
        surfaceContainerHigh = look.bubble,
        surfaceContainerHighest = look.bubble,
        secondaryContainer = look.bubble,
        onSecondaryContainer = look.text,
        outline = look.faint,
        outlineVariant = look.faint
    ),
    LilloPalette(
        background = look.background,
        hero = Chat.Hero,
        brand = look.green,
        onBrand = look.onGreen,
        accentText = if (dark) look.green else look.greenText
    )
)

val ChatLight = style(Chat.Day, dark = false)
val ChatDark = style(Chat.Night, dark = true)
