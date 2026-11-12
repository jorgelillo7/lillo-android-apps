package com.jorgelillo.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp

// The component catalogue: every core component in one place, for Android Studio's preview pane
// and its screenshot tests: CatalogueScreenshotTest captures every preview here, so a new
// component only needs a preview. Previews are dropped from release builds.

// CatalogueScreenshotTest captures each preview in both looks: the dark one under the preview's
// name, the light one with `_light` added.

/** An app-like pair of looks: core components take their colours from these roles, as in every app. */
private val CatalogueDark = LilloStyle(
    darkColorScheme(
        primary = Color(0xFF00D26A),
        onPrimary = Color.Black,
        background = Color(0xFF14102B),
        onBackground = Color.White,
        surfaceContainer = Color(0xFF2A2350),
        onSurfaceVariant = Color(0xFFB7B0D9)
    ),
    LilloPalette(
        background = SolidColor(Color(0xFF14102B)),
        hero = listOf(Color(0xFF2B3FD8), Color(0xFF00695C)),
        brand = Color(0xFF00D26A),
        onBrand = Color.Black,
        accentText = Color(0xFF5CF0A2)
    )
)

private val CatalogueLight = LilloStyle(
    lightColorScheme(
        primary = Color(0xFF006C3A),
        onPrimary = Color.White,
        background = Color(0xFFF6F4FB),
        onBackground = Color(0xFF1B1530),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1B1530),
        surfaceContainer = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFEDEAF5),
        onSurfaceVariant = Color(0xFF4A4363)
    ),
    LilloPalette(
        background = SolidColor(Color(0xFFF6F4FB)),
        hero = listOf(Color(0xFF2B3FD8), Color(0xFF00695C)),
        brand = Color(0xFF006C3A),
        onBrand = Color.White,
        accentText = Color(0xFF006C3A)
    )
)

@Composable
private fun CatalogueLooks(content: @Composable () -> Unit) = LilloTheme(CatalogueLight, CatalogueDark, content)

@Composable
private fun CatalogueTheme(content: @Composable () -> Unit) = CatalogueLooks {
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            content()
        }
    }
}

@Preview(name = "Buttons")
@Composable
private fun ButtonsPreview() = CatalogueTheme {
    LilloBigButton("Continue", {})
    LilloBigButton("Disabled", {}, enabled = false)
    LilloSecondaryButton("Short season · 10 matchdays", {})
}

@Preview(name = "Summary hero")
@Composable
private fun SummaryHeroPreview() = CatalogueTheme {
    LilloSummaryHero(
        title = "Your tournaments",
        subtitle = "1 in progress · 3 finished",
        emoji = "🏆",
        chips = listOf(HeroChip("⚔️ Office ping-pong: 2 to play"), HeroChip("🏆 Last champion: Riley"))
    )
}

@Preview(name = "Screen header")
@Composable
private fun ScreenHeaderPreview() = CatalogueTheme {
    LilloScreenHeader("Your wheels", subtitle = "2 wheels · 4 decisions", emoji = "🎡")
    LilloScreenHeader("Settings")
}

@Preview(name = "Type scale")
@Composable
private fun TypeScalePreview() = CatalogueTheme {
    Text("12 días · 1.240", style = MaterialTheme.typography.displayMedium)
    Text("¿Quién miente?", style = MaterialTheme.typography.headlineMedium)
    Text("Ronda 3 de 5", style = MaterialTheme.typography.titleLarge)
    Text("Body text stays the phone's own face, as easy to read as every other app.")
}

/** The display face at twice the font size, as a phone set to the largest text shows it. */
@Preview(name = "Type scale, largest font")
@Composable
private fun TypeScaleLargestFontPreview() = CatalogueTheme {
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
        Text("12 días", style = MaterialTheme.typography.displayMedium)
        Text("¿Quién llora esta jornada?", style = MaterialTheme.typography.headlineMedium)
        LilloScreenHeader("Tus ruedas", subtitle = "2 ruedas · 4 decisiones", emoji = "🎡")
    }
}

