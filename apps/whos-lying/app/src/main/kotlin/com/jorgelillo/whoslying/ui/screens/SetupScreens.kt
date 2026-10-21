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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.LilloSettingRow
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.domain.GameMode
import com.jorgelillo.whoslying.domain.GameSettings
import com.jorgelillo.whoslying.domain.Match
import com.jorgelillo.whoslying.domain.Rules
import com.jorgelillo.whoslying.domain.WordPack
import com.jorgelillo.whoslying.ui.theme.Neon

@Composable
fun HomeScreen(onPlay: () -> Unit, onHowTo: () -> Unit, onModes: () -> Unit, onPacks: () -> Unit, onAbout: () -> Unit) {
    Page(title = null, onBack = null, bottom = {
        BigButton(stringResource(R.string.play), onPlay, Modifier.testTag("play"))
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HomeChip("📖", stringResource(R.string.rules_short), onHowTo, Modifier.weight(1f).testTag("how_to"))
            HomeChip("🎭", stringResource(R.string.modes_short), onModes, Modifier.weight(1f).testTag("modes"))
            HomeChip("🗂️", stringResource(R.string.packs_short), onPacks, Modifier.weight(1f).testTag("packs"))
        }
        TextButton(onClick = onAbout, modifier = Modifier.align(Alignment.CenterHorizontally).testTag("about")) {
            Text("⚙️  " + stringResource(R.string.action_about), color = Neon.Muted)
        }
    }) {
        Spacer(Modifier.weight(1f))
        Emoji("🕵️", size = 110, modifier = Modifier.fillMaxWidth())
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
        Spacer(Modifier.weight(1.3f))
    }
}

@Composable
private fun HomeChip(emoji: String, label: String, onClick: () -> Unit, modifier: Modifier) {
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
fun PlayersScreen(
    players: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    var draft by rememberSaveable { mutableStateOf("") }
    fun add() {
        if (draft.isNotBlank()) onAdd(draft)
        draft = ""
    }
    Page(
        title = stringResource(R.string.players_title),
        onBack = onBack,
        modifier = Modifier.imePadding(),
        bottom = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = { Text(stringResource(R.string.players_add_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { add() }),
                    modifier = Modifier.weight(1f).testTag("player_name")
                )
                Spacer(Modifier.width(10.dp))
                FilledIconButton(onClick = { add() }, enabled = draft.isNotBlank(), modifier = Modifier.size(52.dp).testTag("add_player")) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.players_add))
                }
            }
            Spacer(Modifier.height(12.dp))
            BigButton(
                stringResource(R.string.continue_),
                onContinue,
                enabled = players.size >= Rules.MIN_PLAYERS,
                modifier = Modifier.testTag("continue")
            )
        }
    ) {
        Text(
            if (players.size < Rules.MIN_PLAYERS) {
                pluralStringResource(R.plurals.players_need_more, Rules.MIN_PLAYERS, Rules.MIN_PLAYERS)
            } else {
                stringResource(R.string.players_subtitle)
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
                    Text(name, Modifier.weight(1f).padding(horizontal = 14.dp), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { onRemove(name) }) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.players_remove, name), tint = Neon.Pink)
                    }
                }
            }
        }
    }
}

