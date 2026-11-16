package com.jorgelillo.whoslying.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.HeroChip
import com.jorgelillo.core.designsystem.LilloBigButton
import com.jorgelillo.core.designsystem.LilloPage
import com.jorgelillo.core.designsystem.LilloSecondaryButton
import com.jorgelillo.core.designsystem.LilloSettingRow
import com.jorgelillo.core.designsystem.LilloSummaryHero
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.domain.GameMode
import com.jorgelillo.whoslying.domain.GameSettings
import com.jorgelillo.whoslying.domain.Match
import com.jorgelillo.whoslying.domain.Rules
import com.jorgelillo.whoslying.domain.WordPack
import com.jorgelillo.whoslying.ui.theme.Neon

@Composable
internal fun HomeScreen(
    /** The saved group and match, for the summary; null or no players: no summary. */
    group: HomeGroup?,
    onPlay: () -> Unit,
    /** A match half played: its next round, straight away. */
    onContinueMatch: () -> Unit,
    /** A match half played: scores back to zero, then setup. */
    onNewMatch: () -> Unit,
    onHowTo: () -> Unit,
    onModes: () -> Unit,
    onPacks: () -> Unit,
    onAbout: () -> Unit
) {
    LilloPage(title = null, onBack = null, actions = {
        IconButton(onClick = onAbout, modifier = Modifier.testTag("about")) {
            Icon(Icons.Outlined.Info, contentDescription = stringResource(R.string.action_about))
        }
    }, bottom = {
        // The round it continues with is on the summary card just above.
        if (group?.matchInProgress() != null) {
            LilloBigButton(
                stringResource(R.string.home_continue),
                onContinueMatch,
                modifier = Modifier.testTag("continue_match")
            )
            LilloSecondaryButton(
                stringResource(R.string.home_new_match),
                onNewMatch,
                modifier = Modifier.padding(top = 8.dp).testTag("new_match")
            )
        } else {
            LilloBigButton(stringResource(R.string.play), onPlay, modifier = Modifier.testTag("play"))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HomeChip("📖", stringResource(R.string.rules_short), onHowTo, Modifier.weight(1f).testTag("how_to"))
            HomeChip("🎭", stringResource(R.string.modes_short), onModes, Modifier.weight(1f).testTag("modes"))
            HomeChip("🗂️", stringResource(R.string.packs_short), onPacks, Modifier.weight(1f).testTag("packs"))
        }
    }, readable = true) {
        val summary = group?.takeIf { it.players.isNotEmpty() }
        if (summary != null && isShortScreen()) {
            // Landscape and handhelds: the group's summary is worth more than the big detective.
            Spacer(Modifier.weight(1f))
            GroupSummary(summary)
        } else {
            Spacer(Modifier.weight(1f))
            Emoji("🕵️", size = if (summary != null) 80 else 110, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.home_title),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                stringResource(R.string.home_subtitle),
                color = Neon.Muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            if (summary != null) {
                Spacer(Modifier.height(24.dp))
                GroupSummary(summary)
            }
        }
        Spacer(Modifier.weight(1.3f))
    }
}

/** What the home summary shows: the saved group, its settings and the match so far. */
internal data class HomeGroup(
    val players: List<String>,
    val settings: GameSettings,
    val scores: Map<String, Int> = emptyMap(),
    val roundsPlayed: Int = 0
) {
    /** The next round and the match length, while a scored match is half played; null otherwise. */
    fun matchInProgress(): Pair<Int, Int>? = (roundsPlayed + 1 to settings.rounds).takeIf {
        roundsPlayed > 0 && settings.rounds > 0 && !Match.isOver(roundsPlayed, settings.rounds) &&
            settings.mode != GameMode.CHAOS && players.size >= Rules.MIN_PLAYERS
    }
}

