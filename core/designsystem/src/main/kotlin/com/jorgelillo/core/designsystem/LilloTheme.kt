package com.jorgelillo.core.designsystem

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The colours of an app beyond Material's roles, read by core components through [LilloColors]:
 * the page [background], the [hero] gradient (dark enough for white text at 4.5:1), the [brand]
 * fill of the main action and the text on it ([onBrand]), and [accentText], the brand colour as it
 * reads on the background (titles, links, numbers).
 */
@Immutable
data class LilloPalette(
    val background: Brush,
    val hero: List<Color>,
    val brand: Color,
    val onBrand: Color,
    val accentText: Color
) {
    companion object {
        /** What a scheme alone implies: a flat background and the primary colour as the brand. */
        fun from(scheme: ColorScheme) = LilloPalette(
            background = SolidColor(scheme.background),
            hero = listOf(scheme.primary, lerp(scheme.primary, Color.Black, 0.35f)),
            brand = scheme.primary,
            onBrand = scheme.onPrimary,
            accentText = scheme.primary
        )
    }
}

/** One look of an app: Material's [scheme] and the app's own [palette]. */
@Immutable
data class LilloStyle(val scheme: ColorScheme, val palette: LilloPalette = LilloPalette.from(scheme))

/** Which of an app's two looks shows: the phone's setting, or the one the user picked. */
enum class LilloThemeMode { System, Light, Dark }

/** The app's [LilloPalette], provided by [LilloTheme]; read it through [LilloColors]. */
val LocalLilloPalette = staticCompositionLocalOf { LilloPalette.from(darkColorScheme()) }

/**
 * The user's [LilloThemeMode], for apps that have a light and a dark look; null for apps with
 * one, which then offer no choice. Provided by [setLilloContent]; tests provide it to force a look.
 */
val LocalLilloThemeMode = compositionLocalOf<MutableState<LilloThemeMode>?> { null }

/** The current app's [LilloPalette]. */
object LilloColors {
    val current: LilloPalette
        @Composable @ReadOnlyComposable
        get() = LocalLilloPalette.current
}

/** Whether the [LilloTheme] in effect shows the dark look. */
@Composable
fun lilloIsDark(): Boolean = when (LocalLilloThemeMode.current?.value ?: LilloThemeMode.System) {
    LilloThemeMode.System -> isSystemInDarkTheme()
    LilloThemeMode.Light -> false
    LilloThemeMode.Dark -> true
}

/**
 * Base theme shared by all apps: the app's [light] or [dark] look, following the phone unless the
 * user picked one ([LocalLilloThemeMode]). Typography and shapes stay the same across the apps.
 * No dynamic colour: each app's colour is its identity.
 */
@Composable
fun LilloTheme(light: LilloStyle, dark: LilloStyle, content: @Composable () -> Unit) =
    LilloTheme(if (lilloIsDark()) dark else light, content)

/** An app with a single look: [style] whatever the phone's setting. */
@Composable
fun LilloTheme(style: LilloStyle, content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLilloPalette provides style.palette) {
        MaterialTheme(
            colorScheme = style.scheme,
            typography = LilloTypography,
            shapes = LilloShapes,
            content = content
        )
    }
}

/** An app with a single look, from its scheme alone. */
@Composable
fun LilloTheme(colorScheme: ColorScheme = darkColorScheme(), content: @Composable () -> Unit) =
    LilloTheme(LilloStyle(colorScheme), content)

/** Remembers the user's [LilloThemeMode] for this app (one per app: preferences are per package). */
object LilloThemeModes {
    private const val FILE = "lillo_theme"
    private const val KEY = "mode"

    fun read(context: Context): LilloThemeMode = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        .getString(KEY, null)
        ?.let { name -> LilloThemeMode.entries.firstOrNull { it.name == name } }
        ?: LilloThemeMode.System

    fun write(context: Context, mode: LilloThemeMode) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, mode.name).apply()
    }
}

/**
 * The display face: Figtree (SIL Open Font License, `assets/licenses/figtree-OFL.txt`), the
 * heavy geometric voice of the store graphics, for titles and big numbers. Two static weights,
 * Latin only; body text stays the system face.
 */
val LilloDisplayFamily = FontFamily(
    Font(R.font.figtree_extrabold, FontWeight.ExtraBold),
    Font(R.font.figtree_black, FontWeight.Black)
)

private val Base = Typography()

/** Tabular figures keep counters from jittering as digits change. */
private const val TABULAR_NUMBERS = "tnum"

private fun TextStyle.display(weight: FontWeight, tabular: Boolean = false) = copy(
    fontFamily = LilloDisplayFamily,
    fontWeight = weight,
    fontFeatureSettings = if (tabular) TABULAR_NUMBERS else null
)

val LilloTypography = Base.copy(
    displayLarge = Base.displayLarge.display(FontWeight.Black, tabular = true),
    displayMedium = Base.displayMedium.display(FontWeight.Black, tabular = true),
    displaySmall = Base.displaySmall.display(FontWeight.ExtraBold, tabular = true),
    headlineLarge = Base.headlineLarge.display(FontWeight.ExtraBold),
    headlineMedium = Base.headlineMedium.display(FontWeight.ExtraBold),
    headlineSmall = Base.headlineSmall.display(FontWeight.ExtraBold),
    titleLarge = Base.titleLarge.display(FontWeight.ExtraBold),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
)

val LilloShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)
