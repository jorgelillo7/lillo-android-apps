package com.jorgelillo.core.designsystem

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat

/** The languages the apps ship in, as BCP 47 tag to the language's own name. */
val LilloLanguages = listOf("es" to "Español", "en" to "English")

/**
 * Whether the app can switch its own language: per-app languages arrived in Android 13 (the
 * convention plugin generates the locale config they need). Before that it follows the system.
 */
@get:ChecksSdkIntAtLeast(api = Build.VERSION_CODES.TIRAMISU)
val canPickAppLanguage: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/** The language the app shows now, in that language ("Español", "English"). */
@Composable
fun currentAppLanguageName(): String =
    ConfigurationCompat.getLocales(LocalConfiguration.current)[0]?.let { it.getDisplayLanguage(it) }.orEmpty().replaceFirstChar { it.uppercase() }

/**
 * Picks the app's language (or back to the system's) without leaving it; Android restarts the
 * activity in the new language. Test tags: language_system, language_<tag>.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun LilloLanguageDialog(onDismiss: () -> Unit, languages: List<Pair<String, String>> = LilloLanguages) {
    val context = LocalContext.current
    fun choose(tags: String) {
        context.getSystemService(LocaleManager::class.java).applicationLocales = LocaleList.forLanguageTags(tags)
        onDismiss()
    }
    AlertDialog(
        modifier = TestTagsAsResourceIds,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.lillo_language)) },
        text = {
            Column {
                (listOf("" to stringResource(R.string.lillo_language_system)) + languages).forEach { (tag, label) ->
                    TextButton(onClick = { choose(tag) }, modifier = Modifier.fillMaxWidth().testTag("language_${tag.ifEmpty { "system" }}")) {
                        Text(label, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.lillo_cancel)) } },
    )
}
