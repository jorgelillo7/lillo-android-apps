package com.jorgelillo.tournaments.ui.tournament

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.LilloQrCode
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.core.platform.shareText
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.domain.Share
import com.jorgelillo.tournaments.domain.Tournament
import com.jorgelillo.tournaments.ui.BigButton
import com.jorgelillo.tournaments.ui.theme.Arena

/**
 * Hand the tournament to another phone: a QR code the other phone's camera opens, or the link
 * through any chat. The whole tournament travels inside the link; nothing is uploaded.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareSheet(t: Tournament, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val link = remember(t) { Share.link(t) }
    val message = stringResource(R.string.share_message, t.name, link)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = Arena.Raised) {
        Column(
            TestTagsAsResourceIds.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.share_title), fontSize = 22.sp, fontWeight = FontWeight.Black)
            if (link.length <= Share.QR_MAX_CHARS) {
                LilloQrCode(link, Modifier.testTag("qr"))
                Text(stringResource(R.string.share_qr_help), color = Arena.Muted, textAlign = TextAlign.Center, fontSize = 14.sp)
            }
            BigButton(stringResource(R.string.share_send_link), { context.shareText(message) }, "send_link")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { shareImage(context, t) }, modifier = Modifier.weight(1f).testTag("share_image")) {
                    Text(stringResource(R.string.export_image_short))
                }
                OutlinedButton(onClick = { shareCsv(context, t) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.export_csv_short))
                }
            }
            Text(stringResource(R.string.share_snapshot_note), color = Arena.Muted, textAlign = TextAlign.Center, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
        }
    }
}