/** The group at a glance: players and mode, the match so far, who is leading. */
@Composable
private fun GroupSummary(group: HomeGroup) {
    val settings = group.settings
    val mode = stringResource(
        when (settings.mode) {
            GameMode.ORIGINAL -> R.string.mode_original
            GameMode.MYSTERIOUS -> R.string.mode_mysterious
            GameMode.CHAOS -> R.string.mode_chaos
        }
    )
    // Chaos keeps no score: a leader or a round count would belong to another match.
    val scored = settings.mode != GameMode.CHAOS
    val chips = buildList {
        if (scored) {
            group.scores.filterKeys { it in group.players }.maxByOrNull { it.value }?.takeIf { it.value > 0 }?.let {
                add(HeroChip(stringResource(R.string.hero_leader, it.key, it.value), "hero_leader"))
            }
            group.matchInProgress()?.let { (next, of) ->
                add(HeroChip(stringResource(R.string.hero_match, next, of), "hero_match"))
            }
        }
        if (settings.drawing) add(HeroChip(stringResource(R.string.hero_drawing)))
    }
    LilloSummaryHero(
        title = stringResource(R.string.hero_title),
        subtitle = pluralStringResource(R.plurals.hero_players, group.players.size, group.players.size) + " · " + mode,
        colors = Neon.Hero,
        emoji = "👥",
        chips = chips
    )
}

