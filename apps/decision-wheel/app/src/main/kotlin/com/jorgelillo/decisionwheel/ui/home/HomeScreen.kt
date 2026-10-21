package com.jorgelillo.decisionwheel.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.decisionwheel.R
import com.jorgelillo.decisionwheel.domain.AppState
import com.jorgelillo.decisionwheel.domain.Wheel
import com.jorgelillo.decisionwheel.ui.theme.WheelColors
import com.jorgelillo.decisionwheel.ui.wheel.segmentColors

@Composable
internal fun HomeScreen(
    state: AppState,
    onOpen: (Wheel) -> Unit,
    onNew: () -> Unit,
    onAvoidRepeats: (Boolean) -> Unit,
    onSound: (Boolean) -> Unit,
    onAbout: () -> Unit,
    onToggleFavorite: (Wheel) -> Unit,
    onRestorePresets: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNew,
                modifier = Modifier.testTag("new_wheel"),
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_wheel), fontWeight = FontWeight.Bold) },
                containerColor = WheelColors.Sun,
                contentColor = WheelColors.Night
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.home_title),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black
                        )
                        Text(stringResource(R.string.home_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Box {
                        IconButton(onClick = { menu = true }, modifier = Modifier.testTag("more")) {
                            Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.action_more))
                        }
                        DropdownMenu(expanded = menu, onDismissRequest = {
                            menu = false
                        }, modifier = TestTagsAsResourceIds) {
                            CheckItem(stringResource(R.string.settings_avoid_repeats), state.avoidRepeats) {
                                onAvoidRepeats(!state.avoidRepeats)
                            }
                            CheckItem(stringResource(R.string.settings_sound), state.soundOn) {
                                onSound(!state.soundOn)
                            }
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.restore_presets)) },
                                onClick = {
                                    menu = false
                                    onRestorePresets()
                                },
                                modifier = Modifier.testTag("restore_presets")
                            )
                            DropdownMenuItem(text = { Text(stringResource(R.string.action_about)) }, onClick = {
                                menu = false
                                onAbout()
                            })
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }
            items(state.sortedWheels, key = { it.id }) { wheel ->
                WheelCard(wheel, onClick = { onOpen(wheel) }, onToggleFavorite = { onToggleFavorite(wheel) })
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CheckItem(label: String, checked: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        trailingIcon = { if (checked) Icon(Icons.Filled.Check, contentDescription = null, tint = WheelColors.Sun) },
        onClick = onClick
    )
}

@Composable
private fun WheelCard(wheel: Wheel, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    val colors = segmentColors(wheel.options.size)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(WheelColors.NightRaised)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mini wheel preview.
        Canvas(Modifier.size(52.dp)) {
            val sweep = 360f / wheel.options.size.coerceAtLeast(1)
            colors.forEachIndexed { i, color ->
                drawArc(
                    color,
                    -90f + i * sweep,
                    sweep,
                    useCenter = true,
                    topLeft = Offset.Zero,
                    size = Size(size.width, size.height)
                )
            }
            drawCircle(WheelColors.NightRaised, size.minDimension * 0.16f)
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                wheel.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                wheel.options.joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                pluralStringResource(R.plurals.options_count, wheel.options.size, wheel.options.size),
                style = MaterialTheme.typography.labelSmall,
                color = WheelColors.Sun
            )
        }
        FavoriteButton(wheel, onToggleFavorite)
    }
}

/** Star toggle: favourites go first and become launcher shortcuts. */
@Composable
internal fun FavoriteButton(wheel: Wheel, onToggle: () -> Unit) {
    IconButton(onClick = onToggle, modifier = Modifier.testTag("favorite_${wheel.id}")) {
        Icon(
            Icons.Filled.Star,
            contentDescription = stringResource(
                if (wheel.favorite) R.string.favorite_remove else R.string.favorite_add,
                wheel.name
            ),
            tint = if (wheel.favorite) WheelColors.Sun else WheelColors.Muted.copy(alpha = 0.45f)
        )
    }
}
