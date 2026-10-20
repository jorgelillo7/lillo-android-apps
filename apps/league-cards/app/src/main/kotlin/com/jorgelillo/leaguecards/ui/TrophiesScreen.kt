package com.jorgelillo.leaguecards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.LilloSectionTitle
import com.jorgelillo.leaguecards.R
import com.jorgelillo.leaguecards.domain.League
import com.jorgelillo.leaguecards.domain.Saved
import com.jorgelillo.leaguecards.domain.SeasonRecord
import com.jorgelillo.leaguecards.ui.theme.Chat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Endings discovered (the rest locked), achievements, and the last seasons played. */
@Composable
fun TrophiesScreen(league: League, saved: Saved, onBack: () -> Unit) {
    Page(title = stringResource(R.string.trophies), onBack = onBack) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            val endings = league.endings.keys
            Section(stringResource(R.string.endings_count, endings.count { it in saved.endingsSeen }, endings.size))
            endings.forEach { key ->
                val seen = key in saved.endingsSeen
                Tile(
                    emoji = if (seen) endingEmoji(key) else "🔒",
                    title = if (seen) league.endings.text(key).replace("{position}", "N") else stringResource(R.string.locked),
                    subtitle = null,
                    dim = !seen,
                    tag = "ending_$key"
                )
            }

            Section(stringResource(R.string.achievements_count, league.achievements.count { it.id in saved.achievements }, league.achievements.size))
            league.achievements.forEach { a ->
                val done = a.id in saved.achievements
                Tile(if (done) a.emoji else "🔒", a.title, a.description, dim = !done, tag = "achievement_${a.id}")
            }

            Section(stringResource(R.string.history))
            if (saved.history.isEmpty()) {
                Text(stringResource(R.string.history_empty), color = Chat.Muted)
            }
            saved.history.forEach { HistoryRow(league, it) }
        }
    }
}

/** Emoji for an ending key ([com.jorgelillo.leaguecards.domain.Ending.key]). */
fun endingEmoji(key: String): String = when (key) {
    "champion" -> "🏆"
    "finished" -> "🏁"
    "money_low" -> "🪙"
    "money_high" -> "🤑"
    "squad_low" -> "🩼"
    "squad_high" -> "🦸"
    "patience_low" -> "🚪"
    "patience_high" -> "👑"
    "tears_low" -> "🗿"
    "tears_high" -> "🔇"
    else -> "❔"
}

@Composable
private fun Section(title: String) = LilloSectionTitle(title, Chat.Muted, Modifier.padding(top = 20.dp, bottom = 8.dp), fontWeight = FontWeight.Bold)

@Composable
private fun Tile(emoji: String, title: String, subtitle: String?, dim: Boolean, tag: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(Chat.Bubble, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .alpha(if (dim) 0.55f else 1f)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, color = Chat.Text, fontWeight = if (dim) FontWeight.Normal else FontWeight.Bold)
            subtitle?.let { Text(it, color = Chat.Muted, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun HistoryRow(league: League, r: SeasonRecord) {
    val date = LocalDate.ofEpochDay(r.day).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(Chat.Bubble, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(endingEmoji(r.ending), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.history_line, r.position, r.points, r.matchdaysPlayed), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(date, color = Chat.Muted, style = MaterialTheme.typography.labelSmall)
        }
        if (r.achievements.isNotEmpty()) {
            Text(r.achievements.mapNotNull { league.achievement(it)?.emoji }.joinToString(" "))
        }
    }
}
