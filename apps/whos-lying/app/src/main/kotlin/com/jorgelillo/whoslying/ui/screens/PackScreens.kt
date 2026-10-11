package com.jorgelillo.whoslying.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.LilloQrCode
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.core.platform.shareText
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.domain.PackShare
import com.jorgelillo.whoslying.domain.WordPack
import com.jorgelillo.whoslying.domain.WordPacks
import com.jorgelillo.whoslying.ui.theme.Neon

/** Multi-select grid of packs for the next game. An empty [selected] set means every pack. */
@Composable
fun PackPickerScreen(
    packs: List<WordPack>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onSelectAll: () -> Unit,
    onNewPack: () -> Unit,
    onDone: () -> Unit,
) {
    val all = selected.isEmpty()
    Page(
        title = stringResource(R.string.pick_packs_title),
        onBack = onDone,
        bottom = { BigButton(stringResource(R.string.continue_), onDone, Modifier.testTag("packs_done")) },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (all) stringResource(R.string.pick_packs_all) else pluralStringResource(R.plurals.pick_packs_some, selected.size, selected.size),
                color = Neon.Muted,
                modifier = Modifier.weight(1f),
            )
            if (!all) {
                TextButton(onClick = onSelectAll, modifier = Modifier.testTag("select_all_packs")) {
                    Text(stringResource(R.string.pick_packs_select_all), color = Neon.Turquoise)
                }
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 8.dp),
        ) {
            items(packs, key = { it.id }) { pack ->
                PackTile(pack, checked = all || pack.id in selected, onClick = { onToggle(pack.id) })
            }
            item(key = "new") {
                Column(
                    Modifier
                        .clip(MaterialTheme.shapes.large)
                        .border(2.dp, Neon.Card, MaterialTheme.shapes.large)
                        .clickable(onClick = onNewPack)
                        .padding(vertical = 22.dp)
                        .testTag("picker_new_pack"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("＋", style = MaterialTheme.typography.displaySmall, color = Neon.Turquoise)
                    Text(stringResource(R.string.packs_new), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun PackTile(pack: WordPack, checked: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(MaterialTheme.shapes.large)
            .background(if (checked) Neon.Violet.copy(alpha = 0.25f) else Neon.Card)
            .border(2.dp, if (checked) Neon.Violet else Color.Transparent, MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .testTag("pack_${pack.id}"),
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = 18.dp, horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(pack.emoji, fontSize = 44.sp)
            Text(pack.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, modifier = Modifier.padding(top = 8.dp))
            Text(
                pluralStringResource(R.plurals.pack_words, pack.entries.size, pack.entries.size),
                style = MaterialTheme.typography.bodySmall,
                color = Neon.Muted,
            )
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) Neon.Turquoise else Color.Transparent)
                .border(2.dp, if (checked) Neon.Turquoise else Neon.Muted, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Text("✓", color = Neon.Night, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun PacksScreen(
    builtIn: List<WordPack>,
    custom: List<WordPack>,
    onEdit: (String?) -> Unit,
    onImport: (String) -> Unit,
    onBack: () -> Unit,
) {
    var sharing by remember { mutableStateOf<WordPack?>(null) }
    var importing by rememberSaveable { mutableStateOf(false) }
    Page(
        title = stringResource(R.string.packs),
        onBack = onBack,
        actions = {
            TextButton(onClick = { importing = true }, modifier = Modifier.testTag("import_pack")) {
                Text(stringResource(R.string.pack_import), color = Neon.Turquoise)
            }
        },
        bottom = { BigButton("＋ " + stringResource(R.string.packs_new), { onEdit(null) }, Modifier.testTag("new_pack")) },
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (custom.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.packs_custom)) }
                items(custom, key = { it.id }) { PackRow(it, onClick = { onEdit(it.id) }, onShare = { sharing = it }) }
            }
            item { SectionTitle(stringResource(R.string.packs_builtin)) }
            items(builtIn, key = { it.id }) { PackRow(it, onClick = null) }
        }
    }
    sharing?.let { PackShareSheet(it, onDismiss = { sharing = null }) }
    if (importing) PackImportDialog(onImport = { onImport(it); importing = false }, onDismiss = { importing = false })
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Neon.Muted, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
}

@Composable
private fun PackRow(pack: WordPack, onClick: (() -> Unit)?, onShare: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Neon.Card)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(pack.emoji, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(pack.name, fontWeight = FontWeight.Bold)
            Text(
                pluralStringResource(R.plurals.pack_words, pack.entries.size, pack.entries.size),
                style = MaterialTheme.typography.bodySmall,
                color = Neon.Muted,
            )
        }
        if (onShare != null) {
            IconButton(onClick = onShare, modifier = Modifier.testTag("share_pack_${pack.id}")) {
                Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.pack_share), tint = Neon.Turquoise)
            }
        }
        if (onClick != null) Icon(Icons.Filled.Edit, contentDescription = null, tint = Neon.Muted)
    }
}

