package com.jorgelillo.tournaments.ui

import android.content.res.Resources
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import com.jorgelillo.core.designsystem.LilloBigButton
import com.jorgelillo.core.designsystem.LilloPage
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.domain.Format
import com.jorgelillo.tournaments.domain.Match
import com.jorgelillo.tournaments.domain.Stage
import com.jorgelillo.tournaments.domain.Tournament
import com.jorgelillo.tournaments.ui.theme.Arena
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun Page(
    title: String?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    /** Lists and text keep a readable width on tablets (see [LilloPage]); off for brackets and canvases. */
    readable: Boolean = true,
    bottom: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) = LilloPage(
    title,
    onBack,
    Arena.Background,
    modifier = modifier,
    actions = actions,
    readable = readable,
    bottom = bottom,
    content = content
)

@Composable
internal fun BigButton(
    text: String,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) = LilloBigButton(text, onClick, modifier = modifier.testTag(tag), enabled = enabled)

fun formatDay(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

fun today(): Long = LocalDate.now().toEpochDay()

/** "Final", "Semifinals"… counted from the end; Swiss rounds and big early rounds are numbered. */
fun Resources.roundName(t: Tournament, stage: Stage, round: Int): String {
    if (stage == Stage.SWISS) return getString(R.string.round_n, round)
    val prefix = if (t.format == Format.SWISS) getString(R.string.top_cut_prefix) + " · " else ""
    return prefix + when (t.eliminationRounds - round) {
        0 -> getString(R.string.round_final)
        1 -> getString(R.string.round_semifinals)
        2 -> getString(R.string.round_quarterfinals)
        3 -> getString(R.string.round_of_16)
        else -> getString(R.string.round_n, round)
    }
}

fun Resources.roundName(t: Tournament, m: Match): String = roundName(t, m.stage, m.round)

@Composable
fun roundName(t: Tournament, stage: Stage, round: Int): String = LocalResources.current.roundName(t, stage, round)

/** "Pepe beat Luis 2-1" / "Pepe and Luis drew 1-1". */
fun Resources.resultSentence(t: Tournament, m: Match): String? {
    val a = t.player(m.a)?.name ?: return null
    val b = t.player(m.b)?.name ?: return null
    if (m.draw) return getString(R.string.result_draw_sentence, a, b, m.winsA, m.winsB)
    return when (m.winner(t.bestOf)) {
        m.a -> getString(R.string.result_sentence, a, b, m.winsA, m.winsB)
        m.b -> getString(R.string.result_sentence, b, a, m.winsB, m.winsA)
        else -> null
    }
}

fun Resources.bestOfLabel(bestOf: Int): String = if (bestOf ==
    1
) {
    getString(R.string.best_of_one)
} else {
    getString(R.string.best_of_n, bestOf)
}
