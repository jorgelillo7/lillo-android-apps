package com.jorgelillo.slipper.ui.theme

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

/**
 * One look of grandma's house (apps/slipper/DESIGN.md): the menus' colours and the backdrop the
 * run is drawn on. The characters, the slipper and the things on the floor are the same in both.
 */
@Immutable
class KitchenLook(
    val cream: Color,
    val creamDeep: Color,
    val ink: Color,
    val muted: Color,
    val raised: Color,
    /** The Play fill and the text on it. */
    val green: Color,
    val onGreen: Color,
    /** Green and red as text: on the panels and over the scene. */
    val greenText: Color,
    val shout: Color,
    val kitchenWall: Color,
    val tile: Color,
    val tileWhite: Color,
    val grout: Color,
    val floor: Color,
    val floorEdge: Color,
    val plank: Color,
    val beam: Color,
    val hallPaper: Color,
    val hallStripe: Color,
    val hallFloor: Color,
    val livingPaper: Color,
    val livingFloor: Color,
    val wainscot: Color,
    val wainscotEdge: Color,
    val upstairs: Color,
    val upstairsLine: Color,
    /** Through the windows (the cream cloud in it reads as the moon at night). */
    val sky: Color,
    /** Light pooled under each ceiling lamp; transparent by day. */
    val lampGlow: Color,
    /** Behind the points and words that pop up over the scene. */
    val popBubble: Color
) {
    val background: Brush = Brush.verticalGradient(listOf(cream, creamDeep))
}

/** "Grandma's house": a sunny kitchen by day, the same rooms with the lamps on by night. */
object Kitchen {
    /** Cream walls, blue and white tiles, terracotta floor. */
    val Day = KitchenLook(
        cream = Color(0xFFFFECC8),
        creamDeep = Color(0xFFFFD6AA),
        ink = Color(0xFF3C2828),
        muted = Color(0xFF6B4A3A),
        raised = Color(0xFFFFF6E6),
        green = Color(0xFF1F7A5A), // white text on it: 5.4:1
        onGreen = Color.White,
        greenText = Color(0xFF155C44), // on cream: 6:1
        shout = Color(0xFFB0263A), // red text on cream: 5.6:1
        kitchenWall = Color(0xFFFFECC8),
        tile = Color(0xFF468CC8),
        tileWhite = Color(0xFFEBF0F5),
        grout = Color(0xFFD9C9B0),
        floor = Color(0xFFB0623E),
        floorEdge = Color(0xFF8C4628),
        plank = Color(0xFF9E5434),
        beam = Color(0xFF7A4F28),
        hallPaper = Color(0xFFE6EED8),
        hallStripe = Color(0xFFCFDDBE),
        hallFloor = Color(0xFF9C6B3E),
        livingPaper = Color(0xFFF3DCD0),
        livingFloor = Color(0xFFA8463E),
        wainscot = Color(0xFFB07A45),
        wainscotEdge = Color(0xFF7A4F28),
        upstairs = Color(0xFFE9CFA8),
        upstairsLine = Color(0xFFD2B48C),
        sky = Color(0xFF9FD3F0),
        lampGlow = Color.Transparent,
        popBubble = Color.White
    )

    /** The same rooms at night: walls about half as bright and a little cooler, the lamps on. */
    val Night = KitchenLook(
        cream = Color(0xFF2A1C17),
        creamDeep = Color(0xFF1A110E),
        ink = Color(0xFFFFECC8),
        muted = Color(0xFFD9BFA3),
        raised = Color(0xFF33231E),
        green = Color(0xFF6BD6A8),
        onGreen = Color(0xFF10261C),
        greenText = Color(0xFF7FDDB5),
        shout = Color(0xFFFF8A8F),
        kitchenWall = Color(0xFF4B3A3F),
        tile = Color(0xFF2B5480),
        tileWhite = Color(0xFF7C8CA6),
        grout = Color(0xFF4A4352),
        floor = Color(0xFF6B3A28),
        floorEdge = Color(0xFF4A2518),
        plank = Color(0xFF5A2E1E),
        beam = Color(0xFF3E2A1A),
        hallPaper = Color(0xFF4A5048),
        hallStripe = Color(0xFF3E4A3E),
        hallFloor = Color(0xFF553A22),
        livingPaper = Color(0xFF524048),
        livingFloor = Color(0xFF5E2826),
        wainscot = Color(0xFF5E4128),
        wainscotEdge = Color(0xFF3E2A1A),
        upstairs = Color(0xFF3A2C2A),
        upstairsLine = Color(0xFF54443A),
        sky = Color(0xFF1B2748),
        lampGlow = Color(0x1CFFD27A),
        popBubble = Color(0xFF33231E)
    )

    /** The look in effect, picked by the theme's background. */
    val look: KitchenLook
        @Composable @ReadOnlyComposable
        get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Night else Day

    val Ink: Color
        @Composable @ReadOnlyComposable
        get() = look.ink
    val Muted: Color
        @Composable @ReadOnlyComposable
        get() = look.muted
    val Raised: Color
        @Composable @ReadOnlyComposable
        get() = look.raised
    val Green: Color
        @Composable @ReadOnlyComposable
        get() = look.green
    val GreenText: Color
        @Composable @ReadOnlyComposable
        get() = look.greenText
    val Shout: Color
        @Composable @ReadOnlyComposable
        get() = look.shout
    val Background: Brush
        @Composable @ReadOnlyComposable
        get() = look.background

    /** The same in both looks: grandma's red slipper and the croquetas. */
    val Slipper = Color(0xFFE5465A)
    val Croqueta = Color(0xFFD68C32)
}

private fun scheme(look: KitchenLook, dark: Boolean) = (if (dark) darkColorScheme() else lightColorScheme()).copy(
    primary = look.green,
    onPrimary = look.onGreen,
    secondary = Kitchen.Slipper,
    background = look.cream,
    onBackground = look.ink,
    surface = look.raised,
    onSurface = look.ink,
    surfaceContainer = look.raised,
    surfaceContainerLow = look.raised,
    onSurfaceVariant = look.muted
)

private fun style(look: KitchenLook, dark: Boolean) = scheme(look, dark).let {
    LilloStyle(
        it,
        LilloPalette(
            background = look.background,
            hero = listOf(look.green, look.shout),
            brand = look.green,
            onBrand = look.onGreen,
            accentText = look.shout
        )
    )
}

val KitchenLight = style(Kitchen.Day, dark = false)
val KitchenDark = style(Kitchen.Night, dark = true)
