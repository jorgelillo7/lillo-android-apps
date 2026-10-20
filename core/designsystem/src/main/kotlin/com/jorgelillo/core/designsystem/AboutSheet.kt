package com.jorgelillo.core.designsystem

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Texts of [LilloAboutSheet], from the app's own resources. */
data class AboutTexts(val title: String, val version: String, val body: String, val rate: String?, val privacy: String)

/**
 * The About sheet: app icon, name, version, a short text, the app's language (Android 13+, when it
 * ships in more than one of [languages]), and links to rate it on Google Play (left out while the
 * app isn't published: [playStoreUrl] null) and to the privacy policy.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LilloAboutSheet(
    icon: Painter,
    iconBackground: Color,
    containerColor: Color,
    texts: AboutTexts,
    privacyUrl: String,
    playStoreUrl: String?,
    onDismiss: () -> Unit,
    languages: List<Pair<String, String>> = LilloLanguages,
) {
    val uriHandler = LocalUriHandler.current
    var pickLanguage by rememberSaveable { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = containerColor, modifier = TestTagsAsResourceIds) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(88.dp).clip(MaterialTheme.shapes.large).background(iconBackground),
            )
            Text(texts.title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Text(texts.version, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(texts.body, textAlign = TextAlign.Center)
            if (canPickAppLanguage && languages.size > 1) {
                OutlinedButton(onClick = { pickLanguage = true }, modifier = Modifier.fillMaxWidth().testTag("language")) {
                    Text(stringResource(R.string.lillo_language) + ": " + currentAppLanguageName())
                }
            }
            if (playStoreUrl != null && texts.rate != null) {
                OutlinedButton(onClick = { uriHandler.openUri(playStoreUrl) }, modifier = Modifier.fillMaxWidth().testTag("rate")) {
                    Text(texts.rate)
                }
            }
            OutlinedButton(onClick = { uriHandler.openUri(privacyUrl) }, modifier = Modifier.fillMaxWidth().testTag("privacy")) {
                Text(texts.privacy)
            }
        }
    }
    if (pickLanguage && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        LilloLanguageDialog(onDismiss = { pickLanguage = false }, languages = languages)
    }
}
