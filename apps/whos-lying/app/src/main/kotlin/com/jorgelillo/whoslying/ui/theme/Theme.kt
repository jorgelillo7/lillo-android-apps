package com.jorgelillo.whoslying.ui.theme

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
import com.jorgelillo.core.designsystem.LilloTheme

/**
 * "Neon": violet actions, turquoise for the word, pink for impostors, amber for hints. Menus are
 * lavender by day and violet night by night; the round itself plays on vivid stages (reveal,
 * vote, results) that look the same in both, inside [NightStage].
 */
object Neon {
    /** Dark ink for text and icons on the bright fills (Go, turquoise, amber, avatars). */
    val OnFill = Color(0xFF14052B)
    val Violet = Color(0xFF7756FA)
    val Turquoise = Color(0xFF00E0B8)
    val Pink = Color(0xFFFF4D94)
    val Amber = Color(0xFFFFC94D)

    /** "Go" actions: hide the card, vote, next round. */
    val Go = Color(0xFF00D26A)

    /** The home summary's gradient: electric violet into raspberry, white text on top. */
    val Hero = listOf(Color(0xFF5B2EE6), Color(0xFFC2185B))

    /** Confetti and fireworks. */
    val Confetti = listOf(Amber, Pink, Turquoise, Color.White, Color(0xFF7CFF6B), Violet)

    val RevealBackdrop = Brush.verticalGradient(listOf(Color(0xFF6A4AF0), Color(0xFF4B2BD6)))

    // Result screens: who won sets the mood. The light end of each gradient keeps white text at
    // 4.5:1 or more (WCAG AA), checked by the accessibility checks in the screenshot tests.
    val CiviliansBackdrop = Brush.verticalGradient(listOf(Color(0xFF0A72D6), Color(0xFF2346E8)))
    val ImpostorsBackdrop = Brush.verticalGradient(listOf(Color(0xFFC2410C), Color(0xFFC81E3A)))
    val DrifterBackdrop = Brush.verticalGradient(listOf(Color(0xFFB45309), Color(0xFFBE185D)))

    private val look: NeonLook
        @Composable
        @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) NeonLook.Dark else NeonLook.Light

    /** The page background colour. */
    val Night: Color
        @Composable @ReadOnlyComposable
        get() = look.night
    val Card: Color
        @Composable @ReadOnlyComposable
        get() = look.card
    val CardHigh: Color
        @Composable @ReadOnlyComposable
        get() = look.cardHigh
    val Ink: Color
        @Composable @ReadOnlyComposable
        get() = look.ink
    val Muted: Color
        @Composable @ReadOnlyComposable
        get() = look.muted

    // Text tones of the accents, ≥ 4.5:1 on this look's cards and background.
    val VioletLight: Color
        @Composable @ReadOnlyComposable
        get() = look.violetText
    val TurquoiseText: Color
        @Composable @ReadOnlyComposable
        get() = look.turquoiseText
    val PinkText: Color
        @Composable @ReadOnlyComposable
        get() = look.pinkText
    val AmberText: Color
        @Composable @ReadOnlyComposable
        get() = look.amberText
}

private data class NeonLook(
    val night: Color,
    val card: Color,
    val cardHigh: Color,
    val ink: Color,
    val muted: Color,
    val violetText: Color,
    val turquoiseText: Color,
    val pinkText: Color,
    val amberText: Color
) {
    companion object {
        val Light = NeonLook(
            night = Color(0xFFF6F2FF),
            card = Color.White,
            cardHigh = Color(0xFFECE4FF),
            ink = Color(0xFF1D1035),
            muted = Color(0xFF5D4E78),
            violetText = Color(0xFF4B2BC8),
            turquoiseText = Color(0xFF006B5A),
            pinkText = Color(0xFFB0124F),
            amberText = Color(0xFF7F4C00)
        )
        val Dark = NeonLook(
            night = Color(0xFF14052B),
            card = Color(0xFF210F3D),
            cardHigh = Color(0xFF2F1A50),
            ink = Color(0xFFEEE0FF),
            muted = Color(0xFFB6A3D1),
            violetText = Color(0xFF8B6EFF),
            turquoiseText = Color(0xFF6AE7D0),
            pinkText = Color(0xFFFFA3C8),
            amberText = Color(0xFFFFE29A)
        )
    }
}

private val DayViolet = Color(0xFF5B3BE0)

val NeonLight = LilloStyle(
    lightColorScheme(
        primary = DayViolet,
        onPrimary = Color.White,
        secondary = Color(0xFF00806B),
        onSecondary = Color.White,
        tertiary = Color(0xFFD81B60),
        background = NeonLook.Light.night,
        onBackground = NeonLook.Light.ink,
        surface = NeonLook.Light.night,
        onSurface = NeonLook.Light.ink,
        onSurfaceVariant = NeonLook.Light.muted,
        surfaceContainerLow = NeonLook.Light.card,
        surfaceContainer = NeonLook.Light.card,
        surfaceContainerHigh = NeonLook.Light.cardHigh,
        surfaceContainerHighest = NeonLook.Light.cardHigh,
        outline = Color(0x551D1035),
        error = Color(0xFFB0124F)
    ),
    LilloPalette(
        background = Brush.verticalGradient(listOf(Color(0xFFE4DAFF), NeonLook.Light.night, NeonLook.Light.night)),
        hero = Neon.Hero,
        brand = DayViolet,
        onBrand = Color.White,
        accentText = NeonLook.Light.violetText
    )
)

val NeonDark = LilloStyle(
    darkColorScheme(
        primary = Neon.Violet,
        onPrimary = Color.White,
        secondary = Neon.Turquoise,
        onSecondary = Neon.OnFill,
        tertiary = Neon.Pink,
        background = NeonLook.Dark.night,
        onBackground = NeonLook.Dark.ink,
        surface = NeonLook.Dark.night,
        onSurface = NeonLook.Dark.ink,
        onSurfaceVariant = NeonLook.Dark.muted,
        surfaceContainerLow = NeonLook.Dark.card,
        surfaceContainer = NeonLook.Dark.card,
        surfaceContainerHigh = NeonLook.Dark.cardHigh,
        surfaceContainerHighest = NeonLook.Dark.cardHigh,
        outline = Color(0x559692BE),
        error = Neon.Pink
    ),
    LilloPalette(
        background = Brush.verticalGradient(listOf(Color(0xFF2B1B6B), NeonLook.Dark.night, NeonLook.Dark.night)),
        hero = Neon.Hero,
        brand = Neon.Violet,
        onBrand = Color.White,
        accentText = NeonLook.Dark.violetText
    )
)

/**
 * The round's vivid stages (reveal, debate, vote, results, ranking) look the same by day and by
 * night: their colours and text tones are the night ones whatever the phone's setting.
 */
@Composable
internal fun NightStage(content: @Composable () -> Unit) = LilloTheme(NeonDark, content)
