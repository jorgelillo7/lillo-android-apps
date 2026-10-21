package com.jorgelillo.decisionwheel.ui.wheel

import android.os.Build
import android.text.format.DateUtils
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.ConfettiBurst
import com.jorgelillo.decisionwheel.R
import com.jorgelillo.decisionwheel.domain.AppState
import com.jorgelillo.decisionwheel.domain.DecisionEngine
import com.jorgelillo.decisionwheel.domain.Spin
import com.jorgelillo.decisionwheel.domain.Wheel
import com.jorgelillo.decisionwheel.ui.home.FavoriteButton
import com.jorgelillo.decisionwheel.ui.theme.WheelColors
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.random.Random
import kotlinx.coroutines.launch

private val FrictionEasing = Easing { Spin.easeOut(it) }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WheelScreen(
    wheel: Wheel,
    state: AppState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAccept: (String) -> Unit,
    onClearHistory: () -> Unit,
    onTick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    val snackbar = remember { SnackbarHostState() }
    val savedText = stringResource(R.string.saved_to_history)

    var vetoed by rememberSaveable(wheel.id) { mutableStateOf(listOf<String>()) }
    var winner by rememberSaveable(wheel.id) { mutableStateOf<String?>(null) }
    var spinning by remember { mutableStateOf(false) }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    var celebration by remember { mutableStateOf<Int?>(null) }
    val rotation = remember(wheel.id) { Animatable(0f) }

    val history = state.historyOf(wheel.id)
    val weights = DecisionEngine.weights(
        options = wheel.options,
        vetoed = vetoed.toSet(),
        history = history,
        nowEpochMillis = System.currentTimeMillis(),
        avoidRepeats = state.avoidRepeats
    )
    val segments = DecisionEngine.segments(weights)
    val canSpin = segments.size >= 2 && !spinning
    val chipColors = segmentColors(wheel.options.size)
    val colorByOption = wheel.options.zip(chipColors).toMap()

    fun spin(fling: Float? = null) {
        if (!canSpin) return
        val plan = Spin.plan(rotation.value.toDouble(), Random.Default, fling)
        winner = null
        spinning = true
        scope.launch {
            var last = rotation.value.toDouble()
            var lastTickAt = 0L
            rotation.animateTo(plan.targetDegrees.toFloat(), tween(plan.durationMillis, easing = FrictionEasing)) {
                val now = value.toDouble()
                if (DecisionEngine.boundariesCrossed(segments, last, now) > 0) {
                    val t = System.nanoTime()
                    if (t - lastTickAt > 25_000_000) { // at most ~40 ticks per second
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        if (state.soundOn) onTick()
                        lastTickAt = t
                    }
                }
                last = now
            }
            rotation.snapTo(rotation.value % 360f)
            winner = DecisionEngine.segmentAtPointer(segments, rotation.value.toDouble())?.option
            view.performHapticFeedback(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.LONG_PRESS
            )
            celebration = (celebration ?: 0) + 1
            spinning = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(wheel.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    FavoriteButton(wheel, onToggleFavorite)
                    IconButton(onClick = { showHistory = true }, modifier = Modifier.testTag("history")) {
                        Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.action_history))
                    }
                    IconButton(onClick = onEdit, enabled = !spinning, modifier = Modifier.testTag("edit")) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.action_edit))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .pointerInput(segments, spinning) {
                        if (spinning) return@pointerInput
                        val tracker = VelocityTracker()
                        var previousAngle = 0f
                        detectDragGestures(
                            onDragStart = { start ->
                                tracker.resetTracking()
                                previousAngle = angleOf(start, size.width / 2f, size.height / 2f)
                            },
                            onDrag = { change, _ ->
                                tracker.addPosition(change.uptimeMillis, change.position)
                                val angle = angleOf(change.position, size.width / 2f, size.height / 2f)
                                var delta = angle - previousAngle
                                if (delta > 180f) delta -= 360f
                                if (delta < -180f) delta += 360f
                                previousAngle = angle
                                if (delta > 0f) scope.launch { rotation.snapTo(rotation.value + delta) }
                            },
                            onDragEnd = {
                                val velocity = tracker.calculateVelocity()
                                val speed = abs(velocity.x) + abs(velocity.y)
                                if (speed > 400f) spin(fling = speed * 2f)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                WheelCanvas(segments, { colorByOption[it] ?: WheelColors.Muted }, rotation.value, winner, Modifier.fillMaxSize())
                ConfettiBurst(celebration, WheelColors.Segments, Modifier.fillMaxSize())
            }

            Spacer(Modifier.height(12.dp))

            AnimatedVisibility(visible = winner == null) {
                Button(
                    onClick = { spin() },
                    enabled = canSpin,
                    modifier = Modifier.widthIn(min = 200.dp).height(56.dp).testTag("spin"),
                    colors = ButtonDefaults.buttonColors(containerColor = WheelColors.Sun, contentColor = WheelColors.Night)
                ) {
                    Text(
                        stringResource(if (spinning) R.string.spinning else R.string.spin),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            AnimatedVisibility(
                visible = winner != null,
                enter = fadeIn() + scaleIn(initialScale = 0.85f) + slideInVertically { it / 3 },
                exit = fadeOut()
            ) {
                ResultCard(
                    winner = winner.orEmpty(),
                    onAccept = {
                        winner?.let(onAccept)
                        winner = null
                        scope.launch { snackbar.showSnackbar(savedText) }
                    },
                    onAgain = { spin() }
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(if (segments.size < 2) R.string.need_two else R.string.veto_hint),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                wheel.options.forEachIndexed { i, option ->
                    val isVetoed = option in vetoed
                    val isRecent = !isVetoed && (weights[option] ?: 1.0) < 1.0
                    OptionChip(
                        label = option,
                        color = chipColors[i],
                        vetoed = isVetoed,
                        recent = isRecent,
                        enabled = !spinning,
                        onClick = {
                            winner = null
                            vetoed = if (isVetoed) vetoed - option else vetoed + option
                        }
                    )
                }
            }
            if (vetoed.isNotEmpty()) {
                TextButton(onClick = { vetoed = emptyList() }, enabled = !spinning) { Text(stringResource(R.string.veto_reset)) }
            }
            if (weights.values.any { it < 1.0 }) {
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.recent_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showHistory) {
        HistorySheet(
            entries = history.map { it.option to it.atEpochMillis },
            onClear = {
                onClearHistory()
                showHistory = false
            },
            onDismiss = { showHistory = false }
        )
    }
}

private fun angleOf(point: Offset, cx: Float, cy: Float): Float = Math.toDegrees(atan2((point.y - cy).toDouble(), (point.x - cx).toDouble())).toFloat()

@Composable
private fun ResultCard(winner: String, onAccept: () -> Unit, onAgain: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(WheelColors.NightRaised)
            .border(1.dp, WheelColors.Sun.copy(alpha = 0.5f), MaterialTheme.shapes.large)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.result_title), color = WheelColors.Sun, style = MaterialTheme.typography.labelLarge)
        Text(
            winner,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onAgain, modifier = Modifier.testTag("again")) { Text(stringResource(R.string.result_again)) }
            Button(
                onClick = onAccept,
                modifier = Modifier.testTag("accept"),
                colors = ButtonDefaults.buttonColors(containerColor = WheelColors.Sun, contentColor = WheelColors.Night)
            ) { Text(stringResource(R.string.result_accept), fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun OptionChip(label: String, color: Color, vetoed: Boolean, recent: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (vetoed) Color.Transparent else color.copy(alpha = 0.18f))
            .border(1.dp, if (vetoed) WheelColors.Muted.copy(alpha = 0.4f) else color.copy(alpha = 0.7f), CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (vetoed) WheelColors.Muted.copy(alpha = 0.4f) else color))
        Spacer(Modifier.size(6.dp))
        Text(
            text = if (recent) "$label ⏱" else label,
            color = if (vetoed) WheelColors.Muted.copy(alpha = 0.6f) else WheelColors.Cream,
            textDecoration = if (vetoed) TextDecoration.LineThrough else null,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistorySheet(entries: List<Pair<String, Long>>, onClear: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = WheelColors.NightRaised) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text(stringResource(R.string.history_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            if (entries.isEmpty()) {
                Text(stringResource(R.string.history_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(Modifier.height((entries.size.coerceAtMost(8) * 44).dp)) {
                    items(entries) { (option, at) ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                            Text(option, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                            Text(
                                if (System.currentTimeMillis() - at < DateUtils.MINUTE_IN_MILLIS) {
                                    stringResource(R.string.history_just_now)
                                } else {
                                    DateUtils.getRelativeTimeSpanString(at).toString()
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                TextButton(onClick = onClear) { Text(stringResource(R.string.history_clear)) }
            }
        }
    }
}
