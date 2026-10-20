package com.jorgelillo.whoslying.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.ui.theme.Neon

/**
 * A friendly note on first launch: made for friends, no ads, no profit, tested as far as two
 * beta testers could go, feedback welcome. "Don't show again" (ticked by default) remembers it;
 * Settings → About this app shows it again.
 */
@Composable
fun WelcomeDialog(showCheckbox: Boolean, onDismiss: (dontShowAgain: Boolean) -> Unit) {
    val uriHandler = LocalUriHandler.current
    var dontShowAgain by rememberSaveable { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = { onDismiss(dontShowAgain) },
        modifier = TestTagsAsResourceIds,
        containerColor = Neon.Card,
        icon = { Text("🕵️", style = androidx.compose.material3.MaterialTheme.typography.displaySmall) },
        title = { Text(stringResource(R.string.welcome_title), fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.welcome_friends))
                Text(stringResource(R.string.welcome_free))
                Text(stringResource(R.string.welcome_testers))
                if (showCheckbox) {
                    Row(
                        Modifier.fillMaxWidth().clickable { dontShowAgain = !dontShowAgain }.padding(top = 4.dp).testTag("welcome_dont_show"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = dontShowAgain,
                            onCheckedChange = { dontShowAgain = it },
                            colors = CheckboxDefaults.colors(checkedColor = Neon.Turquoise)
                        )
                        Text(stringResource(R.string.welcome_dont_show), color = Neon.Muted)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onDismiss(dontShowAgain) }, modifier = Modifier.testTag("welcome_ok")) {
                Text(stringResource(R.string.welcome_ok), fontWeight = FontWeight.Bold, color = Neon.Turquoise)
            }
        },
        dismissButton = {
            TextButton(onClick = { uriHandler.openUri(PLAY_STORE_URL) }, modifier = Modifier.testTag("welcome_feedback")) {
                Text(stringResource(R.string.welcome_feedback), color = Neon.Muted)
            }
        }
    )
}
