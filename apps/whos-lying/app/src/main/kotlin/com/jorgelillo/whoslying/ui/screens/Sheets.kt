package com.jorgelillo.whoslying.ui.screens

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.LilloLanguageDialog
import com.jorgelillo.core.designsystem.LilloSettingRow
import com.jorgelillo.core.designsystem.R as CoreR
import com.jorgelillo.core.designsystem.canPickAppLanguage
import com.jorgelillo.core.designsystem.currentAppLanguageName
import com.jorgelillo.core.platform.LilloLinks
import com.jorgelillo.core.platform.shareText
import com.jorgelillo.whoslying.BuildConfig
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.ui.WordReport
import com.jorgelillo.whoslying.ui.theme.Neon

val PRIVACY_POLICY_URL = LilloLinks.privacyPolicy("whos-lying")
val PLAY_STORE_URL = LilloLinks.playStore(BuildConfig.APPLICATION_ID)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HowToSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Neon.Card) {
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            listOf(
                Triple("🤫", R.string.how_1_title, R.string.how_1),
                Triple("💬", R.string.how_2_title, R.string.how_2),
                Triple("🗳️", R.string.how_3_title, R.string.how_3),
                Triple("🏆", R.string.how_4_title, R.string.how_4)
            ).forEach { (emoji, title, body) ->
                Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(Neon.Night).padding(18.dp)) {
                    Text(
                        "$emoji  " + stringResource(title),
                        color = Neon.AmberText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(stringResource(body), modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

/** Detailed guide to each game mode and the options that change a game. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ModesSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Neon.Card) {
        Column(
            Modifier.verticalScroll(
                rememberScrollState()
            ).padding(horizontal = 24.dp).padding(bottom = 32.dp).then(DialogTags).testTag("modes_sheet"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                stringResource(R.string.modes_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Text(stringResource(R.string.modes_intro), color = Neon.Muted)
            listOf(
                ModeGuide(
                    "🎭",
                    R.string.mode_classic,
                    R.string.modes_classic_who,
                    R.string.modes_classic_win,
                    R.string.modes_classic_tip
                ),
                ModeGuide(
                    "🙈",
                    R.string.mode_blind,
                    R.string.modes_blind_who,
                    R.string.modes_blind_win,
                    R.string.modes_blind_tip
                ),
                ModeGuide(
                    "🤷",
                    R.string.mode_drifter,
                    R.string.modes_drifter_who,
                    R.string.modes_drifter_win,
                    R.string.modes_drifter_tip
                )
            ).forEach { mode ->
                Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(Neon.Night).padding(18.dp)) {
                    Text(
                        "${mode.emoji}  " + stringResource(mode.title),
                        color = Neon.AmberText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    listOf(
                        R.string.modes_who_gets to mode.who,
                        R.string.modes_how_win to mode.win,
                        R.string.modes_tip to mode.tip
                    ).forEach { (label, body) ->
                        Text(
                            stringResource(label),
                            color = Neon.TurquoiseText,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Text(stringResource(body), modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
            listOf(
                Triple("🎨", R.string.modes_drawing_title, R.string.modes_drawing),
                Triple("🏆", R.string.modes_scores_title, R.string.modes_scores),
                Triple("🌀", R.string.modes_chaos_title, R.string.modes_chaos),
                Triple("💡", R.string.modes_hint_title, R.string.modes_hint),
                Triple("⏱️", R.string.modes_timer_title, R.string.modes_timer)
            ).forEach { (emoji, title, body) ->
                Column(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(Neon.Night).padding(18.dp)) {
                    Text(
                        "$emoji  " + stringResource(title),
                        color = Neon.AmberText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(stringResource(body), modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

private class ModeGuide(
    val emoji: String,
    @param:StringRes val title: Int,
    @param:StringRes val who: Int,
    @param:StringRes val win: Int,
    @param:StringRes val tip: Int
)

/** Lets the player say what's wrong with a word, then opens the prefilled report form. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReportSheet(word: String, decoy: String?, pack: String, language: String, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val reasons = listOf(
        R.string.report_offensive,
        R.string.report_typo,
        R.string.report_not_similar,
        R.string.report_difficulty,
        R.string.report_other
    )
        .map { stringResource(it) }
    var chosen by rememberSaveable { mutableStateOf(listOf<String>()) }
    var comment by rememberSaveable { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Neon.Card) {
        Column(
            Modifier.verticalScroll(
                rememberScrollState()
            ).padding(horizontal = 24.dp).padding(bottom = 32.dp).then(DialogTags).testTag("report_sheet"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "⚠️  " + stringResource(R.string.report_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            Text(stringResource(R.string.report_word, word), color = Neon.AmberText, fontWeight = FontWeight.Bold)
            if (decoy !=
                null
            ) {
                Text(stringResource(R.string.report_decoy, decoy), color = Neon.AmberText, fontWeight = FontWeight.Bold)
            }
            Text(stringResource(R.string.report_pick), color = Neon.Muted, style = MaterialTheme.typography.bodySmall)
            reasons.forEachIndexed { i, reason ->
                val selected = reason in chosen
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(Neon.Night)
                        .clickable { chosen = if (selected) chosen - reason else chosen + reason }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("reason_$i"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(reason, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text(if (selected) "✅" else "⚪")
                }
            }
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text(stringResource(R.string.report_comment)) },
                modifier = Modifier.fillMaxWidth().testTag("report_comment")
            )
            Text(
                stringResource(R.string.report_privacy),
                color = Neon.Muted,
                style = MaterialTheme.typography.bodySmall
            )
            BigButton(
                stringResource(R.string.report_send),
                {
                    uriHandler.openUri(WordReport.url(word, decoy, pack, language, chosen, comment).toString())
                    onDismiss()
                },
                Modifier.testTag("report_send"),
                enabled = chosen.isNotEmpty() || comment.isNotBlank()
            )
        }
    }
}

/** Settings and about: language, rules, sharing, rating and privacy. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSheet(onHowTo: () -> Unit, onModes: () -> Unit, onWelcome: () -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var pickLanguage by rememberSaveable { mutableStateOf(false) }
    val shareText = stringResource(R.string.share_text, PLAY_STORE_URL)
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Neon.Card) {
        Column(
            Modifier.verticalScroll(
                rememberScrollState()
            ).padding(horizontal = 24.dp).padding(bottom = 32.dp).then(DialogTags).testTag("settings_sheet"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black
            )
            if (canPickAppLanguage) {
                SettingsRow("🌐", stringResource(CoreR.string.lillo_language), currentAppLanguageName(), "language") {
                    pickLanguage =
                        true
                }
            }
            SettingsRow("📖", stringResource(R.string.how_to_play), null, "settings_how_to") {
                onDismiss()
                onHowTo()
            }
            SettingsRow("🎭", stringResource(R.string.modes_title), null, "settings_modes") {
                onDismiss()
                onModes()
            }
            SettingsRow(
                "💌",
                stringResource(R.string.settings_share),
                stringResource(R.string.settings_share_desc),
                "share"
            ) {
                context.shareText(shareText)
            }
            SettingsRow("🤝", stringResource(R.string.welcome_settings), null, "settings_welcome") {
                onDismiss()
                onWelcome()
            }
            SettingsRow("⭐", stringResource(R.string.about_rate), null, "rate") { uriHandler.openUri(PLAY_STORE_URL) }
            SettingsRow("🔒", stringResource(R.string.about_privacy), null, "privacy") {
                uriHandler.openUri(PRIVACY_POLICY_URL)
            }
            Text(
                stringResource(R.string.about_body) + "\n" +
                    stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = Neon.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )
        }
    }
    if (pickLanguage && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        LilloLanguageDialog(onDismiss = { pickLanguage = false })
    }
}

@Composable
private fun SettingsRow(emoji: String, title: String, subtitle: String?, tag: String, onClick: () -> Unit) =
    LilloSettingRow(
        title,
        // Darker than the sheet's surfaceContainer, so the rows stand out on it.
        containerColor = Neon.Night,
        subtitle = subtitle,
        leading = emoji,
        tag = tag,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        onClick = onClick
    ) { Text("›", style = MaterialTheme.typography.headlineSmall, color = Neon.Muted) }
