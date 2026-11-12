package com.jorgelillo.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Tells the user the app has a home-screen widget and how to put it there. When the launcher
 * accepts requests ([pinnable]) a button asks it; the steps to add it by hand from the launcher's
 * widget list are always shown, because some launchers (MIUI among them) refuse the request or
 * drop it silently. Test tags: widget_card, widget_add.
 */
@Composable
fun LilloWidgetCard(
    appName: String,
    pinnable: Boolean,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    /** Apps with larger text than Material's defaults pass their own sizes. */
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    bodyStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("widget_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.lillo_widget_title), style = titleStyle)
            if (pinnable) {
                OutlinedButton(onClick = onAdd, modifier = Modifier.fillMaxWidth().testTag("widget_add")) {
                    Text(stringResource(R.string.lillo_widget_add), style = bodyStyle)
                }
            }
            Text(
                stringResource(
                    if (pinnable) R.string.lillo_widget_by_hand_too else R.string.lillo_widget_by_hand,
                    appName
                ),
                style = bodyStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
