package com.jorgelillo.grouppolls.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jorgelillo.grouppolls.R
import com.jorgelillo.grouppolls.data.LocalState
import com.jorgelillo.grouppolls.domain.Poll
import com.jorgelillo.grouppolls.domain.Split
import com.jorgelillo.grouppolls.domain.Visibility
import com.jorgelillo.grouppolls.ui.theme.Polls
import java.util.Locale
import kotlinx.coroutines.flow.Flow

/** Two tabs: the public feed (one section per language) and the polls you created or were sent. */
@Composable
internal fun HomeScreen(
    feed: List<Pair<String, List<Poll>>>,
    local: LocalState,
    pollFlow: (String) -> Flow<Poll?>,
    onOpen: (String) -> Unit,
    onCreate: () -> Unit,
    onSettings: () -> Unit,
    /** Wall clock, for how long ago and how long left (fixed in screenshot tests). */
    clock: () -> Long = System::currentTimeMillis
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val now = remember { clock() }
    Box(Modifier.fillMaxSize()) {
        Page(
            title = stringResource(R.string.app_name),
            onBack = null,
            actions = {
                IconButton(onClick = onSettings, modifier = Modifier.testTag("settings")) {
                    Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_title))
                }
            }
        ) {
            Row(
                Modifier.fillMaxWidth().clip(
                    CircleShape
                ).background(Polls.Card).border(1.dp, Polls.Line, CircleShape).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Tab(stringResource(R.string.tab_public), tab == 0, onClick = {
                    tab = 0
                }, modifier = Modifier.weight(1f).testTag("tab_public"))
                Tab(stringResource(R.string.tab_mine), tab == 1, onClick = {
                    tab = 1
                }, modifier = Modifier.weight(1f).testTag("tab_mine"))
            }
            if (tab == 0) {
                if (feed.isEmpty()) {
                    Empty("🗳️", stringResource(R.string.feed_empty))
                } else {
                    LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                        feed.forEach { (language, polls) ->
                            item(key = "h_$language") { SectionTitle(languageName(language)) }
                            items(polls, key = { it.code }) { poll ->
                                PollCard(poll, local, now, showVisibility = false) { onOpen(poll.code) }
                            }
                        }
                    }
                }
            } else {
                if (local.myPolls.isEmpty()) {
                    Empty("🔗", stringResource(R.string.mine_empty))
                } else {
                    LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)) {
                        items(local.myPolls, key = { it }) { code ->
                            val poll by pollFlow(code).collectAsState(initial = null)
                            poll?.let { PollCard(it, local, now, showVisibility = true) { onOpen(code) } }
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onCreate,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.create_title), fontWeight = FontWeight.Bold) },
            containerColor = Polls.Ink,
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).testTag("create")
        )
    }
}

@Composable
private fun Tab(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) Polls.Ink else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        color = if (selected) Color.White else Polls.Muted,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun Empty(emoji: String, text: String) {
    Column(Modifier.fillMaxWidth().padding(top = 64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, style = MaterialTheme.typography.displayMedium)
        Text(text, color = Polls.Muted, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
    }
}

@Composable
internal fun PollCard(poll: Poll, local: LocalState, now: Long, showVisibility: Boolean, onClick: () -> Unit) {
    val mine = local.votes[poll.code]
    Column(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Polls.Card)
            .border(1.dp, Polls.Line, MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag("poll_${poll.code}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showVisibility) {
                Text(
                    stringResource(
                        if (poll.visibility ==
                            Visibility.PUBLIC
                        ) {
                            R.string.badge_public
                        } else {
                            R.string.badge_private
                        }
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Polls.Muted,
                    modifier = Modifier.clip(
                        CircleShape
                    ).background(Polls.Paper).padding(horizontal = 8.dp, vertical = 3.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                listOfNotNull(poll.creatorName.ifBlank { null }, age(poll.createdAt, now)).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = Polls.Muted,
                modifier = Modifier.weight(1f)
            )
            if (!poll.isOpen(now)) Text("🔒", style = MaterialTheme.typography.bodySmall)
        }
        Text(
            poll.question,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        if (mine != null || !poll.isOpen(now)) {
            val split = Split.of(poll.redVotes, poll.blueVotes)
            SplitBar(poll)
            Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text(
                    "${poll.red} ${split.red}%",
                    color = Polls.Red,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${split.blue}% ${poll.blue}",
                    color = Polls.Blue,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(poll.red, Polls.RedSoft, Polls.Red, Modifier.weight(1f))
                Chip(poll.blue, Polls.BlueSoft, Polls.Blue, Modifier.weight(1f))
            }
        }
        Text(
            pluralStringResource(R.plurals.votes, poll.totalVotes, poll.totalVotes),
            style = MaterialTheme.typography.bodySmall,
            color = Polls.Muted,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun Chip(text: String, background: Color, color: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.clip(CircleShape).background(background).padding(horizontal = 12.dp, vertical = 8.dp),
        color = color,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}

private fun languageName(code: String): String = Locale.forLanguageTag(code).let {
    it.getDisplayLanguage(it)
}.replaceFirstChar { it.uppercase() }.ifBlank { code }
