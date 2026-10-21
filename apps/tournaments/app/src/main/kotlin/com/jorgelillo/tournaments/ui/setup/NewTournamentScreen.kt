package com.jorgelillo.tournaments.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.LilloSectionTitle
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.tournaments.R
import com.jorgelillo.tournaments.domain.Bracket
import com.jorgelillo.tournaments.domain.Format
import com.jorgelillo.tournaments.domain.Library
import com.jorgelillo.tournaments.domain.Stats
import com.jorgelillo.tournaments.domain.Swiss
import com.jorgelillo.tournaments.domain.Tournament
import com.jorgelillo.tournaments.domain.Tournaments
import com.jorgelillo.tournaments.ui.BigButton
import com.jorgelillo.tournaments.ui.Page
import com.jorgelillo.tournaments.ui.bestOfLabel
import com.jorgelillo.tournaments.ui.formatDay
import com.jorgelillo.tournaments.ui.theme.Arena
import com.jorgelillo.tournaments.ui.today

private const val DAY_MILLIS = 86_400_000L
private val GAME_SUGGESTIONS = listOf("Magic", "Pokémon", "Lorcana", "One Piece", "Yu-Gi-Oh!", "FIFA", "Ping-pong", "Catan")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewTournamentScreen(library: Library, newId: () -> String, onCreate: (Tournament) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val resources = LocalResources.current
    var name by rememberSaveable { mutableStateOf("") }
    var game by rememberSaveable { mutableStateOf("") }
    var day by rememberSaveable { mutableLongStateOf(today()) }
    var format by rememberSaveable { mutableStateOf(Format.ELIMINATION) }
    var bestOf by rememberSaveable { mutableIntStateOf(3) }
    var swissRounds by rememberSaveable { mutableIntStateOf(0) } // 0 = recommended for the field
    var topCut by rememberSaveable { mutableIntStateOf(0) }
    val players = rememberSaveable(saver = listSaver()) { mutableStateListOf() }
    var newPlayer by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var pickDate by rememberSaveable { mutableStateOf(false) }
    val short = isShortScreen()

    val duplicateMessage = stringResource(R.string.setup_duplicate)
    fun add(raw: String) {
        // A pasted list ("Pepe, Luis\nAna") adds everyone at once.
        val names = raw.split(',', '\n', ';').map { it.trim() }.filter { it.isNotEmpty() }
        val taken = players.map(Stats::key).toMutableSet()
        val (added, duplicates) = names.partition { taken.add(Stats.key(it)) }
        added.take(Tournaments.MAX_PLAYERS - players.size).forEach(players::add)
        error = if (duplicates.isNotEmpty()) duplicateMessage.format(duplicates.first()) else null
        newPlayer = ""
    }

    val rounds = if (swissRounds == 0) Swiss.recommendedRounds(players.size) else swissRounds
    val defaultName = stringResource(R.string.setup_default_name, formatDay(day))
    val canStart = players.size >= Tournaments.MIN_PLAYERS
    val start = {
        onCreate(
            Tournaments.create(
                id = newId(),
                name = name.ifBlank { defaultName },
                epochDay = day,
                names = players.toList(),
                format = format,
                bestOf = bestOf,
                game = game,
                swissRounds = rounds,
                topCut = topCut
            )
        )
    }
    val startButton = @Composable {
        BigButton(stringResource(R.string.setup_start), start, "start", enabled = canStart)
    }

    Page(
        title = stringResource(R.string.new_tournament),
        onBack = onBack,
        bottom = { if (!short) startButton() }
    ) {
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(60) },
                    label = { Text(stringResource(R.string.setup_name)) },
                    placeholder = { Text(defaultName) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().testTag("name")
                )
            }
            item {
                OutlinedTextField(
                    value = game,
                    onValueChange = { game = it.take(40) },
                    label = { Text(stringResource(R.string.setup_game)) },
                    placeholder = { Text(stringResource(R.string.setup_game_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth().testTag("game")
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (library.knownGames + GAME_SUGGESTIONS).distinctBy { it.lowercase() }.take(8).forEach { g ->
                        FilterChip(selected = game.equals(g, ignoreCase = true), onClick = { game = if (game == g) "" else g }, label = { Text(g) })
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.setup_date), color = Arena.Muted, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { pickDate = true }, modifier = Modifier.testTag("date")) {
                        Text(if (day == today()) stringResource(R.string.setup_today, formatDay(day)) else formatDay(day))
                    }
                }
            }
            item {
                Label(stringResource(R.string.setup_format))
                Segmented(
                    options = listOf(Format.ELIMINATION to stringResource(R.string.format_elimination), Format.SWISS to stringResource(R.string.format_swiss)),
                    selected = format,
                    onSelect = { format = it },
                    tag = "format"
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(if (format == Format.ELIMINATION) R.string.format_elimination_help else R.string.format_swiss_help),
                    color = Arena.Muted,
                    fontSize = 13.sp
                )
            }
            item {
                Label(stringResource(R.string.setup_matches))
                Segmented(
                    options = Tournaments.BEST_OF.map { it to resources.bestOfLabel(it) },
                    selected = bestOf,
                    onSelect = { bestOf = it },
                    tag = "bo"
                )
            }
            if (format == Format.SWISS) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.setup_swiss_rounds), modifier = Modifier.weight(1f))
                        IconButton(onClick = { swissRounds = (rounds - 1).coerceAtLeast(1) }) { Text("−", fontSize = 22.sp) }
                        Text("$rounds", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = { swissRounds = (rounds + 1).coerceAtMost(maxOf(1, players.size - 1)) }) { Icon(Icons.Filled.Add, null) }
                    }
                    Label(stringResource(R.string.setup_top_cut))
                    Segmented(
                        options = Tournaments.TOP_CUTS.map { it to if (it == 0) stringResource(R.string.setup_top_cut_none) else stringResource(R.string.setup_top_cut_n, it) },
                        selected = topCut,
                        onSelect = { topCut = it },
                        tag = "cut",
                        enabled = { it == 0 || it < players.size }
                    )
                }
            }
            item {
                Label(pluralStringResource(R.plurals.players_count, players.size, players.size))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newPlayer,
                        onValueChange = { newPlayer = it.take(200) },
                        placeholder = { Text(stringResource(R.string.setup_player_hint)) },
                        singleLine = true,
                        isError = error != null,
                        supportingText = error?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { if (newPlayer.isNotBlank()) add(newPlayer) }),
                        modifier = Modifier.weight(1f).testTag("player")
                    )
                    IconButton(onClick = { add(newPlayer) }, enabled = newPlayer.isNotBlank(), modifier = Modifier.testTag("add_player")) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.setup_add))
                    }
                }
                val suggestions = library.knownNames.filter { n -> players.none { Stats.key(it) == Stats.key(n) } }.take(12)
                if (suggestions.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        suggestions.forEach { n -> SuggestionChip(onClick = { add(n) }, label = { Text("+ $n") }) }
                    }
                }
                if (format == Format.ELIMINATION && players.size >= 3) {
                    val byes = Bracket.size(players.size) - players.size
                    if (byes > 0) Text(pluralStringResource(R.plurals.setup_byes, byes, byes), color = Arena.Muted, fontSize = 13.sp)
                }
            }
            itemsIndexed(players, key = { _, n -> n }) { i, n ->
                Row(
                    Modifier.fillMaxWidth().background(Arena.Raised, RoundedCornerShape(12.dp)).padding(start = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${i + 1}", color = Arena.Muted, modifier = Modifier.width(28.dp))
                    Text(n, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    IconButton(onClick = { players.removeAt(i) }) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.setup_remove, n), tint = Arena.Muted)
                    }
                }
            }
            if (short) item { startButton() }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (pickDate) {
        DateDialog(day, onPick = {
            day = it
            pickDate = false
        }, onDismiss = { pickDate = false })
    }
}

