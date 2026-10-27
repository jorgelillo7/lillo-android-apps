package com.jorgelillo.tournaments.ui.tournament

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.domain.Match
import com.jorgelillo.tournaments.domain.Stage
import com.jorgelillo.tournaments.domain.Tournament
import com.jorgelillo.tournaments.ui.BigButton
import com.jorgelillo.tournaments.ui.resultSentence
import com.jorgelillo.tournaments.ui.roundName
import com.jorgelillo.tournaments.ui.theme.Arena

/** Every round with its matches; opens scrolled to the first round that still needs results. */
@Composable
internal fun MatchList(t: Tournament, onEdit: (Match) -> Unit) {
    val rounds = t.matches.filter { it.a != null || it.b != null }
        .groupBy { it.stage to it.round }
        .toSortedMap(compareBy({ it.first != Stage.SWISS }, { it.second }))
    val firstPending = rounds.keys.indexOfFirst { key ->
        rounds.getValue(key).any {
            it.a != null && it.b != null &&
                !it.isDone(t.bestOf)
        }
    }
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = rounds.keys.take(firstPending.coerceAtLeast(0)).sumOf {
                rounds.getValue(it).size +
                    1
            }
        )
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag("matches")
    ) {
        rounds.forEach { (key, matches) ->
            item(key = "${key.first}${key.second}") {
                Text(
                    roundName(t, key.first, key.second),
                    color = Arena.Gold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(
                matches.sortedBy {
                    it.slot
                },
                key = { "${it.stage}${it.round}-${it.slot}" }
            ) { MatchCard(t, it, onEdit) }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

@Composable
private fun MatchCard(t: Tournament, m: Match, onEdit: (Match) -> Unit) {
    val playable = m.a != null && m.b != null
    val done = m.isDone(t.bestOf)
    val winner = m.winner(t.bestOf)
    Column(
        Modifier
            .fillMaxWidth()
            .background(Arena.Raised, RoundedCornerShape(14.dp))
            .then(
                if (playable &&
                    !done
                ) {
                    Modifier.border(BorderStroke(1.dp, Arena.Faint), RoundedCornerShape(14.dp))
                } else {
                    Modifier
                }
            )
            .clickable(enabled = playable) { onEdit(m) }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("match_${m.stage.name.lowercase()}_${m.round}_${m.slot}")
    ) {
        if (m.isBye) {
            Text(stringResource(R.string.match_bye, t.player(m.a ?: m.b)?.name.orEmpty()), color = Arena.Muted)
            return@Column
        }
        PlayerLine(t.player(m.a)?.name, m.winsA, done, winner == m.a && winner != null)
        PlayerLine(t.player(m.b)?.name, m.winsB, done, winner == m.b && winner != null)
        if (m.draw) Text(stringResource(R.string.match_draw), color = Arena.Muted, fontSize = 12.sp)
        if (m.comment.isNotBlank()) {
            Text(
                "“${m.comment}”",
                color = Arena.Muted,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (playable && !done) Text(stringResource(R.string.match_tap_to_score), color = Arena.Teal, fontSize = 12.sp)
    }
}

@Composable
private fun PlayerLine(name: String?, wins: Int, done: Boolean, won: Boolean) {
    Row(Modifier.fillMaxWidth().heightIn(min = 28.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.width(
                4.dp
            ).height(20.dp).background(if (won) Arena.Gold else Color.Transparent, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(8.dp))
        Text(
            name ?: stringResource(R.string.match_tbd),
            color = when {
                name == null -> Arena.Muted
                won -> Arena.Gold
                done -> Arena.Muted
                else -> Arena.Cream
            },
            fontWeight = if (won) FontWeight.Black else FontWeight.SemiBold,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (done) {
            Text(
                "$wins",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = if (won) Arena.Gold else Arena.Muted
            )
        }
    }
}

/**
 * "Who won and how": tap the winner (or draw, in Swiss), pick the score, optional comment. Only
 * valid scores are offered, so a Bo3 knock-out can't end 1-0.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun ResultSheet(
    t: Tournament,
    match: Match,
    onSave: (winsA: Int, winsB: Int, draw: Boolean, comment: String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val res = LocalResources.current
    val needed = Match.needed(t.bestOf)
    val a = t.player(match.a)!!.name
    val b = t.player(match.b)!!.name
    val current = match.winner(t.bestOf)
    // 0 = A won, 1 = B won, 2 = draw, -1 = not chosen yet.
    var outcome by rememberSaveable {
        mutableIntStateOf(
            if (match.draw) {
                2
            } else if (current == match.a) {
                0
            } else if (current == match.b) {
                1
            } else {
                -1
            }
        )
    }
    var loserGames by rememberSaveable {
        mutableIntStateOf(
            if (current == match.a) {
                match.winsB
            } else if (current == match.b) {
                match.winsA
            } else if (match.draw) {
                match.winsA
            } else {
                0
            }
        )
    }
    var comment by rememberSaveable { mutableStateOf(match.comment) }
    val allowDraw = match.stage == Stage.SWISS
    // Draw options: k-k for k = needed-1 down to 0 (e.g. 1-1, 0-0 in a Bo3).
    if (outcome == 2 && loserGames > needed - 1) loserGames = needed - 1

    val (winsA, winsB) = when (outcome) {
        0 -> needed to loserGames
        1 -> loserGames to needed
        2 -> loserGames to loserGames
        else -> 0 to 0
    }
    val preview = if (outcome >=
        0
    ) {
        res.resultSentence(t, match.copy(winsA = winsA, winsB = winsB, draw = outcome == 2))
    } else {
        null
    }
    val laterReset = match.stage == Stage.ELIMINATION &&
        t.matches.any {
            it.stage == Stage.ELIMINATION && it.round > match.round &&
                it.slot == match.slot shr (it.round - match.round) &&
                (it.winsA + it.winsB > 0)
        }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Arena.Raised
    ) {
        Column(
            TestTagsAsResourceIds.verticalScroll(
                rememberScrollState()
            ).padding(horizontal = 20.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(res.roundName(t, match), color = Arena.Gold, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.result_who_won), fontSize = 22.sp, fontWeight = FontWeight.Black)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WinnerButton(a, outcome == 0, { outcome = 0 }, "winner_a", Modifier.weight(1f))
                WinnerButton(b, outcome == 1, { outcome = 1 }, "winner_b", Modifier.weight(1f))
            }
            if (allowDraw) {
                OutlinedButton(onClick = { outcome = 2 }, modifier = Modifier.fillMaxWidth().testTag("draw")) {
                    Text(
                        stringResource(R.string.result_draw),
                        color = if (outcome ==
                            2
                        ) {
                            Arena.Gold
                        } else {
                            Arena.Cream
                        },
                        fontWeight = if (outcome ==
                            2
                        ) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        }
                    )
                }
            }
            val scores = when (outcome) {
                0, 1 -> (0 until needed).toList()
                2 -> (needed - 1 downTo 0).toList()
                else -> emptyList()
            }
            if (scores.size > 1) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    scores.forEach { k ->
                        val label = if (outcome == 2) "$k - $k" else "$needed - $k"
                        FilterChip(
                            selected = loserGames == k,
                            onClick = { loserGames = k },
                            label = { Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Arena.Gold,
                                selectedLabelColor = Arena.Ink
                            ),
                            modifier = Modifier.testTag("score_$k")
                        )
                    }
                }
            }
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it.take(80) },
                label = { Text(stringResource(R.string.result_comment)) },
                placeholder = { Text(stringResource(R.string.result_comment_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth().testTag("comment")
            )
            preview?.let {
                Text(
                    it,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().testTag("preview")
                )
            }
            if (laterReset) Text(stringResource(R.string.result_resets_later), color = Arena.Coral, fontSize = 13.sp)
            BigButton(
                stringResource(R.string.result_save),
                {
                    onSave(winsA, winsB, outcome == 2, comment)
                },
                "save",
                enabled =
                    outcome >= 0
            )
            if (match.isDone(t.bestOf)) {
                TextButton(
                    onClick = onClear,
                    modifier = Modifier.align(Alignment.CenterHorizontally).testTag("clear")
                ) {
                    Text(stringResource(R.string.result_clear), color = Arena.Muted)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun WinnerButton(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .heightIn(min = 72.dp)
            .background(if (selected) Arena.Gold else Arena.High, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            (if (selected) "🏆 " else "") + name,
            color = if (selected) Arena.Ink else Arena.Cream,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
