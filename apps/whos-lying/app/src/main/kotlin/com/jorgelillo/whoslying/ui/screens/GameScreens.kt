package com.jorgelillo.whoslying.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.TestTagsAsResourceIds
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.domain.Card
import com.jorgelillo.whoslying.domain.Game
import com.jorgelillo.whoslying.domain.Role
import com.jorgelillo.whoslying.ui.theme.Neon
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Dialogs live in their own window: expose their test tags as resource ids too. */
internal val DialogTags = TestTagsAsResourceIds

/**
 * Pass-the-phone reveal. The word sits under a curtain the player slides up while holding it; on
 * release it drops back on its own, so the word is visible as briefly as possible. "Next player"
 * only shows up once the curtain has been lifted.
 */
@Composable
internal fun RevealScreen(game: Game, onDone: () -> Unit, onQuit: () -> Unit) {
    var index by rememberSaveable(game) { mutableIntStateOf(0) }
    var peeked by rememberSaveable(game) { mutableStateOf(false) }
    val card = game.cards[index]
    val lift = remember(index) { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Page(
        title = stringResource(R.string.reveal_counter, index + 1, game.cards.size),
        onBack = onQuit,
        background = Neon.RevealBackdrop,
        bottom = {
            if (peeked) {
                BigButton(stringResource(R.string.reveal_hide), {
                    peeked = false
                    if (index == game.cards.lastIndex) onDone() else index++
                }, color = Neon.Go, contentColor = Neon.Night, modifier = Modifier.testTag("hide"))
            } else {
                Spacer(Modifier.height(60.dp))
            }
        }
    ) {
        // Clipped so the lifted curtain never covers the header.
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
            val height = constraints.maxHeight.toFloat()
            // Short screens (landscape phones, handhelds like the AYN Thor): smaller curtain content so it all fits.
            val compact = maxHeight < 440.dp
            Box(Modifier.fillMaxSize().padding(bottom = 8.dp), contentAlignment = Alignment.BottomCenter) {
                SecretCard(card, compact)
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, -lift.value.roundToInt()) }
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Curtain)
                    .border(2.dp, Color.White.copy(alpha = 0.25f), MaterialTheme.shapes.extraLarge)
                    .pointerInput(index) {
                        val drop: () -> Unit = {
                            scope.launch { lift.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                        }
                        detectVerticalDragGestures(onDragEnd = drop, onDragCancel = drop) { change, dy ->
                            change.consume()
                            scope.launch {
                                lift.snapTo((lift.value - dy).coerceIn(0f, height * 0.85f))
                                if (lift.value > height * PEEK_SHARE) peeked = true
                            }
                        }
                    }
                    .testTag("tap_to_reveal"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (compact) {
                    // Short screens: side by side, so the whole instruction fits.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Emoji("🤫", size = 56)
                        Spacer(Modifier.width(24.dp))
                        Column {
                            Text(
                                stringResource(R.string.reveal_pass_to),
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                card.player,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "⬆  " + stringResource(R.string.reveal_tap),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    Text(
                        stringResource(R.string.reveal_pass_to),
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        card.player,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(32.dp))
                    Emoji("🤫", size = 84)
                    Spacer(Modifier.height(24.dp))
                    Text("⬆", color = Color.White, fontSize = 36.sp)
                    Text(
                        stringResource(R.string.reveal_tap),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

/** How far the curtain must be lifted (share of its height) to count as "seen". */
private const val PEEK_SHARE = 0.22f

private val Curtain = Brush.verticalGradient(listOf(Color(0xFF9B7DFF), Color(0xFF5A35E6)))

@Composable
private fun SecretCard(card: Card, compact: Boolean) {
    val accent = if (card.knowsRole) Neon.Pink else Neon.Turquoise
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Neon.Night)
            .border(3.dp, accent, MaterialTheme.shapes.extraLarge)
            .padding(if (compact) 14.dp else 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Short screens drop the name (it is on the curtain) and shrink everything so the word fits.
        if (!compact) {
            Text(card.player, color = Neon.Muted, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(18.dp))
        }
        if (card.knowsRole) {
            Emoji(if (card.role == Role.DRIFTER) "🤷" else "🕵️", size = if (compact) 36 else 64)
            Text(
                stringResource(
                    if (card.role ==
                        Role.DRIFTER
                    ) {
                        R.string.reveal_you_are_drifter
                    } else {
                        R.string.reveal_you_are_impostor
                    }
                ),
                color = accent,
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
            Text(
                stringResource(
                    if (card.role ==
                        Role.DRIFTER
                    ) {
                        R.string.reveal_drifter_desc
                    } else {
                        R.string.reveal_impostor_desc
                    }
                ),
                color = Neon.Muted,
                style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = if (compact) 2.dp else 8.dp)
            )
        } else {
            // CLASSIC impostors also land here: same layout as civilians, so nothing gives them away.
            Text(stringResource(R.string.reveal_your_word), color = Neon.Muted)
            Text(
                card.word.orEmpty(),
                color = accent,
                fontSize = if (compact) 32.sp else 44.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = if (compact) 36.sp else 48.sp,
                modifier = Modifier.padding(vertical = if (compact) 2.dp else 8.dp).testTag("secret_word")
            )
        }
        card.hint?.let { hint ->
            Text(
                stringResource(R.string.reveal_hint, hint),
                color = Neon.AmberText,
                modifier = Modifier.padding(top = if (compact) 4.dp else 14.dp)
            )
        }
    }
}

/** "X starts. Get ready! 3, 2, 1" before the clue round. */
@Composable
internal fun CountdownScreen(starter: String, onDone: () -> Unit) {
    var count by rememberSaveable { mutableIntStateOf(3) }
    val done by rememberUpdatedState(onDone)
    LaunchedEffect(Unit) {
        while (count > 0) {
            delay(1000)
            count--
        }
        done()
    }
    Page(title = null, onBack = null, background = Neon.RevealBackdrop) {
        Spacer(Modifier.weight(1f))
        Text(
            stringResource(R.string.countdown_starts, starter),
            color = Neon.AmberText,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            stringResource(R.string.countdown_ready),
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        AnimatedContent(count, transitionSpec = {
            scaleIn(initialScale = 1.6f) + fadeIn() togetherWith fadeOut()
        }, modifier = Modifier.fillMaxWidth(), label = "count") {
            Text(
                "${it.coerceAtLeast(1)}",
                color = Color.White,
                fontSize = 180.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.weight(1.4f))
    }
}
