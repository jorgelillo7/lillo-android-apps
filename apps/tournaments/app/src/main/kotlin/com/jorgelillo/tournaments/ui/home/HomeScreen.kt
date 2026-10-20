package com.jorgelillo.tournaments.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.AboutTexts
import com.jorgelillo.core.designsystem.LilloAboutSheet
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.core.platform.LilloLinks
import com.jorgelillo.tournaments.BuildConfig
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.domain.Format
import com.jorgelillo.tournaments.domain.Library
import com.jorgelillo.tournaments.domain.Tournament
import com.jorgelillo.tournaments.domain.Tournaments
import com.jorgelillo.tournaments.ui.BigButton
import com.jorgelillo.tournaments.ui.Page
import com.jorgelillo.tournaments.ui.formatDay
import com.jorgelillo.tournaments.ui.theme.Arena

@Composable
fun HomeScreen(
    library: Library,
    onOpen: (Tournament) -> Unit,
    onNew: () -> Unit,
    onImport: () -> Unit,
    onStats: () -> Unit,
    onAbout: () -> Unit,
) {
    val short = isShortScreen()
    Page(
        title = stringResource(R.string.app_name),
        onBack = null,
        actions = {
            TextButton(onClick = onImport, modifier = Modifier.testTag("import")) { Text(stringResource(R.string.action_import)) }
            if (library.tournaments.isNotEmpty()) {
                IconButton(onClick = onStats, modifier = Modifier.testTag("stats")) {
                    Icon(Icons.Filled.Star, contentDescription = stringResource(R.string.stats_title))
                }
            }
            IconButton(onClick = onAbout, modifier = Modifier.testTag("about")) {
                Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.action_about))
            }
        },
        bottom = { if (!short) BigButton(stringResource(R.string.new_tournament), onNew, "new") },
    ) {
        if (library.tournaments.isEmpty()) {
            Column(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("🏆", fontSize = 64.sp)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.home_empty_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.home_empty_body), color = Arena.Muted, textAlign = TextAlign.Center)
                if (short) {
                    Spacer(Modifier.height(16.dp))
                    BigButton(stringResource(R.string.new_tournament), onNew, "new")
                }
            }
            return@Page
        }
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (short) item { BigButton(stringResource(R.string.new_tournament), onNew, "new") }
            items(library.sorted, key = { it.id }) { TournamentCard(it, onClick = { onOpen(it) }) }
            item { Spacer(Modifier.height(4.dp)) }
        }
    }
}

@Composable
private fun TournamentCard(t: Tournament, onClick: () -> Unit) {
    val champion = t.player(t.champion)
    Row(
        Modifier
            .fillMaxWidth()
            .background(Arena.Raised, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag("tournament_${t.id}"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).background(if (champion != null) Arena.Gold else Arena.High, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Text(if (champion != null) "🏆" else if (t.format == Format.SWISS) "♟" else "⚔", fontSize = 22.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(t.name, fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                listOf(t.game, formatDay(t.epochDay), pluralStringResource(R.plurals.players_count, t.players.size, t.players.size))
                    .filter { it.isNotBlank() }.joinToString(" · "),
                color = Arena.Muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            val pending = Tournaments.pending(t).size
            Text(
                when {
                    champion != null -> stringResource(R.string.status_champion, champion.name)
                    pending > 0 -> pluralStringResource(R.plurals.status_pending, pending, pending)
                    else -> stringResource(R.string.status_next_round)
                },
                color = if (champion != null) Arena.Gold else Arena.Teal,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** Paste a link (or a whole chat message with one) from another phone. */
@Composable
fun ImportDialog(onImport: (String) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = TestTagsAsResourceIds,
        title = { Text(stringResource(R.string.import_title)) },
        text = {
            Column {
                Text(stringResource(R.string.import_body), color = Arena.Muted)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("https://jorgelillo7.github.io/t/#…") },
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth().testTag("import_text"),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onImport(text) }, enabled = text.isNotBlank(), modifier = Modifier.testTag("import_ok")) {
                Text(stringResource(R.string.action_import))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
fun AboutSheet(onDismiss: () -> Unit) = LilloAboutSheet(
    icon = painterResource(R.drawable.ic_launcher_foreground),
    iconBackground = Arena.Ink,
    containerColor = Arena.Raised,
    texts = AboutTexts(
        title = stringResource(R.string.app_name),
        version = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
        body = stringResource(R.string.about_body),
        rate = null,
        privacy = stringResource(R.string.about_open_privacy),
    ),
    privacyUrl = LilloLinks.privacyPolicy("tournaments"),
    playStoreUrl = null, // not on Google Play yet
    onDismiss = onDismiss,
)
