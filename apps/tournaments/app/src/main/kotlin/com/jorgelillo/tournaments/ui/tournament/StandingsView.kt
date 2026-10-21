package com.jorgelillo.tournaments.ui.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.domain.Standings
import com.jorgelillo.tournaments.domain.Tournament
import com.jorgelillo.tournaments.ui.theme.Arena
import kotlin.math.roundToInt

/** Swiss table: points, W-D-L and the first two tie-breakers; a gold line marks the top cut. */
@Composable
internal fun StandingsView(t: Tournament) {
    val rows = Standings.of(t)
    LazyColumn(Modifier.testTag("standings"), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)) {
                Header("#", 28)
                Text(
                    stringResource(R.string.standings_player),
                    color = Arena.Muted,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Header(stringResource(R.string.standings_points), 40)
                Header(stringResource(R.string.standings_record), 64)
                Header("OMW", 44)
                Header("GW", 44)
            }
        }
        itemsIndexed(rows, key = { _, s -> s.player }) { i, s ->
            if (t.topCut > 0 && i == t.topCut) {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).height(2.dp).background(Arena.Gold))
                    Text(
                        stringResource(R.string.setup_top_cut_n, t.topCut),
                        color = Arena.Gold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Box(Modifier.weight(1f).height(2.dp).background(Arena.Gold))
                }
            }
            val leader = i == 0 && s.points > 0
            Row(
                Modifier.fillMaxWidth().background(
                    Arena.Raised,
                    RoundedCornerShape(10.dp)
                ).padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${i + 1}",
                    color = if (leader) Arena.Gold else Arena.Muted,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(28.dp)
                )
                Text(
                    t.player(s.player)?.name.orEmpty(),
                    fontWeight = FontWeight.Bold,
                    color = if (leader) Arena.Gold else Arena.Cream,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Cell("${s.points}", 40, bold = true)
                Cell("${s.wins}-${s.draws}-${s.losses}", 64)
                Cell(percent(s.opponentsWinRate), 44)
                Cell(percent(s.gameWinRate), 44)
            }
        }
        item { Spacer(Modifier.height(8.dp)) }
    }
}

private fun percent(v: Double) = "${(v * 100).roundToInt()}%"

@Composable
private fun Header(text: String, width: Int) =
    Text(text, color = Arena.Muted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.width(width.dp))

@Composable
private fun Cell(text: String, width: Int, bold: Boolean = false) = Text(
    text,
    fontSize = 13.sp,
    fontWeight = if (bold) FontWeight.Black else FontWeight.Normal,
    textAlign = TextAlign.Center,
    modifier = Modifier.width(width.dp)
)
