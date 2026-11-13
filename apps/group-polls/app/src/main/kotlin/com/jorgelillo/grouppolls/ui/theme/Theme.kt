package com.jorgelillo.grouppolls.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.jorgelillo.core.designsystem.LilloPalette
import com.jorgelillo.core.designsystem.LilloStyle
import com.jorgelillo.grouppolls.domain.Side

/** One look of the voting paper (apps/group-polls/DESIGN.md). */
@Immutable
class PollsLook(
    val paper: Color,
    val headerTop: Color,
    val card: Color,
    val raised: Color,
    val ink: Color,
    val muted: Color,
    val line: Color,
    /** Red and blue as text, borders and dots; their soft tints behind an answer. */
    val red: Color,
    val blue: Color,
    val redSoft: Color,
    val blueSoft: Color,
    /** The main button and the text on it. */
    val brand: Color,
    val onBrand: Color
) {
    val header: Brush = Brush.verticalGradient(listOf(headerTop, paper))
}

/** Light, rounded and friendly by day, graphite at night; two big colours: red against blue. */
object Polls {
    // Red and blue are deep enough for text on their soft tints and for white text on their fills (4.5:1).
    val Day = PollsLook(
        paper = Color(0xFFF3F6FB),
        headerTop = Color(0xFFDDEBFF),
        card = Color.White,
        raised = Color(0xFFF3F6FB),
        ink = Color(0xFF1B2433),
        muted = Color(0xFF5A6577),
        line = Color(0xFFE2E8F1),
        red = Color(0xFFC81E3A),
        blue = Color(0xFF1E50E0),
        redSoft = Color(0xFFFFE3E6),
        blueSoft = Color(0xFFE1EAFF),
        brand = Color(0xFF1E50E0),
        onBrand = Color.White
    )

    /** Neutral graphite, not navy: a blue-tinted night would hide the blue answer. */
    val Night = PollsLook(
        paper = Color(0xFF14161A),
        headerTop = Color(0xFF1B2233),
        card = Color(0xFF1E2127),
        raised = Color(0xFF272B33),
        ink = Color(0xFFECEFF4),
        muted = Color(0xFFA9B1BF),
        line = Color(0xFF2E333D),
        red = Color(0xFFFF8A93),
        blue = Color(0xFF8FB0FF),
        redSoft = Color(0xFF3A1A20),
        blueSoft = Color(0xFF1A2444),
        brand = Color(0xFF8FB0FF),
        onBrand = Color(0xFF0E1830)
    )

    /** The look in effect, picked by the theme's background. */
    val look: PollsLook
        @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Night else Day

    val Paper: Color
        @Composable @ReadOnlyComposable
        get() = look.paper
    val Card: Color
        @Composable @ReadOnlyComposable
        get() = look.card
    val Ink: Color
        @Composable @ReadOnlyComposable
        get() = look.ink
    val Muted: Color
        @Composable @ReadOnlyComposable
        get() = look.muted
    val Line: Color
        @Composable @ReadOnlyComposable
        get() = look.line
    val Red: Color
        @Composable @ReadOnlyComposable
        get() = look.red
    val Blue: Color
        @Composable @ReadOnlyComposable
        get() = look.blue
    val RedSoft: Color
        @Composable @ReadOnlyComposable
        get() = look.redSoft
    val BlueSoft: Color
        @Composable @ReadOnlyComposable
        get() = look.blueSoft
    val Brand: Color
        @Composable @ReadOnlyComposable
        get() = look.brand
    val Header: Brush
        @Composable @ReadOnlyComposable
        get() = look.header

    /** The answers' fills, white text on them, the same in both looks. */
    val RedFillColor = Color(0xFFC81E3A)
    val BlueFillColor = Color(0xFF1E50E0)
    val RedFill = Brush.verticalGradient(listOf(Color(0xFFD93348), RedFillColor))
    val BlueFill = Brush.verticalGradient(listOf(Color(0xFF3366F0), BlueFillColor))

    @Composable @ReadOnlyComposable
    fun color(side: Side) = if (side == Side.RED) Red else Blue

    @Composable @ReadOnlyComposable
    fun soft(side: Side) = if (side == Side.RED) RedSoft else BlueSoft

    fun fill(side: Side) = if (side == Side.RED) RedFill else BlueFill
}

private fun style(look: PollsLook, dark: Boolean) = LilloStyle(
    (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = look.brand,
        onPrimary = look.onBrand,
        secondary = look.red,
        onSecondary = if (dark) look.paper else Color.White,
        background = look.paper,
        onBackground = look.ink,
        surface = look.card,
        onSurface = look.ink,
        onSurfaceVariant = look.muted,
        surfaceContainerLow = look.card,
        surfaceContainer = look.card,
        surfaceContainerHigh = look.raised,
        outline = look.line,
        error = look.red
    ),
    LilloPalette(
        background = look.header,
        hero = listOf(look.brand, look.red),
        brand = look.brand,
        onBrand = look.onBrand,
        accentText = look.blue
    )
)

val PollsLight = style(Polls.Day, dark = false)
val PollsDark = style(Polls.Night, dark = true)
