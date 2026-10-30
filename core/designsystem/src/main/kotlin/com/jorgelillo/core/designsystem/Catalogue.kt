package com.jorgelillo.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// The component catalogue: every core component in one place, for Android Studio's preview pane
// and its screenshot tests: CatalogueScreenshotTest captures every preview here, so a new
// component only needs a preview. Previews are dropped from release builds.

/** An app-like scheme: core components take their colours from these roles, as in every app. */
private val CatalogueColors = darkColorScheme(
    primary = Color(0xFF00D26A),
    onPrimary = Color.Black,
    background = Color(0xFF14102B),
    onBackground = Color.White,
    surfaceContainer = Color(0xFF2A2350),
    onSurfaceVariant = Color(0xFFB7B0D9)
)

@Composable
private fun CatalogueTheme(content: @Composable () -> Unit) = LilloTheme(CatalogueColors) {
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
        colors = listOf(Color(0xFF3346A8), Color(0xFF166558)),
        emoji = "🏆",
        chips = listOf(HeroChip("⚔️ Office ping-pong: 2 to play"), HeroChip("🏆 Last champion: Riley"))
    )
}

@Preview(name = "Summary hero, landscape", device = Devices.PIXEL_7, widthDp = 915, heightDp = 412)
@Composable
private fun SummaryHeroLandscapePreview() = CatalogueTheme {
    LilloSummaryHero(
        title = "Your tournaments",
        subtitle = "1 in progress · 3 finished",
        colors = listOf(Color(0xFF3346A8), Color(0xFF166558)),
        emoji = "🏆",
        chips = listOf(HeroChip("⚔️ Office ping-pong: 2 to play"), HeroChip("🏆 Last champion: Riley"))
    )
}

@Preview(name = "Avatars")
@Composable
private fun AvatarsPreview() = CatalogueTheme {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LilloAvatar("Ana", Color(0xFFFF4D8D), 56.dp)
        LilloAvatar("álvaro", Color(0xFF00D2B4), 56.dp, ring = Color(0xFFFFC83D))
        LilloAvatar("Bea", Color(0xFFB3A1FF), 56.dp, style = AvatarStyle.Tinted)
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
private fun PagePreview() = LilloTheme(CatalogueColors) { SamplePage() }

/** Landscape (and handhelds): the actions move beside the content. */
@Preview(name = "Page, short screen", device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
private fun PageShortPreview() = LilloTheme(CatalogueColors) { SamplePage() }

/** Landscape without side actions: the actions stay below and the content scrolls. */
@Preview(name = "Page, short screen, actions below", device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
private fun PageShortActionsBelowPreview() = LilloTheme(CatalogueColors) { SamplePage(sideActions = false) }