@Composable
private fun Label(text: String) = LilloSectionTitle(text, modifier = Modifier.padding(bottom = 6.dp))

@Composable
private fun <T> Segmented(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit, tag: String, enabled: (T) -> Boolean = { true }) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { i, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelect(value) },
                enabled = enabled(value),
                shape = SegmentedButtonDefaults.itemShape(i, options.size),
                modifier = Modifier.testTag("${tag}_$i"),
                colors = SegmentedButtonDefaults.colors(activeContainerColor = Arena.Gold, activeContentColor = Arena.Ink),
                label = { Text(label, maxLines = 1) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateDialog(day: Long, onPick: (Long) -> Unit, onDismiss: () -> Unit) {
    val state = rememberDatePickerState(initialSelectedDateMillis = day * DAY_MILLIS)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        modifier = TestTagsAsResourceIds,
        confirmButton = {
            TextButton(onClick = { onPick((state.selectedDateMillis ?: (day * DAY_MILLIS)) / DAY_MILLIS) }, modifier = Modifier.testTag("date_ok")) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    ) { DatePicker(state) }
}

private fun listSaver() = androidx.compose.runtime.saveable.listSaver<androidx.compose.runtime.snapshots.SnapshotStateList<String>, String>(
    save = { it.toList() },
    restore = { mutableStateListOf<String>().apply { addAll(it) } }
)