@Composable
fun SetupScreen(
    playerCount: Int,
    settings: GameSettings,
    packs: List<WordPack>,
    /** Words not played yet in the selected packs, out of how many. */
    unplayed: Pair<Int, Int>,
    onChange: ((GameSettings) -> GameSettings) -> Unit,
    onModesInfo: () -> Unit,
    onChoosePacks: () -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    val maxImpostors = Rules.maxImpostors(playerCount, settings.mode).coerceAtLeast(1)
    // On short screens a fixed button would leave almost no room to scroll: it goes at the end of the list.
    val short = isShortScreen()
    val startButton: @Composable () -> Unit = {
        BigButton(
            stringResource(R.string.start),
            onStart,
            enabled = Rules.canStart(playerCount, settings),
            modifier = Modifier.testTag("start")
        )
    }
    Page(
        title = stringResource(R.string.setup_title),
        onBack = onBack,
        bottom = { if (!short) startButton() }
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            TextButton(onClick = onModesInfo, modifier = Modifier.align(Alignment.End).testTag("modes_info")) {
                Text("ⓘ  " + stringResource(R.string.setup_which_mode), color = Neon.Turquoise)
            }
            listOf(
                Triple(GameMode.CLASSIC, R.string.mode_classic to R.string.mode_classic_desc, "🎭"),
                Triple(GameMode.BLIND, R.string.mode_blind to R.string.mode_blind_desc, "🙈"),
                Triple(GameMode.DRIFTER, R.string.mode_drifter to R.string.mode_drifter_desc, "🤷")
            ).forEach { (mode, texts, emoji) ->
                val selected = settings.mode == mode
                val enabled = playerCount >= Rules.minPlayers(mode)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clip(MaterialTheme.shapes.large)
                        .background(if (selected) Neon.CardHigh else Neon.Card)
                        .border(2.dp, if (selected) Neon.Violet else Color.Transparent, MaterialTheme.shapes.large)
                        .clickable(enabled = enabled) { onChange { it.copy(mode = mode) } }
                        .padding(16.dp)
                        .testTag("mode_${mode.name.lowercase()}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(emoji, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(texts.first), fontWeight = FontWeight.Bold, color = if (enabled) Neon.Ink else Neon.Muted)
                        Text(stringResource(texts.second), style = MaterialTheme.typography.bodySmall, color = Neon.Muted)
                    }
                }
            }

            SettingRow(stringResource(R.string.setup_impostors), null) {
                IconButton(onClick = { onChange { it.copy(impostors = it.impostors - 1) } }, enabled = settings.impostors > 1, modifier = Modifier.testTag("impostors_minus")) {
                    Text("−", style = MaterialTheme.typography.headlineSmall)
                }
                Text("${settings.impostors}", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = { onChange { it.copy(impostors = it.impostors + 1) } }, enabled = settings.impostors < maxImpostors, modifier = Modifier.testTag("impostors_plus")) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
            }
            if (settings.mode != GameMode.CLASSIC || settings.drawing) {
                SettingRow(stringResource(R.string.setup_hint), stringResource(R.string.setup_hint_desc)) {
                    NeonSwitch(settings.categoryHint, "hint") { v -> onChange { it.copy(categoryHint = v) } }
                }
            }
            SettingRow(stringResource(R.string.setup_chaos), stringResource(R.string.setup_chaos_desc)) {
                NeonSwitch(settings.chaos, "chaos") { v -> onChange { it.copy(chaos = v) } }
            }
            SettingRow("🎨 " + stringResource(R.string.setup_drawing), stringResource(R.string.setup_drawing_desc)) {
                NeonSwitch(settings.drawing, "drawing") { v -> onChange { it.copy(drawing = v) } }
            }
            val recommendedRounds = Match.recommendedRounds(playerCount)
            SettingRow(
                "🏆 " + stringResource(R.string.setup_rounds),
                stringResource(R.string.setup_rounds_desc) + " · " + stringResource(R.string.setup_recommended, "$recommendedRounds")
            ) {
                val step = Match.ROUND_OPTIONS.indexOf(settings.rounds).coerceAtLeast(0)
                IconButton(onClick = { onChange { it.copy(rounds = Match.ROUND_OPTIONS[step - 1]) } }, enabled = step > 0, modifier = Modifier.testTag("rounds_minus")) {
                    Text("−", style = MaterialTheme.typography.headlineSmall)
                }
                Text(
                    if (settings.rounds == 0) "∞" else "${settings.rounds}",
                    color = if (settings.rounds == recommendedRounds) Neon.Turquoise else Neon.Ink,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(40.dp).testTag("rounds")
                )
                IconButton(onClick = { onChange { it.copy(rounds = Match.ROUND_OPTIONS[step + 1]) } }, enabled = step < Match.ROUND_OPTIONS.lastIndex, modifier = Modifier.testTag("rounds_plus")) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
            }
            val recommendedTimer = Rules.recommendedTimer(playerCount)
            SettingRow(
                "⏱️ " + stringResource(R.string.setup_timer),
                stringResource(R.string.setup_timer_desc) + " · " +
                    stringResource(R.string.setup_recommended, stringResource(R.string.setup_timer_minutes, recommendedTimer / 60))
            ) {
                val step = Rules.TIMER_OPTIONS.indexOf(settings.discussionSeconds).coerceAtLeast(0)
                IconButton(onClick = { onChange { it.copy(discussionSeconds = Rules.TIMER_OPTIONS[step - 1]) } }, enabled = step > 0, modifier = Modifier.testTag("timer_minus")) {
                    Text("−", style = MaterialTheme.typography.headlineSmall)
                }
                Text(
                    if (settings.discussionSeconds == 0) {
                        stringResource(R.string.setup_timer_off)
                    } else {
                        stringResource(R.string.setup_timer_minutes, settings.discussionSeconds / 60)
                    },
                    color = if (settings.discussionSeconds == recommendedTimer) Neon.Turquoise else Neon.Ink,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(64.dp).testTag("timer_value")
                )
                IconButton(onClick = { onChange { it.copy(discussionSeconds = Rules.TIMER_OPTIONS[step + 1]) } }, enabled = step < Rules.TIMER_OPTIONS.lastIndex, modifier = Modifier.testTag("timer_plus")) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
            }

            val (left, total) = unplayed
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(Neon.Card)
                    .clickable(onClick = onChoosePacks)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("choose_packs"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🗂️", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.setup_packs), fontWeight = FontWeight.SemiBold)
                    val chosen = packs.filter { it.id in settings.packIds }
                    Text(
                        when {
                            settings.packIds.isEmpty() -> stringResource(R.string.pick_packs_all)
                            chosen.size <= 3 -> chosen.joinToString { "${it.emoji} ${it.name}" }
                            else -> pluralStringResource(R.plurals.pick_packs_some, chosen.size, chosen.size)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = Neon.Muted,
                        maxLines = 2
                    )
                    Text(stringResource(R.string.setup_unplayed, left, total), style = MaterialTheme.typography.bodySmall, color = Neon.Muted, modifier = Modifier.testTag("unplayed"))
                }
                Text("›", style = MaterialTheme.typography.headlineMedium, color = Neon.Muted)
            }
            if (left * 10 < total) {
                Text(stringResource(R.string.setup_running_out), style = MaterialTheme.typography.bodySmall, color = Neon.Amber, modifier = Modifier.padding(top = 6.dp))
            }
            Spacer(Modifier.height(16.dp))
            if (short) {
                startButton()
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingRow(title: String, subtitle: String?, control: @Composable () -> Unit) = LilloSettingRow(
    title,
    Neon.Card,
    Modifier.padding(vertical = 6.dp),
    subtitle = subtitle,
    subtitleColor = Neon.Muted,
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
) { control() }

@Composable
private fun NeonSwitch(checked: Boolean, tag: String, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        modifier = Modifier.testTag(tag),
        colors = SwitchDefaults.colors(checkedTrackColor = Neon.Violet, checkedThumbColor = Color.White)
    )
}
