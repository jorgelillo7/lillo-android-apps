package com.jorgelillo.leaguecards.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jorgelillo.leaguecards.R
import com.jorgelillo.leaguecards.domain.League
import com.jorgelillo.leaguecards.domain.Report
import com.jorgelillo.leaguecards.domain.YOU
import com.jorgelillo.leaguecards.ui.theme.Chat
import kotlinx.coroutines.delay

/**
 * After every matchday, the league's group chat: your points, the day's events as yellow
 * notices, then the members' messages arriving one by one.
 */
@Composable
fun ReportScreen(league: League, report: Report, unlocked: List<String>, seasonOver: Boolean, onNext: () -> Unit, onTable: () -> Unit, onBack: () -> Unit) {
    val ranked = report.scores.entries.sortedByDescending { it.value }
    val dayPosition = ranked.indexOfFirst { it.key == YOU } + 1
    val top = ranked.first()
    val messages = buildList<@Composable () -> Unit> {
        add { SystemNotice(stringResource(R.string.report_points, report.matchday, report.yourPoints, dayPosition, ranked.size)) }
        if (top.key != YOU) add { SystemNotice(stringResource(R.string.report_top, league.nameOf(top.key), top.value)) }
        report.events.forEach { e -> add { SystemNotice(e.text) } }
        unlocked.forEach { id -> league.achievement(id)?.let { a -> add { AchievementBanner(a) } } }
        report.chat.forEachIndexed { i, line -> add { ChatBubble(league, line.character, line.text, time = "21:%02d".format(10 + i * 3)) } }
    }
    var shown by remember(report.matchday) { mutableIntStateOf(1) }
    LaunchedEffect(report.matchday) {
        while (shown < messages.size) {
            delay(700)
            shown++
        }
    }
    Page(
        title = league.name,
        onBack = onBack,
        sideActionsWhenShort = true,
        actions = {
            androidx.compose.material3.TextButton(onClick = onTable, modifier = Modifier.testTag("table")) {
                Text(stringResource(R.string.table), color = Chat.Text)
            }
        },
        bottom = {
            BigButton(stringResource(if (seasonOver) R.string.see_ending else R.string.next_matchday), onNext, "next")
        }
    ) {
        Text(
            stringResource(R.string.report_title, report.matchday),
            color = Chat.Muted,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.fillMaxWidth()
        )
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).testTag("chat"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            messages.forEachIndexed { i, message ->
                AnimatedVisibility(visible = i < shown, enter = fadeIn() + slideInVertically { it / 2 }) { message() }
            }
        }
    }
}