@Preview(name = "Appearance choice")
@Composable
private fun ThemeModePreview() = CatalogueTheme {
    var mode by remember { mutableStateOf(LilloThemeMode.System) }
    ThemeModeRow(mode) { mode = it }
}

@Preview(name = "Summary hero, landscape", device = Devices.PIXEL_7, widthDp = 915, heightDp = 412)
@Composable
private fun SummaryHeroLandscapePreview() = CatalogueTheme {
    LilloSummaryHero(
        title = "Your tournaments",
        subtitle = "1 in progress · 3 finished",
        emoji = "🏆",
        chips = listOf(HeroChip("⚔️ Office ping-pong: 2 to play"), HeroChip("🏆 Last champion: Riley"))
    )
}

@Preview(name = "Accent edge")
@Composable
private fun AccentEdgePreview() = CatalogueTheme {
    listOf(Color(0xFF3DDC97) to "In progress", Color(0xFFFFC83D) to "Finished").forEach { (color, label) ->
        Text(
            label,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .accentEdge(color)
                .padding(20.dp)
        )
    }
}

@Preview(name = "Avatars")
@Composable
private fun AvatarsPreview() = CatalogueTheme {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LilloAvatar("Ana", Color(0xFFFF4D8D), 56.dp)
        LilloAvatar("álvaro", Color(0xFF00D2B4), 56.dp, ring = Color(0xFFFFC83D))
        // A tinted initial is drawn in its colour: the light look needs a darker one.
        LilloAvatar(
            "Bea",
            if (lilloIsDark()) Color(0xFFB3A1FF) else Color(0xFF4A2FB0),
            56.dp,
            style = AvatarStyle.Tinted
        )
        LilloAvatar("🦁 Leones", Color(0xFFFFC83D), 56.dp, style = AvatarStyle.Tinted)
        LilloAvatar("👨‍🍳 Chef", Color(0xFF3DDC97), 56.dp)
    }
}

@Preview(name = "Rows and section title")
@Composable
private fun RowsPreview() = CatalogueTheme {
    LilloSectionTitle("Game", uppercase = true)
    LilloSettingRow("Language", subtitle = "English", leading = "🌐", onClick = {}) { Text("›") }
    LilloSettingRow("Chaos", subtitle = "Some games get a random number of impostors") {
        Switch(checked = true, onCheckedChange = {})
    }
    LilloSettingRow("Rename", subtitle = "Ana", borderColor = Color(0xFFB7B0D9).copy(alpha = 0.3f), onClick = {})
}

@Composable
private fun SamplePage(sideActions: Boolean = true) = LilloPage(
    title = null,
    onBack = null,
    background = Brush.verticalGradient(listOf(Color(0xFF0A72D6), Color(0xFF2448E0))),
    contentColor = Color.White,
    sideActionsWhenShort = sideActions,
    bottom = { LilloBigButton("Continue", {}) }
) {
    Text("You caught the impostor!", style = MaterialTheme.typography.headlineMedium)
    repeat(4) { Text("Content line ${it + 1}") }
}

@Preview(name = "Page", device = Devices.PIXEL_7)
@Composable
private fun PagePreview() = CatalogueLooks { SamplePage() }

/** Landscape (and handhelds): the actions move beside the content. */
@Preview(name = "Page, short screen", device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
private fun PageShortPreview() = CatalogueLooks { SamplePage() }

/** Landscape without side actions: the actions stay below and the content scrolls. */
@Preview(name = "Page, short screen, actions below", device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
private fun PageShortActionsBelowPreview() = CatalogueLooks { SamplePage(sideActions = false) }

/** A launcher that accepts the request: the button, and the way by hand in case it drops it. */
@Preview(name = "Widget card")
@Composable
private fun WidgetCardPreview() = CatalogueTheme {
    LilloWidgetCard("¿Hay jaleo?", pinnable = true, onAdd = {})
}

/** A launcher that refuses requests (MIUI and others): only the way by hand. */
@Preview(name = "Widget card, by hand")
@Composable
private fun WidgetCardByHandPreview() = CatalogueTheme {
    LilloWidgetCard("¿Hay jaleo?", pinnable = false, onAdd = {})
}
