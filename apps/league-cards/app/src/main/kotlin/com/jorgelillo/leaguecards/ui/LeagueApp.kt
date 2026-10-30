package com.jorgelillo.leaguecards.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jorgelillo.core.designsystem.AboutTexts
import com.jorgelillo.core.designsystem.ConfettiBurst
import com.jorgelillo.core.designsystem.HeroChip
import com.jorgelillo.core.designsystem.LilloAboutSheet
import com.jorgelillo.core.designsystem.LilloSecondaryButton
import com.jorgelillo.core.designsystem.LilloSummaryHero
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.core.platform.LilloLinks
import com.jorgelillo.core.platform.shareText
import com.jorgelillo.leaguecards.BuildConfig
import com.jorgelillo.leaguecards.R
import com.jorgelillo.leaguecards.domain.Game
import com.jorgelillo.leaguecards.domain.League
import com.jorgelillo.leaguecards.domain.Saved
import com.jorgelillo.leaguecards.domain.Season
import com.jorgelillo.leaguecards.domain.YOU
import com.jorgelillo.leaguecards.ui.theme.Chat

/** Home, or the season: a card, the matchday's group chat, or how it ended. */
@Composable
internal fun LeagueApp(vm: LeagueViewModel) {
    val saved = vm.saved.collectAsStateWithLifecycle().value ?: return
    val league = vm.league
    var playing by rememberSaveable { mutableStateOf(false) }
    var table by rememberSaveable { mutableStateOf(false) }
    var trophies by rememberSaveable { mutableStateOf(false) }
    val season = saved.season?.takeIf { saved.deck == league.name }

    Box(Modifier.fillMaxSize().background(Chat.Ink).then(TestTagsAsResourceIds)) {
        if (trophies) {
            BackHandler { trophies = false }
            TrophiesScreen(league, saved) { trophies = false }
        } else if (!playing || season == null) {
            HomeScreen(
                league,
                saved,
                onContinue = { playing = true },
                onNew = { short ->
                    vm.newSeason(short)
                    playing = true
                },
                onTrophies = { trophies = true }
            )
        } else {
            BackHandler { playing = false }
            val report = season.report
            when {
                season.reportPending && report != null -> ReportScreen(
                    league,
                    report,
                    season.justUnlocked,
                    season.over,
                    onNext = vm::dismissReport,
                    onTable = { table = true },
                    onBack = { playing = false }
                )

                season.over -> EndScreen(league, saved, season, onNew = vm::newSeason, onHome = {
                    playing = false
                }, onTrophies = {
                    trophies =
                        true
                })

                else -> PlayScreen(
                    league,
                    season,
                    onChoose = vm::choose,
                    onJoker = vm::useJoker,
                    onTable = { table = true },
                    onBack = { playing = false }
                )
            }
            if (table) TableSheet(league, season) { table = false }
        }
    }
}

@Composable
private fun AboutButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier.testTag("about")) {
        Text("ⓘ", fontSize = 22.sp, color = Chat.Muted)
    }
}

@Composable
private fun SecondaryButton(text: String, tag: String, onClick: () -> Unit) =
    LilloSecondaryButton(text, onClick, Modifier.padding(bottom = 8.dp).testTag(tag), contentColor = Chat.Text)

