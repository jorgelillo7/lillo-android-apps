package com.jorgelillo.tournaments.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.ui.PendingReplace

/**
 * Asks before a link's newer copy of a tournament replaces the one on this phone: revisions are
 * counted on each phone, so "newer" can still miss results entered only here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReplaceDialog(pending: PendingReplace, onReplace: () -> Unit, onKeep: () -> Unit) =
    BasicAlertDialog(onDismissRequest = onKeep, modifier = TestTagsAsResourceIds) {
        ReplaceCard(pending, onReplace, onKeep)
    }

/** The dialog's content, on its own so a screenshot test can show it. */
@Composable
internal fun ReplaceCard(pending: PendingReplace, onReplace: () -> Unit, onKeep: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("replace_dialog")
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                stringResource(R.string.replace_title, pending.here.name),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                stringResource(R.string.replace_played, pending.incoming.playedMatches, pending.here.playedMatches),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                stringResource(R.string.replace_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                TextButton(onClick = onKeep, modifier = Modifier.testTag("replace_keep")) {
                    Text(stringResource(R.string.replace_keep))
                }
                Button(onClick = onReplace, modifier = Modifier.testTag("replace_confirm")) {
                    Text(stringResource(R.string.replace_confirm))
                }
            }
        }
    }
}
