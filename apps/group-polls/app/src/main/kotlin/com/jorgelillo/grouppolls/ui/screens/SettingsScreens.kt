package com.jorgelillo.grouppolls.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.LilloLanguageDialog
import com.jorgelillo.core.designsystem.LilloSettingRow
import com.jorgelillo.core.designsystem.R as CoreR
import com.jorgelillo.core.designsystem.canPickAppLanguage
import com.jorgelillo.core.designsystem.currentAppLanguageName
import com.jorgelillo.grouppolls.BuildConfig
import com.jorgelillo.grouppolls.R
import com.jorgelillo.grouppolls.data.LocalState
import com.jorgelillo.grouppolls.domain.Drafts
import com.jorgelillo.grouppolls.domain.Limits
import com.jorgelillo.grouppolls.domain.PredictionRecord
import com.jorgelillo.grouppolls.ui.theme.Polls

/** First launch: pick the name your friends will see. */
@Composable
internal fun WelcomeScreen(onDone: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    val clean = Drafts.cleanName(name)
    Page(
        title = null,
        onBack = null,
        modifier = Modifier.imePadding(),
        bottom = {
            BigButton(stringResource(R.string.welcome_start), {
                onDone(clean)
            }, enabled = clean.isNotEmpty(), modifier = Modifier.testTag("start"))
        }
    ) {
        Spacer(Modifier.weight(1f))
        Row(Modifier.align(Alignment.CenterHorizontally)) {
            Text("🔴", fontSize = 56.sp)
            Text("🔵", fontSize = 56.sp)
        }
        Text(
            stringResource(R.string.app_name),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )
        Text(
            stringResource(R.string.welcome_desc),
            color = Polls.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 32.dp)
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(Limits.NAME_MAX) },
            label = { Text(stringResource(R.string.welcome_name)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { if (clean.isNotEmpty()) onDone(clean) }),
            modifier = Modifier.fillMaxWidth().testTag("name_input")
        )
        Text(
            stringResource(R.string.welcome_name_desc),
            style = MaterialTheme.typography.bodySmall,
            color = Polls.Muted,
            modifier = Modifier.padding(top = 6.dp)
        )
        Spacer(Modifier.weight(1.4f))
    }
}

@Composable
internal fun SettingsScreen(
    local: LocalState,
    onRename: (String) -> Unit,
    onDeleteData: () -> Unit,
    onBack: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    var renaming by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var showTerms by rememberSaveable { mutableStateOf(false) }
    var pickLanguage by rememberSaveable { mutableStateOf(false) }
    val accuracy = PredictionRecord(local.predictionHits, local.predictionsResolved).accuracy
    Page(title = stringResource(R.string.settings_title), onBack = onBack) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            SettingRow(stringResource(R.string.settings_name), local.name, "rename") { renaming = true }
            SettingRow(
                stringResource(R.string.settings_predictions),
                if (accuracy == null) {
                    stringResource(R.string.settings_predictions_none)
                } else {
                    stringResource(R.string.settings_predictions_value, accuracy, local.predictionsResolved)
                },
                "predictions",
                onClick = null
            )
            if (canPickAppLanguage) {
                SettingRow(stringResource(CoreR.string.lillo_language), currentAppLanguageName(), "language") {
                    pickLanguage =
                        true
                }
            }
            SettingRow(stringResource(R.string.terms_title), null, "terms") { showTerms = true }
            SettingRow(stringResource(R.string.settings_privacy), null, "privacy") {
                uriHandler.openUri(PRIVACY_POLICY_URL)
            }
            SettingRow(stringResource(R.string.settings_rate), null, "rate") { uriHandler.openUri(PLAY_STORE_URL) }
            SettingRow(
                stringResource(R.string.settings_delete),
                stringResource(R.string.settings_delete_desc),
                "delete_data"
            ) {
                confirmDelete =
                    true
            }
            Text(
                stringResource(R.string.settings_about, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = Polls.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
            )
        }
    }
    if (pickLanguage && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        LilloLanguageDialog(onDismiss = { pickLanguage = false })
    }
    if (renaming) {
        var name by rememberSaveable { mutableStateOf(local.name) }
        AlertDialog(
            modifier = DialogTags,
            onDismissRequest = { renaming = false },
            title = { Text(stringResource(R.string.settings_name)) },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = {
                        name = it.take(Limits.NAME_MAX)
                    }, singleLine = true, modifier = Modifier.testTag("rename_input"))
                    Text(
                        stringResource(R.string.settings_name_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = Polls.Muted,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    renaming = false
                    onRename(name)
                }, enabled = Drafts.cleanName(name).isNotEmpty(), modifier = Modifier.testTag("rename_save")) {
                    Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { renaming = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    if (confirmDelete) {
        AlertDialog(
            modifier = DialogTags,
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.settings_delete)) },
            text = { Text(stringResource(R.string.delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDeleteData()
                }, modifier = Modifier.testTag("confirm_delete")) {
                    Text(
                        stringResource(R.string.delete_confirm_button),
                        color = Polls.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
    if (showTerms) {
        AlertDialog(
            modifier = DialogTags,
            onDismissRequest = { showTerms = false },
            title = { Text(stringResource(R.string.terms_title)) },
            text = { Text(stringResource(R.string.terms_body)) },
            confirmButton = { TextButton(onClick = { showTerms = false }) { Text(stringResource(R.string.ok)) } }
        )
    }
}

@Composable
private fun SettingRow(title: String, value: String?, tag: String, onClick: (() -> Unit)?) = LilloSettingRow(
    title,
    modifier = Modifier.padding(bottom = 8.dp),
    subtitle = value,
    borderColor = MaterialTheme.colorScheme.outline,
    tag = tag,
    onClick = onClick
)
