package com.jorgelillo.tournaments.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.domain.Library
import com.jorgelillo.tournaments.domain.Stats
import com.jorgelillo.tournaments.ui.Page
import com.jorgelillo.tournaments.ui.theme.Arena
import kotlin.math.roundToInt

/** Hall of fame across every tournament on this phone: titles, finals and a win-rate bar. */
@Composable
internal fun StatsScreen(library: Library, onBack: () -> Unit) {
    var game by rememberSaveable { mutableStateOf<String?>(null) }
    val rows = Stats.of(library.tournaments, game)
    val played = library.tournaments.count { game == null || it.game.equals(game, ignoreCase = true) }
    Page(title = stringResource(R.string.stats_title), onBack = onBack) {
        if (library.knownGames.isNotEmpty()) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(selected = game == null, onClick = {
                    game = null
                }, label = { Text(stringResource(R.string.stats_all_games)) })
                library.knownGames.forEach { g ->
                    FilterChip(selected = game == g, onClick = { game = g }, label = { Text(g) })
                }
            }
        }
        Text(pluralStringResource(R.plurals.stats_tournaments, played, played), color = Arena.Muted, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.testTag("leaderboard"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(rows, key = { _, s -> Stats.key(s.name) }) { i, s ->
                Column(Modifier.fillMaxWidth().background(Arena.Raised, RoundedCornerShape(14.dp)).padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            when (i) {
                                0 -> "🥇"
                                1 -> "🥈"
                                2 -> "🥉"
                                else -> "${i + 1}"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp)
                        )
                        Text(
                            s.name,
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (s.titles > 0) Text("🏆 ${s.titles}", color = Arena.GoldText, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.stats_line, s.tournaments, s.finals, s.wins, s.draws, s.losses),
                        color = Arena.Muted,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    WinRateBar(s.winRate)
                }
            }
        }
    }
}

@Composable
private fun WinRateBar(rate: Double) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(10.dp).background(Arena.High, RoundedCornerShape(5.dp))) {
            if (rate >
                0
            ) {
                Box(
                    Modifier.fillMaxWidth(
                        rate.toFloat()
                    ).fillMaxHeight().background(Arena.Accent, RoundedCornerShape(5.dp))
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            stringResource(R.string.stats_win_rate, (rate * 100).roundToInt()),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
