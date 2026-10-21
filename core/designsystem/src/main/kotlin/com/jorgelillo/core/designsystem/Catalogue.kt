package com.jorgelillo.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// The component catalogue: every core component in one place, for Android Studio's preview pane
// (the screenshot tests in src/test pin how they look). Previews are dropped from release builds.

@Composable
private fun CatalogueTheme(content: @Composable () -> Unit) = LilloTheme {
    Column(Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        content()
    }
}

@Preview(name = "Buttons")
@Composable
private fun ButtonsPreview() = CatalogueTheme {
    LilloBigButton("Continue", {})
    LilloBigButton("Disabled", {}, enabled = false)
}

@Preview(name = "Avatars")
@Composable
private fun AvatarsPreview() = CatalogueTheme {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LilloAvatar("Ana", Color(0xFFFF4D8D), 56.dp)
        LilloAvatar("álvaro", Color(0xFF00D2B4), 56.dp, ring = Color(0xFFFFC83D))
        LilloAvatar("Bea", Color(0xFFB3A1FF), 56.dp, style = AvatarStyle.Tinted)
        LilloAvatar("👨‍🍳 Chef", Color(0xFF3DDC97), 56.dp)
    }
}

@Preview(name = "Rows and section title")
@Composable
private fun RowsPreview() = CatalogueTheme {
    LilloSectionTitle("Game", uppercase = true)
    LilloSettingRow("Language", subtitle = "English", leading = "🌐", onClick = {}) { Text("›") }
    LilloSettingRow("Chaos", subtitle = "Some games get a random number of impostors") { Switch(checked = true, onCheckedChange = {}) }
}

@Composable
private fun SamplePage() = LilloPage(
    title = null,
    onBack = null,
    background = Brush.verticalGradient(listOf(Color(0xFF0A72D6), Color(0xFF2448E0))),
    sideActionsWhenShort = true,
    bottom = { LilloBigButton("Continue", {}) }
) {
    Text("You caught the impostor!", style = MaterialTheme.typography.headlineMedium)
    repeat(4) { Text("Content line ${it + 1}") }
}

@Preview(name = "Page", device = Devices.PIXEL_7)
@Composable
private fun PagePreview() = LilloTheme { SamplePage() }

/** Landscape (and handhelds): the actions move beside the content. */
@Preview(name = "Page, short screen", device = "spec:width=411dp,height=891dp,orientation=landscape")
@Composable
private fun PageShortPreview() = LilloTheme { SamplePage() }
