package com.jorgelillo.grouppolls.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.grouppolls.R
import com.jorgelillo.grouppolls.data.LocalState
import com.jorgelillo.grouppolls.data.MyVote
import com.jorgelillo.grouppolls.domain.Poll
import com.jorgelillo.grouppolls.domain.Side
import com.jorgelillo.grouppolls.domain.Split
import com.jorgelillo.grouppolls.domain.Visibility
import com.jorgelillo.grouppolls.domain.Vote
import com.jorgelillo.grouppolls.domain.leader
import com.jorgelillo.grouppolls.ui.theme.Polls
import kotlinx.coroutines.delay

/** Vote on one poll (tap a half, then guess the winner) or see its results. */
@Composable
fun PollScreen(
    poll: Poll?,
    votes: List<Vote>,
    local: LocalState,
    userId: String,
    /** Just created: open the share sheet once. */
    shareOnOpen: Boolean,
    onVote: (Poll, Side, Side) -> Unit,
    onClose: (Poll) -> Unit,
    onReport: (Poll, Boolean) -> Unit,
    onResolvePrediction: (Poll) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val now = remember(poll) { System.currentTimeMillis() }
    var notFound by remember { mutableStateOf(false) }
    LaunchedEffect(poll) {
        if (poll == null) {
            delay(4_000)
            notFound = true
        }
    }
    LaunchedEffect(poll) { poll?.let(onResolvePrediction) }
    val shareText = poll?.let { stringResource(R.string.share_text, it.question) }.orEmpty()
    var shared by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(poll != null) {
        if (poll != null && shareOnOpen && !shared) {
            shared = true
            sharePoll(context, poll, shareText)
        }
    }

    Page(
        title = null,
        onBack = onBack,
        actions = {
            if (poll != null) {
                IconButton(onClick = { sharePoll(context, poll, shareText) }, modifier = Modifier.testTag("share")) {
                    Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.action_share))
                }
            }
        }
    ) {
        when {
            poll == null && notFound -> Message("🤷", stringResource(R.string.poll_not_found))

            poll == null -> Message("⏳", stringResource(R.string.loading))

            else -> {
                val mine = local.votes[poll.code]
                val canVote = mine == null && poll.isOpen(now)
                Header(poll, now)
                AnimatedContent(canVote, transitionSpec = { fadeIn() togetherWith fadeOut() }, modifier = Modifier.weight(1f), label = "poll") { voting ->
                    if (voting) {
                        Voting(poll) { side, prediction -> onVote(poll, side, prediction) }
                    } else {
                        Results(poll, votes, mine, local, userId, now, onClose, onReport)
                    }
                }
            }
        }
    }
}

@Composable
private fun Message(emoji: String, text: String) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(emoji, fontSize = 64.sp)
        Text(text, color = Polls.Muted, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
    }
}

@Composable
private fun Header(poll: Poll, now: Long) {
    val closesAt = poll.closesAt
    val meta = buildList {
        poll.creatorName.ifBlank { null }?.let { add(stringResource(R.string.poll_by, it)) }
        add(stringResource(if (poll.visibility == Visibility.PUBLIC) R.string.badge_public else R.string.badge_private))
        add(
            when {
                !poll.isOpen(now) -> stringResource(R.string.poll_closed)
                closesAt == null -> stringResource(R.string.poll_no_limit)
                else -> stringResource(R.string.poll_closes_in, remaining(closesAt - now))
            }
        )
    }
    Text(meta.joinToString(" · "), color = Polls.Muted, style = MaterialTheme.typography.bodySmall)
    Text(
        poll.question,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Black,
        modifier = Modifier.padding(top = 6.dp, bottom = 16.dp).testTag("question")
    )
}

@Composable
private fun remaining(millis: Long): String {
    val hours = (millis / 3_600_000).coerceAtLeast(0)
    return if (hours >= 24) stringResource(R.string.age_days, hours / 24) else stringResource(R.string.age_hours, hours.coerceAtLeast(1))
}

/** Step 1: tap the red or the blue half. Step 2: guess which one wins. */
@Composable
private fun Voting(poll: Poll, onDone: (Side, Side) -> Unit) {
    var side by rememberSaveable { mutableStateOf<Side?>(null) }
    val chosen = side
    if (chosen == null) {
        Column(Modifier.fillMaxSize().padding(bottom = 16.dp).clip(MaterialTheme.shapes.extraLarge)) {
            Half(poll.red, Side.RED, Modifier.weight(1f)) { side = Side.RED }
            Half(poll.blue, Side.BLUE, Modifier.weight(1f)) { side = Side.BLUE }
        }
    } else {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.you_chose), color = Polls.Muted)
            Text(poll.label(chosen), color = Polls.color(chosen), fontSize = 28.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(Modifier.height(32.dp))
            Text("🔮", fontSize = 48.sp)
            Text(
                stringResource(R.string.predict_question),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Text(stringResource(R.string.predict_desc), color = Polls.Muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PredictButton(poll.red, Side.RED, Modifier.weight(1f)) { onDone(chosen, Side.RED) }
                PredictButton(poll.blue, Side.BLUE, Modifier.weight(1f)) { onDone(chosen, Side.BLUE) }
            }
            TextButton(onClick = { side = null }, modifier = Modifier.padding(top = 16.dp).testTag("change_side")) {
                Text(stringResource(R.string.change_side), color = Polls.Muted)
            }
        }
    }
}

