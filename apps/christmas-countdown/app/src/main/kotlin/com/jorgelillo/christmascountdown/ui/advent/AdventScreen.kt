package com.jorgelillo.christmascountdown.ui.advent

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.christmascountdown.R
import com.jorgelillo.christmascountdown.domain.AdventDoor
import com.jorgelillo.christmascountdown.ui.about.PLAY_STORE_URL
import com.jorgelillo.christmascountdown.ui.theme.ChristmasColors
import com.jorgelillo.core.designsystem.GlassCard
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.core.platform.shareText

private enum class DoorState { Locked, Openable, Opened }

@Composable
fun AdventScreen(
    state: AdventUiState,
    canOpen: (Int) -> Boolean,
    onOpen: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    var shownDoor by rememberSaveable { mutableStateOf<Int?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            GlassCard(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.advent_title), style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = stringResource(
                            if (state.isSeason) R.string.advent_subtitle_season else R.string.advent_subtitle_locked,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        items(state.doorOrder, key = { it }) { door ->
            val doorState = when {
                door in state.openedDoors -> DoorState.Opened
                canOpen(door) -> DoorState.Openable
                else -> DoorState.Locked
            }
            Door(door, doorState) {
                if (doorState == DoorState.Locked) {
                    Toast.makeText(context, resources.getString(R.string.advent_door_locked, door), Toast.LENGTH_SHORT).show()
                } else {
                    onOpen(door)
                    shownDoor = door
                }
            }
        }
    }

    shownDoor?.let { door ->
        val content = state.plan[door] ?: return@let
        val uriHandler = LocalUriHandler.current
        AlertDialog(
            onDismissRequest = { shownDoor = null },
            modifier = TestTagsAsResourceIds,
            confirmButton = {
                TextButton(onClick = { shownDoor = null }, modifier = Modifier.testTag("advent_close")) {
                    Text(stringResource(R.string.advent_close))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        context.shareText(resources.getString(R.string.advent_share_text, door, resources.doorText(content), PLAY_STORE_URL))
                    },
                    modifier = Modifier.testTag("advent_share"),
                ) { Text(stringResource(R.string.advent_share)) }
            },
            icon = { Text(if (content is AdventDoor.Song) "🎶" else "🎁", fontSize = 40.sp) },
            title = { Text(stringResource(R.string.advent_door, door)) },
            text = {
                when (content) {
                    is AdventDoor.Activity -> Text(stringResource(content.activity.text()), textAlign = TextAlign.Center)
                    is AdventDoor.Song -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.advent_song_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.advent_song_text, content.song.title, content.song.artist),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Button(
                            onClick = { uriHandler.openUri(content.song.url) },
                            modifier = Modifier.padding(top = 12.dp).testTag("advent_play"),
                        ) { Text("▶ " + stringResource(R.string.advent_song_play)) }
                    }
                }
            },
        )
    }
}

@Composable
private fun Door(number: Int, state: DoorState, onClick: () -> Unit) {
    val shape = MaterialTheme.shapes.small
    val background = when (state) {
        DoorState.Openable -> Brush.linearGradient(listOf(ChristmasColors.Berry, Color(0xFF9B1B30)))
        DoorState.Opened -> Brush.linearGradient(listOf(Color(0x332A9D5C), Color(0x222A9D5C)))
        DoorState.Locked -> Brush.linearGradient(listOf(Color(0x14FFFFFF), Color(0x0AFFFFFF)))
    }
    val borderColor = when (state) {
        DoorState.Openable -> ChristmasColors.Gold
        DoorState.Opened -> ChristmasColors.Pine
        DoorState.Locked -> Color(0x22FFFFFF)
    }
    Box(
        modifier = Modifier
            .aspectRatio(0.85f)
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            DoorState.Opened -> Text("🎁", fontSize = 26.sp)
            else -> Text(
                text = number.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = if (state == DoorState.Locked) Color(0x88FFFFFF) else Color.White,
            )
        }
        if (state == DoorState.Locked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Color(0x55FFFFFF),
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(12.dp),
            )
        }
    }
}
