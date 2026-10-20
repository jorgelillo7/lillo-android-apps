package com.jorgelillo.tournaments.ui.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.ConfettiBurst
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.domain.Format
import com.jorgelillo.tournaments.domain.Match
import com.jorgelillo.tournaments.domain.Stage
import com.jorgelillo.tournaments.domain.Tournament
import com.jorgelillo.tournaments.domain.Tournaments
import com.jorgelillo.tournaments.ui.Page
import com.jorgelillo.tournaments.ui.bestOfLabel
import com.jorgelillo.tournaments.ui.formatDay
import com.jorgelillo.tournaments.ui.theme.Arena
import kotlin.random.Random

private enum class Tab { MATCHES, BRACKET, STANDINGS }

@Composable
fun TournamentScreen(
    t: Tournament,
    onChange: ((Tournament) -> Tournament) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val tabs = buildList {
        add(Tab.MATCHES)
        if (t.format == Format.SWISS) add(Tab.STANDINGS)
        if (t.eliminationRounds > 0) add(Tab.BRACKET)
    }
    var tab by rememberSaveable { mutableStateOf(if (t.format == Format.ELIMINATION) Tab.BRACKET else Tab.MATCHES) }
    if (tab !in tabs) tab = Tab.MATCHES
    var editing by rememberSaveable(stateSaver = MatchKey.Saver) { mutableStateOf<MatchKey?>(null) }
    var sharing by rememberSaveable { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    // Confetti only when the champion is decided while watching, not every time the screen opens.
    val championAtOpen = rememberSaveable(t.id) { t.champion ?: -1 }
    val celebrate = t.champion?.takeIf { it != championAtOpen }

    Box(Modifier.fillMaxSize()) {
        Page(
            title = t.name,
            onBack = onBack,
            actions = {
                IconButton(onClick = { sharing = true }, modifier = Modifier.testTag("share")) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.action_share))
                }
                Box {
                    IconButton(onClick = { menu = true }, modifier = Modifier.testTag("menu")) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.action_more))
                    }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }, modifier = TestTagsAsResourceIds) {
                        if (Tournaments.canRedraw(t)) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_redraw)) },
                                onClick = {
                                    menu = false
                                    onChange { Tournaments.draw(it, Random.Default) }
                                },
                                modifier = Modifier.testTag("redraw_menu")
                            )
                        }
                        if (Tournaments.canUnpairLastRound(t)) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_unpair)) },
                                onClick = {
                                    menu = false
                                    onChange(Tournaments::unpairLastRound)
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.export_image)) },
                            onClick = {
                                menu = false
                                shareImage(context, t)
                            },
                            modifier = Modifier.testTag("export_image")
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.export_csv)) },
                            onClick = {
                                menu = false
                                shareCsv(context, t)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.action_delete), color = Arena.Coral) },
                            onClick = {
                                menu = false
                                confirmDelete = true
                            },
                            modifier = Modifier.testTag("delete")
                        )
                    }
                }
            },
            bottom = { NextStep(t, onChange) }
        ) {
            val champion = t.player(t.champion)
            // Landscape / handhelds: no room for the banner, the champion goes in the summary line.
            val short = isShortScreen()
            Text(
                listOfNotNull(
                    champion?.takeIf { short }?.let { "🏆 ${it.name}" },
                    t.game,
                    formatDay(t.epochDay),
                    resources.bestOfLabel(t.bestOf),
                    stringResource(if (t.format == Format.SWISS) R.string.format_swiss else R.string.format_elimination)
                ).filter { it.isNotBlank() }.joinToString(" · "),
                color = Arena.Muted,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (champion != null && !short) ChampionBanner(champion.name)
            if (tabs.size > 1) {
                PrimaryTabRow(selectedTabIndex = tabs.indexOf(tab), containerColor = androidx.compose.ui.graphics.Color.Transparent, contentColor = Arena.Cream) {
                    tabs.forEach { option ->
                        Tab(
                            selected = option == tab,
                            onClick = { tab = option },
                            modifier = Modifier.testTag("tab_${option.name.lowercase()}"),
                            text = {
                                Text(
                                    stringResource(
                                        when (option) {
                                            Tab.MATCHES -> R.string.tab_matches
                                            Tab.BRACKET -> R.string.tab_bracket
                                            Tab.STANDINGS -> R.string.tab_standings
                                        }
                                    )
                                )
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    Tab.MATCHES -> MatchList(t, onEdit = { editing = MatchKey.of(it) })
                    Tab.BRACKET -> BracketView(t, onEdit = { editing = MatchKey.of(it) })
                    Tab.STANDINGS -> StandingsView(t)
                }
            }
        }
        ConfettiBurst(celebrate, Arena.Confetti, Modifier.fillMaxSize())
    }

    editing?.let { key ->
        val match = t.matches.firstOrNull { key.matches(it) }
        if (match == null || match.a == null || match.b == null) {
            LaunchedEffect(key) { editing = null }
        } else {
            ResultSheet(
                t = t,
                match = match,
                onSave = { winsA, winsB, draw, comment ->
                    editing = null
                    onChange { Tournaments.record(it, match, winsA, winsB, comment, draw) }
                },
                onClear = {
                    editing = null
                    onChange { Tournaments.clear(it, match) }
                },
                onDismiss = { editing = null }
            )
        }
    }
    if (sharing) ShareSheet(t, onDismiss = { sharing = false })
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            modifier = TestTagsAsResourceIds,
            title = { Text(stringResource(R.string.delete_title, t.name)) },
            text = { Text(stringResource(R.string.delete_body)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }, modifier = Modifier.testTag("delete_ok")) {
                    Text(stringResource(R.string.action_delete), color = Arena.Coral)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.action_cancel)) } }
        )
    }
}

/** The one thing to do next, as the big button: next Swiss round, top cut, or nothing. */
@Composable
private fun NextStep(t: Tournament, onChange: ((Tournament) -> Tournament) -> Unit) {
    when {
        Tournaments.canPairNextRound(t) -> com.jorgelillo.tournaments.ui.BigButton(
            stringResource(R.string.action_pair_round, Tournaments.currentSwissRound(t) + 1),
            { onChange { Tournaments.pairNextRound(it) } },
            "next_round"
        )

        Tournaments.canStartTopCut(t) -> com.jorgelillo.tournaments.ui.BigButton(
            stringResource(R.string.action_start_top_cut, t.topCut),
            { onChange(Tournaments::startTopCut) },
            "top_cut"
        )
    }
}

@Composable
private fun ChampionBanner(name: String) {
    Row(
        Modifier
            .padding(top = 10.dp)
            .fillMaxWidth()
            .background(Arena.Gold, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("champion"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🏆", fontSize = 26.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(stringResource(R.string.champion_label), color = Arena.Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(name, color = Arena.Ink, fontSize = 20.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** A match by position, so the sheet survives the tournament being saved again. */
data class MatchKey(val stage: Stage, val round: Int, val slot: Int) {
    fun matches(m: Match) = m.stage == stage && m.round == round && m.slot == slot

    companion object {
        fun of(m: Match) = MatchKey(m.stage, m.round, m.slot)

        val Saver = androidx.compose.runtime.saveable.Saver<MatchKey?, String>(
            save = { it?.let { k -> "${k.stage.name}:${k.round}:${k.slot}" } ?: "" },
            restore = { raw -> raw.split(':').takeIf { it.size == 3 }?.let { MatchKey(Stage.valueOf(it[0]), it[1].toInt(), it[2].toInt()) } }
        )
    }
}