@Composable
private fun Half(label: String, side: Side, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.fillMaxWidth().background(Polls.fill(side)).clickable(onClick = onClick).testTag("vote_${side.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (side == Side.RED) "🔴" else "🔵", fontSize = 32.sp)
            Text(label, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}

@Composable
private fun PredictButton(label: String, side: Side, modifier: Modifier, onClick: () -> Unit) {
    Text(
        label,
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(Polls.soft(side))
            .border(2.dp, Polls.color(side), MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp, horizontal = 8.dp)
            .testTag("predict_${side.name.lowercase()}"),
        color = Polls.color(side),
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun Results(
    poll: Poll,
    votes: List<Vote>,
    mine: MyVote?,
    local: LocalState,
    userId: String,
    now: Long,
    onClose: (Poll) -> Unit,
    onReport: (Poll, Boolean) -> Unit
) {
    var confirmClose by rememberSaveable { mutableStateOf(false) }
    var reporting by rememberSaveable { mutableStateOf(false) }
    val split = Split.of(poll.redVotes, poll.blueVotes)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ResultRow(poll.red, split.red, Side.RED, mine?.side == Side.RED)
        Spacer(Modifier.height(10.dp))
        ResultRow(poll.blue, split.blue, Side.BLUE, mine?.side == Side.BLUE)
        Text(
            pluralStringResource(R.plurals.votes, poll.totalVotes, poll.totalVotes),
            color = Polls.Muted,
            modifier = Modifier.padding(top = 10.dp).testTag("total_votes")
        )
        if (mine != null) {
            val leader = poll.leader()
            Card {
                Text(stringResource(R.string.you_voted, poll.label(mine.side)), fontWeight = FontWeight.Bold)
                Text(
                    when {
                        !poll.isOpen(now) && leader == mine.prediction -> stringResource(R.string.prediction_right)
                        !poll.isOpen(now) && leader != null -> stringResource(R.string.prediction_wrong)
                        else -> stringResource(R.string.prediction_pending, poll.label(mine.prediction))
                    },
                    color = Polls.Muted,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (leader != null && mine.side == leader && poll.totalVotes > 1) {
                    Text(stringResource(R.string.with_majority, maxOf(split.red, split.blue)), color = Polls.Muted)
                }
            }
        }
        if (poll.visibility == Visibility.PRIVATE && votes.isNotEmpty()) {
            SectionTitle(stringResource(R.string.who_voted))
            Side.entries.forEach { side ->
                val names = votes.filter { it.side == side }.map { it.voterName?.ifBlank { null } ?: "🙈" }
                if (names.isNotEmpty()) {
                    Row(Modifier.padding(bottom = 6.dp), verticalAlignment = Alignment.Top) {
                        Dot(Polls.color(side), 12)
                        Text(
                            "  ${poll.label(side)}: " + names.joinToString(", "),
                            modifier = Modifier.testTag("voters_${side.name.lowercase()}")
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        if (poll.creatorId == userId && poll.isOpen(now)) {
            TextButton(onClick = { confirmClose = true }, modifier = Modifier.testTag("close_poll")) {
                Text("🔒  " + stringResource(R.string.close_poll), color = Polls.Ink)
            }
        }
        if (poll.visibility == Visibility.PUBLIC && poll.creatorId != userId) {
            val reported = poll.code in local.reported
            TextButton(onClick = { reporting = true }, enabled = !reported, modifier = Modifier.testTag("report")) {
                Text("⚠️  " + stringResource(if (reported) R.string.reported else R.string.report), color = Polls.Muted)
            }
        }
    }
    if (confirmClose) {
        AlertDialog(
            modifier = DialogTags,
            onDismissRequest = { confirmClose = false },
            title = { Text(stringResource(R.string.close_poll)) },
            text = { Text(stringResource(R.string.close_poll_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmClose = false
                    onClose(poll)
                }, modifier = Modifier.testTag("confirm_close")) {
                    Text(stringResource(R.string.close_poll), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { confirmClose = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    if (reporting) {
        var hide by rememberSaveable { mutableStateOf(false) }
        AlertDialog(
            modifier = DialogTags,
            onDismissRequest = { reporting = false },
            title = { Text(stringResource(R.string.report_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.report_desc))
                    if (poll.creatorName.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp).clickable { hide = !hide }) {
                            Checkbox(checked = hide, onCheckedChange = { hide = it }, modifier = Modifier.testTag("hide_creator"))
                            Text(stringResource(R.string.hide_creator, poll.creatorName))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    reporting = false
                    onReport(poll, hide)
                }, modifier = Modifier.testTag("confirm_report")) {
                    Text(stringResource(R.string.report), color = Polls.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { reporting = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun ResultRow(label: String, percent: Int, side: Side, mine: Boolean) {
    val fraction by animateFloatAsState(percent / 100f, tween(900), label = "bar")
    Box(
        Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(MaterialTheme.shapes.large)
            .background(Polls.soft(side))
            .then(if (mine) Modifier.border(3.dp, Polls.color(side), MaterialTheme.shapes.large) else Modifier)
            .testTag("result_${side.name.lowercase()}")
    ) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(Polls.fill(side)))
        Row(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text((if (mine) "✓ " else "") + label, fontWeight = FontWeight.Black, color = if (fraction > 0.35f) Color.White else Polls.color(side), modifier = Modifier.weight(1f))
            Text("$percent%", fontWeight = FontWeight.Black, fontSize = 22.sp, color = if (fraction > 0.85f) Color.White else Polls.color(side))
        }
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(top = 16.dp).clip(MaterialTheme.shapes.large).background(Polls.Card).border(1.dp, Polls.Line, MaterialTheme.shapes.large).padding(16.dp)
    ) { content() }
}
