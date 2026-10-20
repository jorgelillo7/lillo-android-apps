package com.jorgelillo.christmascountdown.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorgelillo.christmascountdown.ChristmasCountdownApplication
import com.jorgelillo.christmascountdown.R
import com.jorgelillo.christmascountdown.domain.CountdownState
import com.jorgelillo.christmascountdown.reminders.SeasonNotifications
import com.jorgelillo.christmascountdown.ui.about.AboutSheet
import com.jorgelillo.christmascountdown.ui.about.PLAY_STORE_URL
import com.jorgelillo.christmascountdown.ui.advent.AdventScreen
import com.jorgelillo.christmascountdown.ui.advent.AdventViewModel
import com.jorgelillo.christmascountdown.ui.components.Snowfall
import com.jorgelillo.christmascountdown.ui.countdown.CountdownScreen
import com.jorgelillo.christmascountdown.ui.countdown.CountdownViewModel
import com.jorgelillo.christmascountdown.ui.theme.ChristmasColors
import com.jorgelillo.core.designsystem.GlassCard
import com.jorgelillo.core.designsystem.GradientBackground
import com.jorgelillo.core.platform.shareText
import kotlinx.coroutines.launch

private enum class Tab(val label: Int) { Countdown(R.string.tab_countdown), Advent(R.string.tab_advent) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChristmasApp(app: ChristmasCountdownApplication, openAdvent: Boolean = false) {
    val countdownViewModel: CountdownViewModel = viewModel(factory = CountdownViewModel.factory(app))
    val adventViewModel: AdventViewModel = viewModel(factory = AdventViewModel.factory(app.settings))
    val countdown by countdownViewModel.uiState.collectAsStateWithLifecycle()
    val advent by adventViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var tab by rememberSaveable { mutableIntStateOf(if (openAdvent) Tab.Advent.ordinal else Tab.Countdown.ordinal) }
    // "Shall we remind you?": Android 13+ needs the permission; asked in context, once.
    var canNotify by remember { mutableStateOf(SeasonNotifications.canNotify(context)) }
    val promptDismissed by app.settings.reminderPromptDismissed.collectAsStateWithLifecycle(initialValue = true)
    val scope = rememberCoroutineScope()
    val askPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        canNotify = granted
        scope.launch { app.settings.dismissReminderPrompt() }
    }
    val reminderPrompt: (@Composable () -> Unit)? = if (!canNotify && !promptDismissed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        {
            ReminderPrompt(
                onYes = { askPermission.launch(Manifest.permission.POST_NOTIFICATIONS) },
                onNo = { scope.launch { app.settings.dismissReminderPrompt() } }
            )
        }
    } else {
        null
    }
    var showAbout by rememberSaveable { mutableStateOf(false) }

    // Carols only play while the app is visible.
    LifecycleStartEffect(countdown.musicEnabled) {
        if (countdown.musicEnabled) app.musicPlayer.play()
        onStopOrDispose { app.musicPlayer.stop() }
    }

    GradientBackground(ChristmasColors.Background) {
        Snowfall(Modifier.fillMaxSize())
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { countdownViewModel.setMusicEnabled(!countdown.musicEnabled) }) {
                            Icon(
                                painter = painterResource(if (countdown.musicEnabled) R.drawable.ic_music_note else R.drawable.ic_music_off),
                                contentDescription = stringResource(
                                    if (countdown.musicEnabled) R.string.action_music_off else R.string.action_music_on
                                ),
                                tint = if (countdown.musicEnabled) ChristmasColors.Gold else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { context.shareCountdown(countdown.countdown, countdown.sleepsMode) }) {
                            Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.action_share))
                        }
                        IconButton(onClick = { showAbout = true }) {
                            Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.action_about))
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(containerColor = Color.Black.copy(alpha = 0.25f)) {
                    Tab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item.ordinal,
                            onClick = { tab = item.ordinal },
                            icon = {
                                Icon(
                                    imageVector = if (item == Tab.Countdown) Icons.Filled.Star else Icons.Filled.DateRange,
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(item.label)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                selectedTextColor = ChristmasColors.Gold,
                                indicatorColor = ChristmasColors.Gold
                            )
                        )
                    }
                }
            }
        ) { padding ->
            when (Tab.entries[tab]) {
                Tab.Countdown -> CountdownScreen(
                    state = countdown,
                    onSleepsModeChange = countdownViewModel::setSleepsMode,
                    reminderPrompt = reminderPrompt,
                    modifier = Modifier.padding(padding)
                )

                Tab.Advent -> AdventScreen(
                    state = advent,
                    canOpen = adventViewModel::canOpen,
                    onOpen = adventViewModel::open,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    if (showAbout) AboutSheet(onDismiss = { showAbout = false })
}

/** The shared text follows the mode on screen: "81 days and 13 hours" or "82 sleeps". */
private fun Context.shareCountdown(state: CountdownState, sleepsMode: Boolean) {
    val message = when (state) {
        CountdownState.ChristmasDay -> getString(R.string.share_text_christmas)

        is CountdownState.Counting -> if (sleepsMode) {
            val sleeps = state.sleeps.toInt()
            resources.getQuantityString(R.plurals.share_text_sleeps, sleeps, sleeps)
        } else {
            val days = state.timeLeft.days.toInt()
            val hours = state.timeLeft.hours
            val daysText = resources.getQuantityString(R.plurals.share_days, days, days)
            val hoursText = resources.getQuantityString(R.plurals.share_hours, hours, hours)
            when {
                days > 0 && hours > 0 -> getString(R.string.share_text_time_two, daysText, hoursText)
                days > 0 -> getString(R.string.share_text_time_one, daysText)
                hours > 0 -> getString(R.string.share_text_time_one, hoursText)
                else -> getString(R.string.share_text_time_soon)
            }
        }
    }
    shareText("$message\n$PLAY_STORE_URL", getString(R.string.share_chooser))
}

@Composable
private fun ReminderPrompt(onYes: () -> Unit, onNo: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Text(stringResource(R.string.reminder_prompt_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.reminder_prompt_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onYes,
                    colors = ButtonDefaults.buttonColors(containerColor = ChristmasColors.Gold, contentColor = Color.Black),
                    modifier = Modifier.testTag("reminders_yes")
                ) { Text(stringResource(R.string.reminder_prompt_yes)) }
                TextButton(onClick = onNo, modifier = Modifier.testTag("reminders_no")) { Text(stringResource(R.string.reminder_prompt_no)) }
            }
        }
    }
}
