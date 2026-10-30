package com.jorgelillo.whoslying.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import com.jorgelillo.core.designsystem.LilloSectionTitle
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.core.designsystem.accentEdge
import com.jorgelillo.core.platform.shareText
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.domain.PackShare
import com.jorgelillo.whoslying.domain.WordPack
import com.jorgelillo.whoslying.domain.WordPacks
import com.jorgelillo.whoslying.ui.theme.Neon

/**
 * Multi-select grid of packs for the next game. [selected] empty means every pack. The choice is
 * kept here and handed to [onDone] when leaving (null: nothing chosen, keep the previous choice), so
 * "clear all" can leave nothing ticked while you pick. A long press picks only that pack.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PackPickerScreen(
    packs: List<WordPack>,
    selected: Set<String>,
    onNewPack: (Set<String>?) -> Unit,
    onDone: (Set<String>?) -> Unit
) {
    val ids = packs.map { it.id }
    var chosen by rememberSaveable { mutableStateOf(selected.ifEmpty { ids.toSet() }.toList()) }
    val all = chosen.containsAll(ids)
    // Every pack is the empty set (it also takes in packs made later); none keeps what there was.
    val result = {
        if (chosen.isEmpty()) {
            null
        } else if (all) {
            emptySet()
        } else {
            chosen.toSet()
        }
    }
    BackHandler { onDone(result()) }
    Page(
        title = stringResource(R.string.pick_packs_title),
        onBack = { onDone(result()) },
        bottom = {
            BigButton(
                stringResource(R.string.continue_),
                { onDone(result()) },
                Modifier.testTag("packs_done"),
                enabled = chosen.isNotEmpty()
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                when {
                    all -> stringResource(R.string.pick_packs_all)
                    chosen.isEmpty() -> stringResource(R.string.pick_packs_none)
                    else -> pluralStringResource(R.plurals.pick_packs_some, chosen.size, chosen.size)
                },
                color = if (chosen.isEmpty()) Neon.AmberText else Neon.Muted,
                modifier = Modifier.weight(1f)
            )
            if (all) {
                TextButton(onClick = { chosen = emptyList() }, modifier = Modifier.testTag("clear_packs")) {
                    Text(stringResource(R.string.pick_packs_clear), color = Neon.TurquoiseText)
                }
            } else {
                TextButton(onClick = { chosen = ids }, modifier = Modifier.testTag("select_all_packs")) {
                    Text(stringResource(R.string.pick_packs_select_all), color = Neon.TurquoiseText)
                }
            }
        }
        Text(
            stringResource(R.string.pick_packs_hint),
            style = MaterialTheme.typography.bodySmall,
            color = Neon.Muted
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            items(packs, key = { it.id }) { pack ->
                PackTile(
                    pack,
                    checked = pack.id in chosen,
                    onClick = {
                        chosen = if (pack.id in chosen) chosen - pack.id else chosen + pack.id
                    },
                    onLongClick = { chosen = listOf(pack.id) }
                )
            }
            item(key = "new") {
                Column(
                    Modifier
                        .clip(MaterialTheme.shapes.large)
                        .border(2.dp, Neon.Card, MaterialTheme.shapes.large)
                        .clickable { onNewPack(result()) }
                        .padding(vertical = 22.dp)
                        .testTag("picker_new_pack"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("＋", style = MaterialTheme.typography.displaySmall, color = Neon.TurquoiseText)
                    Text(
                        stringResource(R.string.packs_new),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PackTile(pack: WordPack, checked: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    Box(
        Modifier
            .clip(MaterialTheme.shapes.large)
            .background(if (checked) Neon.Violet.copy(alpha = 0.25f) else Neon.Card)
            .border(2.dp, if (checked) Neon.Violet else Color.Transparent, MaterialTheme.shapes.large)
            .combinedClickable(
                onLongClickLabel = stringResource(R.string.pick_packs_only),
                onLongClick = onLongClick,
                onClick = onClick
            )
            .testTag("pack_${pack.id}")
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 18.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(pack.emoji, fontSize = 44.sp)
            Text(
                pack.name,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                pluralStringResource(R.plurals.pack_words, pack.entries.size, pack.entries.size),
                style = MaterialTheme.typography.bodySmall,
                // On a ticked (violet) tile the muted grey falls short of 4.5:1.
                color = if (checked) Neon.Ink.copy(alpha = 0.85f) else Neon.Muted
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
            contentAlignment = Alignment.Center
        ) {
            if (checked) Text("✓", color = Neon.Night, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
internal fun PacksScreen(
    builtIn: List<WordPack>,
    custom: List<WordPack>,
    onEdit: (String?) -> Unit,
    onCopy: (WordPack) -> Unit,
    onImport: (String) -> Unit,
    onBack: () -> Unit
) {
    var sharing by remember { mutableStateOf<WordPack?>(null) }
    var copying by remember { mutableStateOf<WordPack?>(null) }
    var importing by rememberSaveable { mutableStateOf(false) }
    Page(
        title = stringResource(R.string.packs),
        onBack = onBack,
        actions = {
            TextButton(onClick = { importing = true }, modifier = Modifier.testTag("import_pack")) {
                Text(stringResource(R.string.pack_import), color = Neon.TurquoiseText)
            }
        },
        bottom = {
            BigButton("＋ " + stringResource(R.string.packs_new), { onEdit(null) }, Modifier.testTag("new_pack"))
        }
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (custom.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.packs_custom)) }
                items(custom, key = { it.id }) { PackRow(it, onClick = { onEdit(it.id) }, onShare = { sharing = it }) }
            }
            item { SectionTitle(stringResource(R.string.packs_builtin)) }
            items(builtIn, key = { it.id }) { PackRow(it, onClick = { copying = it }) }
        }
    }
    sharing?.let { PackShareSheet(it, onDismiss = { sharing = null }) }
    copying?.let { pack ->
        // Built-in packs ship with the app (and change with updates): edit a copy of your own.
        AlertDialog(
            onDismissRequest = { copying = null },
            modifier = TestTagsAsResourceIds,
            title = { Text(stringResource(R.string.pack_copy_title, pack.name)) },
            text = { Text(stringResource(R.string.pack_copy_body)) },
            confirmButton = {
                TextButton(onClick = {
                    copying = null
                    onCopy(pack)
                }, modifier = Modifier.testTag("copy_pack_ok")) {
                    Text(stringResource(R.string.pack_copy_ok))
                }
            },
            dismissButton = { TextButton(onClick = { copying = null }) { Text(stringResource(R.string.vote_cancel)) } }
        )
    }
    if (importing) {
        PackImportDialog(onImport = {
            onImport(it)
            importing = false
        }, onDismiss = { importing = false })
    }
}

@Composable
private fun SectionTitle(text: String) =
    LilloSectionTitle(text, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))

@Composable
private fun PackRow(pack: WordPack, onClick: (() -> Unit)?, onShare: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Neon.Card)
            // The group's own packs stand apart from the built-in ones.
            .then(if (pack.custom) Modifier.accentEdge(Neon.Pink) else Modifier)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(16.dp)
            .testTag("pack_${pack.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(pack.emoji, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(pack.name, fontWeight = FontWeight.Bold)
            Text(
                pluralStringResource(R.plurals.pack_words, pack.entries.size, pack.entries.size),
                style = MaterialTheme.typography.bodySmall,
                color = Neon.Muted
            )
        }
        if (onShare != null) {
            IconButton(onClick = onShare, modifier = Modifier.testTag("share_pack_${pack.id}")) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = stringResource(R.string.pack_share),
                    tint = Neon.Turquoise
                )
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
            TestTagsAsResourceIds.verticalScroll(
                rememberScrollState()
            ).padding(horizontal = 20.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "${pack.emoji} ${pack.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            if (link.length <= PackShare.QR_MAX_CHARS) {
                LilloQrCode(link, Modifier.testTag("pack_qr"))
                Text(stringResource(R.string.pack_share_qr_help), color = Neon.Muted, textAlign = TextAlign.Center)
            }
            BigButton(stringResource(R.string.pack_share_send), {
                context.shareText(message)
            }, Modifier.testTag("pack_send"))
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
                    modifier = Modifier.fillMaxWidth().testTag("pack_import_text")
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onImport(text)
            }, enabled = text.isNotBlank(), modifier = Modifier.testTag("pack_import_ok")) {
                Text(stringResource(R.string.pack_import))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.vote_cancel)) } }
    )
}

/** Editor for a custom pack: one entry per line, "word / similar word" with the second part optional. */
@Composable
internal fun PackEditScreen(
    initial: WordPack,
    isNew: Boolean,
    onSave: (WordPack) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
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
                    Text(stringResource(R.string.pack_delete), color = Neon.PinkText)
                }
            }
        }
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.pack_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("pack_name")
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(stringResource(R.string.pack_words_label)) },
            modifier = Modifier.fillMaxWidth().weight(1f).testTag("pack_words")
        )
        Text(
            if (valid) stringResource(R.string.pack_words_help) else stringResource(R.string.pack_need),
            style = MaterialTheme.typography.bodySmall,
            color = if (valid) Neon.Muted else Neon.PinkText,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