@Composable
private fun HomeChip(emoji: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(MaterialTheme.shapes.large)
            .background(Neon.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Text(label, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
internal fun PlayersScreen(
    players: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onRename: (old: String, new: String) -> Unit,
    /** Opened from setup: the button closes the list instead of continuing to it. */
    fromSetup: Boolean,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    var draft by rememberSaveable { mutableStateOf("") }
    var renaming by rememberSaveable { mutableStateOf<String?>(null) }

    // The name field adds a player, or with a name being renamed, saves the new name.
    fun add() {
        val old = renaming
        if (old != null) {
            onRename(old, draft)
        } else if (draft.isNotBlank()) {
            onAdd(draft)
        }
        renaming = null
        draft = ""
    }
    LilloPage(
        title = stringResource(R.string.players_title),
        onBack = onBack,
        modifier = Modifier.imePadding(),
        bottom = {
            if (players.size >= Rules.MAX_PLAYERS && renaming == null) {
                Text(
                    pluralStringResource(R.plurals.players_full, Rules.MAX_PLAYERS, Rules.MAX_PLAYERS),
                    color = Neon.AmberText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().testTag("players_full")
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { draft = it },
                        placeholder = { Text(stringResource(R.string.players_add_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { add() }),
                        modifier = Modifier.weight(1f).testTag("player_name")
                    )
                    Spacer(Modifier.width(10.dp))
                    FilledIconButton(onClick = {
                        add()
                    }, enabled = draft.isNotBlank(), modifier = Modifier.size(52.dp).testTag("add_player")) {
                        if (renaming != null) {
                            Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.players_rename_save))
                        } else {
                            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.players_add))
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            LilloBigButton(
                stringResource(if (fromSetup) R.string.players_close else R.string.continue_),
                onContinue,
                enabled = players.size >= Rules.MIN_PLAYERS,
                modifier = Modifier.testTag("continue")
            )
        },
        readable = true
    ) {
        Text(
            if (players.size < Rules.MIN_PLAYERS) {
                pluralStringResource(R.plurals.players_need_more, Rules.MIN_PLAYERS, Rules.MIN_PLAYERS)
            } else {
                stringResource(R.string.players_range, Rules.MIN_PLAYERS, Rules.MAX_PLAYERS)
            },
            color = Neon.Muted
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(players, key = { it }) { name ->
                Row(
                    Modifier.fillMaxWidth().clip(CircleShape).background(Neon.Card).padding(start = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Avatar(name, players, 38.dp)
                    Text(
                        name,
                        Modifier.weight(1f).padding(horizontal = 14.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = {
                        renaming = name
                        draft = name
                    }, modifier = Modifier.testTag("rename_$name")) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.players_rename, name),
                            tint = Neon.Muted
                        )
                    }
                    IconButton(onClick = { onRemove(name) }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.players_remove, name),
                            tint = Neon.Pink
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun SetupScreen(
    players: List<String>,
    settings: GameSettings,
    packs: List<WordPack>,
    /** Words not played yet in the selected packs, out of how many. */
    unplayed: Pair<Int, Int>,
    onChange: ((GameSettings) -> GameSettings) -> Unit,
    onPlayers: () -> Unit,
    onModesInfo: () -> Unit,
    onChoosePacks: () -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    val playerCount = players.size
    val cap = Rules.maxInfiltrators(playerCount).coerceAtLeast(1)
    val recommendedRounds = Match.recommendedRounds(playerCount)
    val recommendedTimer = Rules.recommendedTimer(playerCount)
    var picking by rememberSaveable { mutableStateOf<String?>(null) }
    // On short screens a fixed button would leave almost no room to scroll: it goes at the end of the list.
    val short = isShortScreen()
    val startButton: @Composable () -> Unit = {
        LilloBigButton(
            stringResource(R.string.start),
            onStart,
            enabled = Rules.canStart(playerCount, settings),
            modifier = Modifier.testTag("start")
        )
    }
    LilloPage(
        title = stringResource(R.string.setup_title),
        onBack = onBack,
        bottom = { if (!short) startButton() },
        readable = true
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ModePicker(settings.mode, onPick = { mode -> onChange { it.copy(mode = mode) } }, onInfo = onModesInfo)
            LilloSettingRow(
                stringResource(R.string.setup_drawing),
                leading = "🎨",
                subtitle = stringResource(R.string.setup_drawing_desc),
                containerColor = Neon.Card
            ) {
                NeonSwitch(settings.drawing, "drawing") { v -> onChange { it.copy(drawing = v) } }
            }
            Text(
                stringResource(R.string.setup_settings),
                color = Neon.Muted,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            SettingsGroup {
                GroupRow(
                    "👥",
                    stringResource(R.string.players_title),
                    tag = "players_row",
                    onClick = onPlayers,
                    value = "$playerCount"
                )
                when (settings.mode) {
                    GameMode.CHAOS -> GroupNote(stringResource(R.string.setup_chaos_note))

                    else -> {
                        GroupDivider()
                        GroupRow("🕵️", stringResource(R.string.setup_impostors)) {
                            Stepper(
                                label = stringResource(R.string.setup_impostors),
                                value = settings.impostors,
                                canLower = settings.impostors > Rules.minImpostors(settings),
                                canRaise = settings.impostors + settings.journalists < cap,
                                tag = "impostors",
                                onChange = { v -> onChange { it.copy(impostors = v) } }
                            )
                        }
                        if (settings.mode == GameMode.MYSTERIOUS) {
                            GroupDivider()
                            GroupRow(
                                "📰",
                                stringResource(R.string.setup_journalists),
                                subtitle = stringResource(R.string.setup_journalists_desc)
                            ) {
                                Stepper(
                                    label = stringResource(R.string.setup_journalists),
                                    value = settings.journalists,
                                    canLower = settings.journalists > 0 &&
                                        settings.impostors + settings.journalists > 1,
                                    canRaise = settings.impostors + settings.journalists < cap,
                                    tag = "journalists",
                                    onChange = { v -> onChange { it.copy(journalists = v) } }
                                )
                            }
                        }
                        if (settings.impostors + settings.journalists >= cap && playerCount >= Rules.MIN_PLAYERS) {
                            GroupNote(
                                if (settings.mode == GameMode.MYSTERIOUS) {
                                    pluralStringResource(R.plurals.setup_cap_infiltrators, cap, cap, playerCount)
                                } else {
                                    pluralStringResource(R.plurals.setup_cap_impostors, cap, cap, playerCount)
                                },
                                color = Neon.AmberText,
                                tag = "cap_warning"
                            )
                        }
                        if (settings.mode == GameMode.ORIGINAL) {
                            GroupDivider()
                            GroupRow(
                                "💡",
                                stringResource(R.string.setup_hint),
                                subtitle = stringResource(R.string.setup_hint_desc)
                            ) {
                                NeonSwitch(settings.impostorHint, "hint") { v ->
                                    onChange { it.copy(impostorHint = v) }
                                }
                            }
                        }
                    }
                }
            }
            SettingsGroup {
                // Chaos keeps no score, so it has no match to count rounds for.
                if (settings.mode != GameMode.CHAOS) {
                    GroupRow(
                        "🏆",
                        stringResource(R.string.setup_rounds),
                        tag = "rounds_row",
                        onClick = { picking = PICK_ROUNDS },
                        value = roundsLabel(settings.rounds)
                    )
                    GroupDivider()
                }
                GroupRow(
                    "⏱️",
                    stringResource(R.string.setup_timer),
                    tag = "timer_row",
                    onClick = { picking = PICK_TIMER },
                    value = timerLabel(settings.discussionSeconds)
                )
            }
            val (left, total) = unplayed
            val chosen = packs.filter { it.id in settings.packIds }
            LilloSettingRow(
                stringResource(R.string.setup_packs),
                leading = "🗂️",
                subtitle = when {
                    settings.packIds.isEmpty() -> stringResource(R.string.pick_packs_all)
                    chosen.size <= 3 -> chosen.joinToString { "${it.emoji} ${it.name}" }
                    else -> pluralStringResource(R.plurals.pick_packs_some, chosen.size, chosen.size)
                } + "\n" + stringResource(R.string.setup_unplayed, left, total),
                containerColor = Neon.Card,
                tag = "choose_packs",
                onClick = onChoosePacks
            ) {
                Text("›", style = MaterialTheme.typography.headlineMedium, color = Neon.Muted)
            }
            if (left * 10 < total) {
                Text(
                    stringResource(R.string.setup_running_out),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neon.AmberText
                )
            }
            if (short) startButton()
            Spacer(Modifier.height(4.dp))
        }
    }
    when (picking) {
        PICK_ROUNDS -> OptionDialog(
            title = stringResource(R.string.setup_rounds),
            options = Match.ROUND_OPTIONS,
            selected = settings.rounds,
            recommended = recommendedRounds,
            label = { roundsLabel(it) },
            tag = "rounds",
            onPick = { v -> onChange { it.copy(rounds = v) } },
            onDismiss = { picking = null }
        )

        PICK_TIMER -> OptionDialog(
            title = stringResource(R.string.setup_timer),
            options = Rules.TIMER_OPTIONS,
            selected = settings.discussionSeconds,
            recommended = recommendedTimer,
            label = { timerLabel(it) },
            tag = "timer",
            onPick = { v -> onChange { it.copy(discussionSeconds = v) } },
            onDismiss = { picking = null }
        )
    }
}

private const val PICK_ROUNDS = "rounds"
private const val PICK_TIMER = "timer"

@Composable
private fun roundsLabel(rounds: Int): String = if (rounds == 0) {
    stringResource(R.string.setup_rounds_unlimited)
} else {
    pluralStringResource(R.plurals.setup_rounds_value, rounds, rounds)
}

@Composable
private fun timerLabel(seconds: Int): String = if (seconds == 0) {
    stringResource(R.string.setup_timer_off)
} else {
    stringResource(R.string.setup_timer_minutes, seconds / 60)
}

/** The three modes side by side; the one picked is outlined, and its line of description sits below. */
@Composable
private fun ModePicker(selected: GameMode, onPick: (GameMode) -> Unit, onInfo: () -> Unit) {
    val modes = listOf(
        Triple(GameMode.ORIGINAL, "🕵️", R.string.mode_original to R.string.mode_original_desc),
        Triple(GameMode.MYSTERIOUS, "🎭", R.string.mode_mysterious to R.string.mode_mysterious_desc),
        Triple(GameMode.CHAOS, "🌀", R.string.mode_chaos to R.string.mode_chaos_desc)
    )
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            modes.forEach { (mode, emoji, texts) ->
                val isSelected = mode == selected
                Column(
                    Modifier
                        .weight(1f)
                        .clip(MaterialTheme.shapes.large)
                        .background(if (isSelected) Neon.CardHigh else Neon.Card)
                        .border(2.dp, if (isSelected) Neon.Violet else Color.Transparent, MaterialTheme.shapes.large)
                        .selectable(isSelected, role = Role.RadioButton) { onPick(mode) }
                        .padding(vertical = 14.dp, horizontal = 6.dp)
                        .testTag("mode_${mode.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(emoji, style = MaterialTheme.typography.headlineMedium)
                    Text(
                        stringResource(texts.first),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        val desc = modes.first { it.first == selected }.third.second
        Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(desc),
                color = Neon.Muted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f).testTag("mode_desc")
            )
            TextButton(onClick = onInfo, modifier = Modifier.testTag("modes_info")) {
                Text("ⓘ  " + stringResource(R.string.setup_which_mode), color = Neon.TurquoiseText)
            }
        }
    }
}

/** Rows that belong together, on one card. */
@Composable
private fun SettingsGroup(content: @Composable () -> Unit) = Column(
    Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(Neon.Card)
) { content() }

@Composable
private fun GroupRow(
    leading: String,
    title: String,
    subtitle: String? = null,
    tag: String? = null,
    onClick: (() -> Unit)? = null,
    value: String? = null,
    control: (@Composable () -> Unit)? = null
) = LilloSettingRow(
    title,
    leading = leading,
    subtitle = subtitle,
    containerColor = Color.Transparent,
    tag = tag,
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    onClick = onClick
) {
    if (value != null) {
        Text(value, color = Neon.Muted, style = MaterialTheme.typography.titleMedium)
        Text("  ›", color = Neon.Muted, style = MaterialTheme.typography.titleLarge)
    }
    control?.invoke()
}

@Composable
private fun GroupDivider() =
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = Neon.Muted.copy(alpha = 0.2f))

@Composable
private fun GroupNote(text: String, color: Color = Neon.Muted, tag: String? = null) = Text(
    text,
    color = color,
    style = MaterialTheme.typography.bodyMedium,
    modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
        .then(if (tag != null) Modifier.testTag(tag) else Modifier)
)

/** A short list to choose one value from, the recommended one marked. */
@Composable
private fun OptionDialog(
    title: String,
    options: List<Int>,
    selected: Int,
    recommended: Int,
    label: @Composable (Int) -> String,
    tag: String,
    onPick: (Int) -> Unit,
    onDismiss: () -> Unit
) = AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {},
    dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
    },
    title = { Text(title) },
    text = {
        LazyColumn(Modifier.then(DialogTags).testTag("${tag}_options")) {
            items(options) { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(option == selected, role = Role.RadioButton) {
                            onPick(option)
                            onDismiss()
                        }
                        .padding(vertical = 12.dp)
                        .testTag("${tag}_option_$option"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = option == selected, onClick = null)
                    Text(label(option), modifier = Modifier.padding(start = 12.dp).weight(1f))
                    if (option == recommended) {
                        Text(
                            stringResource(R.string.setup_recommended_short),
                            color = Neon.TurquoiseText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
)

@Composable
private fun Stepper(
    label: String,
    value: Int,
    canLower: Boolean,
    canRaise: Boolean,
    tag: String,
    onChange: (Int) -> Unit
) = Row(
    verticalAlignment = Alignment.CenterVertically
) {
    val fewer = stringResource(R.string.setup_fewer, label)
    val more = stringResource(R.string.setup_more, label)
    IconButton(
        onClick = { onChange(value - 1) },
        enabled = canLower,
        modifier = Modifier.semantics { contentDescription = fewer }.testTag("${tag}_minus")
    ) {
        Text("−", style = MaterialTheme.typography.headlineSmall)
    }
    Text(
        "$value",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(horizontal = 8.dp).testTag(tag)
    )
    IconButton(
        onClick = { onChange(value + 1) },
        enabled = canRaise,
        modifier = Modifier.semantics { contentDescription = more }.testTag("${tag}_plus")
    ) {
        Text("+", style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun NeonSwitch(checked: Boolean, tag: String, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        modifier = Modifier.testTag(tag),
        colors = SwitchDefaults.colors(checkedTrackColor = Neon.Violet, checkedThumbColor = Color.White)
    )
}
