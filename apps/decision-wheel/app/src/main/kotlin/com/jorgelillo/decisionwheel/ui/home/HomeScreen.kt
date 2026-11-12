package com.jorgelillo.decisionwheel.ui.home

import android.text.format.DateUtils
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jorgelillo.core.designsystem.LilloColors
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.core.designsystem.accentEdge
import com.jorgelillo.core.designsystem.entrance
import com.jorgelillo.core.designsystem.isShortScreen
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
    // Short screens (landscape, handhelds): "New wheel" sits in the title row; a floating button
    // would cover the last wheel's star.
    val short = isShortScreen()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (!short) {
                ExtendedFloatingActionButton(
                    onClick = onNew,
                    modifier = Modifier.testTag("new_wheel"),
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.new_wheel), fontWeight = FontWeight.Bold) },
                    containerColor = LilloColors.current.brand,
                    contentColor = LilloColors.current.onBrand
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 16.dp,
                bottom = if (short) 16.dp else 96.dp
            )
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.home_title),
                        style = MaterialTheme.typography.run { if (short) headlineMedium else displaySmall },
                        color = LilloColors.current.accentText,
                        modifier = Modifier.weight(1f)
                    )
                    if (short) {
                        Button(
                            onClick = onNew,
                            modifier = Modifier.testTag("new_wheel"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LilloColors.current.brand,
                                contentColor = LilloColors.current.onBrand
                            )
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.new_wheel), fontWeight = FontWeight.Bold)
                        }
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
                Spacer(Modifier.height(4.dp))
                Recent(state)
                Spacer(Modifier.height(20.dp))
            }
            items(state.sortedWheels, key = { it.id }) { wheel ->
                WheelCard(wheel, onClick = { onOpen(wheel) }, onToggleFavorite = { onToggleFavorite(wheel) })
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/**
 * What the list below can't say: the last decision and the most picked option, or how to start.
 * A light line under the title, not a card: the wheels themselves are the home screen.
 */
@Composable
private fun Recent(state: AppState) {
    val context = LocalContext.current
    val last = state.history.maxByOrNull { it.atEpochMillis }
    val top = state.history.groupingBy { it.option }.eachCount().maxByOrNull { it.value }?.takeIf { it.value > 1 }
    val lines = buildList {
        if (last == null) {
            add(stringResource(R.string.hero_first_spin) to "hero_first_spin")
        } else {
            val day = DateUtils.formatDateTime(
                context,
                last.atEpochMillis,
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_NO_YEAR
            )
            add(stringResource(R.string.hero_last, last.option, day) to "hero_last")
        }
        top?.let { add(stringResource(R.string.hero_top, it.key, it.value) to "hero_top") }
    }
    Column(Modifier.entrance(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        lines.forEach { (text, tag) ->
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag(tag)
            )
        }
    }
}

@Composable
private fun CheckItem(label: String, checked: Boolean, onClick: () -> Unit) {
    DropdownMenuItem(
        text = { Text(label) },
        trailingIcon = {
            if (checked) Icon(Icons.Filled.Check, contentDescription = null, tint = LilloColors.current.accentText)
        },
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
            .background(MaterialTheme.colorScheme.surfaceContainer)
            // Favourites (first in the list, launcher shortcuts) get the star's colour on their edge.
            .then(if (wheel.favorite) Modifier.accentEdge(LilloColors.current.brand) else Modifier)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mini wheel preview.
        val hole = MaterialTheme.colorScheme.surfaceContainer
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
            drawCircle(hole, size.minDimension * 0.16f)
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
                color = LilloColors.current.accentText
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
            tint = if (wheel.favorite) {
                LilloColors.current.brand
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.45f
                )
            }
        )
    }
}