/** Pass a pack to another phone: QR code for the camera, or the link through any chat. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackShareSheet(pack: WordPack, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val link = remember(pack) { PackShare.link(pack) }
    val message = stringResource(R.string.pack_share_message, pack.emoji, pack.name, link)
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Neon.Card) {
        Column(
            TestTagsAsResourceIds.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("${pack.emoji} ${pack.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            if (link.length <= PackShare.QR_MAX_CHARS) {
                LilloQrCode(link, Modifier.testTag("pack_qr"))
                Text(stringResource(R.string.pack_share_qr_help), color = Neon.Muted, textAlign = TextAlign.Center)
            }
            BigButton(stringResource(R.string.pack_share_send), { context.shareText(message) }, Modifier.testTag("pack_send"))
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** Paste a pack link (or the whole chat message) someone sent you. */
@Composable
private fun PackImportDialog(onImport: (String) -> Unit, onDismiss: () -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = TestTagsAsResourceIds,
        title = { Text(stringResource(R.string.pack_import_title)) },
        text = {
            Column {
                Text(stringResource(R.string.pack_import_body), color = Neon.Muted)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("https://jorgelillo7.github.io/p/#…") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth().testTag("pack_import_text"),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onImport(text) }, enabled = text.isNotBlank(), modifier = Modifier.testTag("pack_import_ok")) {
                Text(stringResource(R.string.pack_import))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.vote_cancel)) } },
    )
}

/** Editor for a custom pack: one entry per line, "word / similar word" with the second part optional. */
@Composable
fun PackEditScreen(initial: WordPack, isNew: Boolean, onSave: (WordPack) -> Unit, onDelete: () -> Unit, onBack: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var text by rememberSaveable {
        mutableStateOf(initial.entries.joinToString("\n", transform = WordPacks::toLine))
    }
    val entries = WordPacks.parseEntries(text)
    val valid = name.isNotBlank() && entries.size >= 3

    Page(
        title = stringResource(if (isNew) R.string.packs_new else R.string.packs),
        onBack = onBack,
        modifier = Modifier.imePadding(),
        bottom = {
            BigButton(stringResource(R.string.pack_save), {
                onSave(initial.copy(name = name.trim(), entries = entries))
            }, enabled = valid, modifier = Modifier.testTag("save_pack"))
            if (!isNew) {
                TextButton(onClick = onDelete, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(R.string.pack_delete), color = Neon.Pink)
                }
            }
        },
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.pack_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("pack_name"),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.pack_words_label)) },
            modifier = Modifier.fillMaxWidth().weight(1f).testTag("pack_words"),
        )
        Text(
            if (valid) stringResource(R.string.pack_words_help) else stringResource(R.string.pack_need),
            style = MaterialTheme.typography.bodySmall,
            color = if (valid) Neon.Muted else Neon.Pink,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
