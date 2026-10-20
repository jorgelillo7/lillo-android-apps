package com.jorgelillo.christmascountdown.ui.countdown

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.christmascountdown.R
import com.jorgelillo.christmascountdown.domain.CountdownState
import com.jorgelillo.christmascountdown.domain.TimeLeft
import com.jorgelillo.christmascountdown.ui.theme.ChristmasColorScheme
import com.jorgelillo.christmascountdown.ui.theme.ChristmasColors
import com.jorgelillo.core.designsystem.GlassCard
import com.jorgelillo.core.designsystem.LilloSectionTitle
import com.jorgelillo.core.designsystem.LilloTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun CountdownScreen(
    state: CountdownUiState,
    onSleepsModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    reminderPrompt: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val countdown = state.countdown) {
            CountdownState.ChristmasDay -> ChristmasDayContent()

            is CountdownState.Counting -> {
                AnimatedContent(
                    targetState = state.sleepsMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "countdown-mode"
                ) { sleepsMode ->
                    if (sleepsMode) SleepsContent(countdown.sleeps) else TimeContent(countdown.timeLeft)
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = countdown.target.formatted(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(28.dp))
                ModeSelector(sleepsMode = state.sleepsMode, onChange = onSleepsModeChange)
                if (reminderPrompt != null) {
                    Spacer(Modifier.height(28.dp))
                    reminderPrompt()
                }
            }
        }
    }
}

@Composable
private fun TimeContent(timeLeft: TimeLeft) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SectionLabel(stringResource(R.string.countdown_title))
        HeroNumber(timeLeft.days.toString())
        Text(
            text = stringResource(R.string.unit_days),
            style = MaterialTheme.typography.titleLarge,
            color = ChristmasColors.Gold
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().widthIn(max = 420.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TimeUnitCard(timeLeft.hours, stringResource(R.string.unit_hours), Modifier.weight(1f))
            TimeUnitCard(timeLeft.minutes, stringResource(R.string.unit_minutes), Modifier.weight(1f))
            TimeUnitCard(timeLeft.seconds, stringResource(R.string.unit_seconds), Modifier.weight(1f))
        }
    }
}

@Composable
private fun SleepsContent(sleeps: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SectionLabel(stringResource(R.string.sleeps_title))
        HeroNumber(sleeps.toString())
        Text(
            text = pluralStringResource(R.plurals.sleeps_left, sleeps.toInt(), sleeps),
            style = MaterialTheme.typography.titleLarge,
            color = ChristmasColors.Gold
        )
        Spacer(Modifier.height(24.dp))
        // A row of moons, one per night, for the last two weeks.
        if (sleeps in 1..14) {
            Text(text = "🌙".repeat(sleeps.toInt()), fontSize = 22.sp, textAlign = TextAlign.Center)
        } else {
            Text(text = "🌙 🎄 🎁", fontSize = 28.sp)
        }
    }
}

@Composable
private fun ChristmasDayContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "🎄", fontSize = 96.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.christmas_day_title),
            style = MaterialTheme.typography.displayMedium,
            color = ChristmasColors.Gold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.christmas_day_body),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SectionLabel(text: String) = LilloSectionTitle(text, MaterialTheme.colorScheme.onSurfaceVariant, uppercase = true, style = MaterialTheme.typography.labelSmall)

@Composable
private fun HeroNumber(value: String) {
    Text(
        text = value,
        style = MaterialTheme.typography.displayLarge.copy(fontSize = 120.sp, lineHeight = 124.sp),
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun TimeUnitCard(value: Int, label: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "%02d".format(value), style = MaterialTheme.typography.displaySmall)
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModeSelector(sleepsMode: Boolean, onChange: (Boolean) -> Unit) {
    val options = listOf(stringResource(R.string.mode_time), stringResource(R.string.mode_sleeps))
    SingleChoiceSegmentedButtonRow(modifier = Modifier.widthIn(max = 320.dp).fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = sleepsMode == (index == 1),
                onClick = { onChange(index == 1) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = ChristmasColors.Gold,
                    activeContentColor = MaterialTheme.colorScheme.onPrimary,
                    inactiveContainerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            ) { Text(label) }
        }
    }
}

@Composable
private fun LocalDate.formatted(): String {
    val formatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL) }
    return format(formatter).replaceFirstChar { it.uppercase() }
}

@Preview
@Composable
private fun CountdownScreenPreview() {
    LilloTheme(ChristmasColorScheme) {
        CountdownScreen(
            state = CountdownUiState(CountdownState.Counting(TimeLeft(83, 5, 42, 7), 84, LocalDate.of(2026, 12, 25))),
            onSleepsModeChange = {}
        )
    }
}