@Composable
internal fun HomeScreen(
    league: League,
    saved: Saved,
    onContinue: () -> Unit,
    onNew: (short: Boolean) -> Unit,
    onTrophies: () -> Unit
) {
    var about by rememberSaveable { mutableStateOf(false) }
    val inProgress = saved.seasonFor(league)
    Page(
        title = null,
        onBack = null,
        sideActionsWhenShort = true,
        actions = { AboutButton({ about = true }) },
        bottom = {
            // On short screens the header (and its ⓘ) gives way to the side column: it goes here.
            if (isShortScreen()) AboutButton({ about = true }, Modifier.align(Alignment.End))
            val full = pluralStringResource(R.plurals.new_season_length, league.matchdays, league.matchdays)
            val short = Game.shortLength(league).let { pluralStringResource(R.plurals.short_season, it, it) }
            if (inProgress != null) {
                BigButton(stringResource(R.string.continue_season, inProgress.matchday), onContinue, "continue")
                Text(
                    stringResource(R.string.autosave),
                    color = Chat.Muted,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).testTag("autosave")
                )
                SecondaryButton(full, "new_season") { onNew(false) }
            } else {
                BigButton(full, { onNew(false) }, "new_season")
                Spacer(Modifier.height(12.dp))
            }
            SecondaryButton(short, "new_short_season") { onNew(true) }
        }
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Summary(league, saved, inProgress)
            Spacer(Modifier.height(16.dp))
            ChatBubble(league, "", stringResource(R.string.how_to_play))
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onTrophies, modifier = Modifier.testTag("trophies")) {
                Text(
                    stringResource(
                        R.string.trophies_button,
                        saved.achievements.count { league.achievement(it) != null },
                        league.achievements.size,
                        saved.endingsSeen.size,
                        league.endings.keys.size
                    ),
                    color = Chat.GoldText
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.characters), color = Chat.Muted, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            league.characters.forEach { c ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(c.emoji.ifBlank { c.name }, league.colorOf(c.id), size = 36)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(c.name, color = league.colorOf(c.id), fontWeight = FontWeight.Bold)
                        if (c.tagline.isNotBlank()) {
                            Text(
                                c.tagline,
                                color = Chat.Muted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
    if (about) {
        LilloAboutSheet(
            icon = painterResource(R.drawable.ic_launcher_foreground),
            iconBackground = Chat.InkTop,
            containerColor = Chat.Raised,
            texts = AboutTexts(
                title = stringResource(R.string.app_name),
                version = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                body = stringResource(R.string.about_body),
                rate = stringResource(R.string.about_rate),
                privacy = stringResource(R.string.about_privacy)
            ),
            privacyUrl = LilloLinks.privacyPolicy("league-cards"),
            playStoreUrl = LilloLinks.playStore("com.jorgelillo.leaguecards"),
            onDismiss = { about = false }
        )
    }
}

/** The league at a glance: seasons and titles, the season in progress, the best finish. */
@Composable
private fun Summary(league: League, saved: Saved, inProgress: Season?) {
    val chips = buildList {
        when {
            inProgress != null -> add(
                HeroChip(
                    stringResource(R.string.hero_in_progress, inProgress.matchday, inProgress.matchdays(league)),
                    "hero_in_progress"
                )
            )

            saved.seasonsPlayed == 0 -> add(HeroChip(stringResource(R.string.hero_first_season)))
        }
        saved.bestPosition?.let { add(HeroChip(stringResource(R.string.hero_best, it), "hero_best")) }
    }
    LilloSummaryHero(
        title = league.name,
        subtitle = listOf(
            pluralStringResource(R.plurals.hero_seasons, saved.seasonsPlayed, saved.seasonsPlayed),
            pluralStringResource(R.plurals.hero_titles, saved.titles, saved.titles)
        ).joinToString(" · "),
        colors = Chat.Hero,
        emoji = "😭",
        chips = chips
    )
}

@Composable
private fun EndScreen(
    league: League,
    saved: Saved,
    season: Season,
    onNew: () -> Unit,
    onHome: () -> Unit,
    onTrophies: () -> Unit
) {
    val ending = season.ending ?: return
    val context = LocalContext.current
    val resources = LocalResources.current
    val position = Game.position(season)
    Box(Modifier.fillMaxSize()) {
        Page(
            title = null,
            onBack = onHome,
            bottom = {
                BigButton(stringResource(R.string.new_season), onNew, "new_season")
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        val unlocked = season.unlocked.mapNotNull { league.achievement(it) }
                        val text =
                            resources.getString(
                                R.string.share_text,
                                resources.getString(R.string.app_name),
                                ending.text,
                                season.matchday - 1,
                                position
                            ) +
                                if (unlocked.isEmpty()) {
                                    ""
                                } else {
                                    "\n" +
                                        unlocked.joinToString(" · ") { it.emoji + " " + it.title }
                                }
                        context.shareText(text)
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("share")
                ) { Text(stringResource(R.string.share), color = Chat.Text) }
            }
        ) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (ending.champion) "🏆" else "😭", fontSize = 80.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    ending.text,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("ending")
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.ending_stats, season.matchday - 1, position),
                    color = Chat.Muted,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = onTrophies, modifier = Modifier.testTag("trophies")) {
                    Text(
                        stringResource(R.string.endings_discovered, saved.endingsSeen.size, league.endings.keys.size),
                        color = Chat.GoldText
                    )
                }
                val unlocked = season.unlocked.mapNotNull { league.achievement(it) }
                if (unlocked.isNotEmpty()) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(Chat.Gold.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            stringResource(R.string.season_achievements, unlocked.size),
                            color = Chat.GoldText,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(unlocked.joinToString(" ") { it.emoji }, style = MaterialTheme.typography.headlineSmall)
                        Text(
                            unlocked.joinToString(" · ") { it.title },
                            color = Chat.Text,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Standings(league, season)
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.record, saved.seasonsPlayed, saved.titles),
                    color = Chat.Muted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        ConfettiBurst(key = ending.takeIf { it.champion }, colors = Chat.Confetti, modifier = Modifier.fillMaxSize())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableSheet(league: League, season: Season, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Chat.Raised, modifier = TestTagsAsResourceIds) {
        Column(
            Modifier.verticalScroll(
                rememberScrollState()
            ).padding(horizontal = 24.dp).padding(bottom = 32.dp).testTag("standings")
        ) {
            Text(
                stringResource(R.string.table),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Standings(league, season)
        }
    }
}

/** The league table, the player's row highlighted: in the table sheet, and beside the card on tablets. */
@Composable
internal fun Standings(league: League, season: Season) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Game.standings(season).forEachIndexed { i, (id, points) ->
            val mine = id == YOU
            // Grey and the player's own green don't read on their green row.
            val muted = if (mine) Chat.Text else Chat.Muted
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (mine) Chat.Mine else Chat.Bubble, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${i + 1}", modifier = Modifier.width(28.dp), color = muted, fontWeight = FontWeight.Bold)
                Column(Modifier.weight(1f)) {
                    val team = league.character(id)?.team.orEmpty()
                    Text(
                        team.ifBlank {
                            league.nameOf(id)
                        },
                        color = if (mine) Chat.Text else league.colorOf(id),
                        fontWeight = FontWeight.Bold
                    )
                    if (team.isNotBlank()) {
                        Text(
                            league.nameOf(id),
                            color = muted,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Text("$points", fontWeight = FontWeight.Black)
            }
        }
    }
}
