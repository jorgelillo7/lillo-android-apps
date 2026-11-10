package com.jorgelillo.stadiumevents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.LilloPage
import com.jorgelillo.stadiumevents.R
import com.jorgelillo.stadiumevents.domain.Level
import com.jorgelillo.stadiumevents.domain.Tomorrow
import com.jorgelillo.stadiumevents.domain.UpcomingDay
import com.jorgelillo.stadiumevents.ui.theme.JaleoPalette

/**
 * The whole app: one answer for tomorrow, the next busy days, and where the data comes from. Big
 * text on purpose: it is for people who never look at event calendars.
 */
@Composable
internal fun JaleoScreen(state: JaleoUiState?, palette: JaleoPalette, onAbout: () -> Unit, onRetry: () -> Unit) {
    val resources = LocalResources.current
    val texts = remember(resources, resources.configuration) { JaleoTexts(resources) }
    LilloPage(
        title = null,
        onBack = null,
        background = SolidColor(palette.background),
        contentColor = palette.text,
        readable = true,
        actions = {
            IconButton(onClick = onAbout, modifier = Modifier.testTag("about")) {
                Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.action_about))
            }
        }
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    stringResource(R.string.question),
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() }
                )
                Text(stringResource(R.string.place), fontSize = 18.sp, color = palette.secondaryText)
            }
            when {
                state == null -> Unit

                state.fetchedAt == null -> LoadingCard(state.offline, palette, onRetry)

                else -> {
                    AnswerCard(state.tomorrow, texts, palette)
                    Upcoming(state.upcoming, texts, palette)
                    Footer(state, texts, palette, onRetry)
                }
            }
        }
    }
}

@Composable
private fun Card(colour: Color, tag: String, content: @Composable () -> Unit) = Box(
    Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(colour).padding(24.dp).testTag(tag)
) { content() }

@Composable
private fun AnswerCard(tomorrow: Tomorrow, texts: JaleoTexts, palette: JaleoPalette) {
    val colour = when (tomorrow) {
        is Tomorrow.Calm -> palette.calm
        is Tomorrow.Busy -> if (tomorrow.event.level == Level.MAX) palette.max else palette.busy
    }
    Card(colour, tag = "answer") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                texts.tomorrowLabel(tomorrow.date),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.onCard
            )
            val answer = when (tomorrow) {
                is Tomorrow.Calm -> R.string.answer_calm
                is Tomorrow.Busy -> if (tomorrow.maybe) R.string.answer_maybe else R.string.answer_busy
            }
            Text(
                stringResource(answer),
                fontSize = 40.sp,
                lineHeight = 46.sp,
                fontWeight = FontWeight.Bold,
                color = palette.onCard
            )
            when (tomorrow) {
                is Tomorrow.Busy -> {
                    Text(
                        texts.what(tomorrow.event),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.onCard
                    )
                    Text(texts.title(tomorrow.event), fontSize = 22.sp, color = palette.onCard)
                    Spacer(Modifier.height(4.dp))
                    Text(texts.hint(tomorrow), fontSize = 19.sp, lineHeight = 26.sp, color = palette.onCard)
                }

                is Tomorrow.Calm -> {
                    Text(stringResource(R.string.calm_line), fontSize = 22.sp, color = palette.onCard)
                    Text(
                        stringResource(R.string.calm_reminder),
                        fontSize = 19.sp,
                        lineHeight = 26.sp,
                        color = palette.onCard
                    )
                }
            }
        }
    }
}

@Composable
private fun Upcoming(days: List<UpcomingDay>, texts: JaleoTexts, palette: JaleoPalette) = Column(
    verticalArrangement = Arrangement.spacedBy(12.dp)
) {
    Text(
        stringResource(R.string.upcoming_title),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp).semantics { heading() }
    )
    if (days.isEmpty()) {
        Text(stringResource(R.string.upcoming_none), fontSize = 19.sp, color = palette.secondaryText)
    }
    days.forEach { day ->
        Row(
            Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(20.dp))
                .background(palette.surface)
                .testTag("upcoming_${day.event.id}")
        ) {
            Box(
                Modifier.width(10.dp).fillMaxHeight()
                    .background(if (day.event.level == Level.MAX) palette.max else palette.busy)
            )
            Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                Text(texts.upcomingWhen(day), fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(texts.upcomingWhat(day), fontSize = 18.sp, lineHeight = 24.sp, color = palette.secondaryText)
            }
        }
    }
}

@Composable
private fun LoadingCard(offline: Boolean, palette: JaleoPalette, onRetry: () -> Unit) =
    Card(palette.surface, tag = "loading") {
        Column {
            Text(
                stringResource(if (offline) R.string.offline else R.string.loading),
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (offline) RetryButton(onRetry)
        }
    }

@Composable
private fun Footer(state: JaleoUiState, texts: JaleoTexts, palette: JaleoPalette, onRetry: () -> Unit) {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(R.string.sources), fontSize = 15.sp, lineHeight = 21.sp, color = palette.secondaryText)
        state.fetchedAt?.let {
            Text(texts.checkedAt(it, state.today, state.zone), fontSize = 15.sp, color = palette.secondaryText)
        }
        if (state.offline) {
            Text(stringResource(R.string.offline), fontSize = 15.sp, color = palette.secondaryText)
            RetryButton(onRetry)
        }
    }
}

@Composable
private fun RetryButton(onRetry: () -> Unit) = TextButton(onClick = onRetry, modifier = Modifier.testTag("retry")) {
    Text(stringResource(R.string.retry), fontSize = 18.sp)
}
