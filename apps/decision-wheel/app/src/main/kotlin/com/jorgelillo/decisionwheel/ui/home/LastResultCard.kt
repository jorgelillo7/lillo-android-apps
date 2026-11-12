package com.jorgelillo.decisionwheel.ui.home

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.LilloColors
import com.jorgelillo.core.designsystem.LilloSecondaryButton
import com.jorgelillo.decisionwheel.R
import com.jorgelillo.decisionwheel.domain.AppState
import com.jorgelillo.decisionwheel.domain.Wheel

/**
 * The last decision, large, with its wheel and day and a way back to that wheel; with no decision
 * yet, an invitation to spin the favourite. Nothing when there is no wheel to offer.
 */
@Composable
internal fun LastResultCard(state: AppState, onOpen: (Wheel) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val last = state.lastResult
    val wheel = last?.second ?: state.firstSpinWheel.takeIf { state.history.isEmpty() } ?: return
    val label: String
    val headline: String
    val detail: String
    val action: String
    if (last != null) {
        val day = DateUtils.formatDateTime(
            context,
            last.first.atEpochMillis,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_NO_YEAR
        )
        label = stringResource(R.string.last_result_label)
        headline = last.first.option
        detail = stringResource(R.string.last_result_detail, wheel.name, day)
        action = stringResource(R.string.last_result_again)
    } else {
        label = stringResource(R.string.first_spin_label)
        headline = stringResource(R.string.first_spin_title)
        detail = stringResource(R.string.first_spin_detail, wheel.name)
        action = stringResource(R.string.first_spin_action)
    }
    Column(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(20.dp)
            .testTag("last_result")
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            headline,
            style = MaterialTheme.typography.displaySmall,
            color = LilloColors.current.accentText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            autoSize = TextAutoSize.StepBased(minFontSize = 22.sp, maxFontSize = 36.sp),
            modifier = Modifier.testTag("last_result_option")
        )
        Text(
            detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(14.dp))
        LilloSecondaryButton(action, onClick = { onOpen(wheel) }, modifier = Modifier.testTag("spin_again"))
    }
}
