package com.jorgelillo.decisionwheel.ui.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.jorgelillo.decisionwheel.R
import com.jorgelillo.decisionwheel.domain.Wheel
import com.jorgelillo.decisionwheel.ui.theme.WheelColors
import com.jorgelillo.decisionwheel.ui.wheel.segmentColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    initial: Wheel,
    isNew: Boolean,
    onSave: (Wheel) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var options by rememberSaveable { mutableStateOf(initial.options) }
    var draft by rememberSaveable { mutableStateOf("") }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val cleanOptions = options.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    val valid = name.isNotBlank() && cleanOptions.size >= 2

    fun addDraft() {
        val text = draft.trim()
        if (text.isNotEmpty() && text !in options) options = options + text
        draft = ""
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isNew) R.string.edit_title_new else R.string.edit_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = { onSave(initial.copy(name = name.trim(), options = cleanOptions)) },
                        enabled = valid,
                        modifier = Modifier.testTag("save")
                    ) {
                        Text(stringResource(R.string.edit_save), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().padding(horizontal = 20.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.edit_name)) },
                singleLine = true,
                isError = name.isBlank(),
                supportingText = { if (name.isBlank()) Text(stringResource(R.string.edit_need_name)) },
                modifier = Modifier.fillMaxWidth().testTag("name")
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text(stringResource(R.string.edit_add_option)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addDraft() }),
                    modifier = Modifier.weight(1f).testTag("new_option")
                )
                Spacer(Modifier.padding(4.dp))
                Button(
                    onClick = { addDraft() },
                    enabled = draft.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = WheelColors.Teal, contentColor = WheelColors.Night)
                ) { Text(stringResource(R.string.edit_add)) }
            }
            if (cleanOptions.size < 2) {
                Text(stringResource(R.string.edit_need_two), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            val colors = segmentColors(options.size)
            LazyColumn(Modifier.weight(1f)) {
                itemsIndexed(options, key = { _, option -> option }) { i, option ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Canvas(Modifier.padding(end = 12.dp).height(12.dp).padding(0.dp)) {
                            drawCircle(colors[i], radius = 6.dp.toPx())
                        }
                        Text(option, Modifier.weight(1f).padding(start = 12.dp))
                        IconButton(onClick = { options = options - option }) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.edit_remove))
                        }
                    }
                }
                if (!isNew) {
                    item {
                        TextButton(onClick = { confirmDelete = true }, modifier = Modifier.padding(top = 16.dp)) {
                            Text(stringResource(R.string.edit_delete), color = WheelColors.Coral)
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            text = { Text(stringResource(R.string.edit_delete_confirm)) },
            confirmButton = { TextButton(onClick = onDelete) { Text(stringResource(R.string.edit_delete), color = WheelColors.Coral) } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.edit_cancel)) } }
        )
    }
}
