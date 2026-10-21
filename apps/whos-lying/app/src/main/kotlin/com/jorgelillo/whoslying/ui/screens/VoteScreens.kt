package com.jorgelillo.whoslying.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jorgelillo.core.designsystem.isShortScreen
import com.jorgelillo.whoslying.R
import com.jorgelillo.whoslying.domain.Card
import com.jorgelillo.whoslying.domain.Elimination
import com.jorgelillo.whoslying.domain.Game
import com.jorgelillo.whoslying.domain.Role
import com.jorgelillo.whoslying.ui.theme.Neon
import kotlinx.coroutines.delay

/** The group picks who to vote out, then confirms. */
@Composable
internal fun VotingScreen(
    game: Game,
    alive: Set<String>,
    /** Time ran out: no going back to the debate. */
    forced: Boolean,
    onConfirm: (String) -> Unit,
    onBack: () -> Unit
) {
    var chosen by rememberSaveable { mutableStateOf<String?>(null) }
    val short = isShortScreen()
    BackHandler(enabled = forced) {}
    Page(
        title = null,
        onBack = onBack.takeUnless { forced },
        background = Neon.RevealBackdrop,
        bottom = {
            BigButton(
                stringResource(R.string.voting_confirm),
                { chosen?.let(onConfirm) },
                Modifier.testTag("confirm_vote"),
                enabled = chosen != null,
                color = Neon.Go,
                contentColor = Neon.Night
            )
            if (!forced) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterHorizontally).testTag("back_to_debate")
                ) {
                    Text(stringResource(R.string.voting_back), color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            // Short screens keep only the question, so the players stay visible.
            if (!short) {
                Emoji("🗳️", size = 56)
                Text(
                    stringResource(R.string.voting_title),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    stringResource(R.string.voting_desc),
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }
            Text(
                stringResource(R.string.voting_question),
                color = Neon.AmberText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = if (short) 4.dp else 12.dp)
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(if (short) 120.dp else 140.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(game.cards.filter { it.player in alive }, key = { it.player }) { card ->
                val selected = chosen == card.player
                Box(
                    Modifier
                        .clip(MaterialTheme.shapes.large)
                        .background(if (selected) Neon.Night.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.1f))
                        .border(
                            2.dp,
                            if (selected) Neon.Go else Color.White.copy(alpha = 0.25f),
                            MaterialTheme.shapes.large
                        )
                        .clickable { chosen = card.player }
                        .testTag("vote_${card.player}")
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = if (short) 8.dp else 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Avatar(card.player, game.cards.map { it.player }, if (short) 40.dp else 64.dp)
                        Text(
                            card.player,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(top = if (short) 4.dp else 10.dp)
                        )
                    }
                    if (selected) {
                        Box(
                            Modifier.align(
                                Alignment.TopEnd
                            ).padding(8.dp).size(24.dp).clip(CircleShape).background(Neon.Go),
                            contentAlignment = Alignment.Center
                        ) { Text("✓", color = Neon.Night, fontWeight = FontWeight.Black) }
                    }
                }
            }
        }
    }
}

/**
 * "Exposed in 3, 2, 1…" and then who the voted player really was. Game over goes straight to the
 * result ([onGameOver]); an eliminated drifter first gets one guess at the word.
 */
@Composable
internal fun ExposeScreen(
    game: Game,
    elimination: Elimination,
    onGuess: (String) -> Boolean,
    onContinue: () -> Unit,
    onGameOver: () -> Unit
) {
    var count by rememberSaveable(elimination) { mutableIntStateOf(3) }
    var guessResult by rememberSaveable(elimination) { mutableStateOf<Boolean?>(null) }
    // The effect outlives recompositions: read the newest game, not the one it started with.
    val latestGame by rememberUpdatedState(game)
    val gameOver by rememberUpdatedState(onGameOver)
    LaunchedEffect(elimination) {
        while (count > 0) {
            delay(800)
            count--
        }
        if (latestGame.isOver && latestGame.pendingGuess == null) gameOver()
    }
    val card = game.cardOf(elimination.player)
    // Game is a plain object: read the guess through guessResult (state) so the bar recomposes after it.
    val guessing = game.pendingGuess != null && guessResult == null
    val background = when {
        count > 0 -> Neon.RevealBackdrop
        card.role == Role.CIVILIAN -> Neon.ImpostorsBackdrop
        card.role == Role.DRIFTER -> Neon.DrifterBackdrop
        else -> Neon.CiviliansBackdrop
    }
    Page(
        title = null,
        onBack = null,
        background = background,
        bottom = {
            if (count == 0 && !guessing && !game.isOver) {
                BigButton(
                    stringResource(R.string.next_vote),
                    onContinue,
                    Modifier.testTag("keep_playing"),
                    color = Color.White,
                    contentColor = Neon.Night
                )
            }
        }
    ) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (count > 0) {
                Emoji("👀", size = 110)
                Text(
                    stringResource(R.string.expose_in) + " " + (3 downTo count).joinToString(", ") + "…",
                    color = Color.White,
                    fontSize = 44.sp,
                    lineHeight = 50.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp).testTag("exposing")
                )
            } else {
                EliminatedCard(card, game, guessResult, onGuess = { guessResult = onGuess(it) })
            }
        }
    }
}

@Composable
private fun ColumnScope.EliminatedCard(card: Card, game: Game, guessResult: Boolean?, onGuess: (String) -> Unit) {
    val (emoji, title, body) = when (card.role) {
        Role.CIVILIAN -> Triple(
            "😇",
            stringResource(R.string.out_was_civilian, card.player),
            stringResource(R.string.out_civilian_desc)
        )

        Role.IMPOSTOR -> Triple(
            "🕵️",
            stringResource(R.string.out_was_impostor, card.player),
            stringResource(R.string.out_impostor_desc)
        )

        Role.DRIFTER -> Triple(
            "🤷",
            stringResource(R.string.out_was_drifter, card.player),
            stringResource(R.string.guess_desc)
        )
    }
    val short = isShortScreen()
    val header = @Composable {
        Emoji(emoji, size = if (short) 56 else 100)
        Text(
            title,
            color = Color.White,
            fontSize = if (short) 28.sp else 36.sp,
            lineHeight = if (short) 32.sp else 42.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("eliminated")
        )
    }
    if (game.pendingGuess == card.player) {
        val prompt = @Composable {
            Text(
                stringResource(R.string.guess_title, card.player),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = if (short) 8.dp else 20.dp)
            )
            Text(body, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center)
        }
        if (short) {
            // Landscape and handhelds: too low to stack it all, so the field and button sit beside the text.
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    header()
                    prompt()
                }
                Column(Modifier.weight(1f)) { GuessInput(onGuess) }
            }
        } else {
            header()
            prompt()
            GuessInput(onGuess)
        }
    } else {
        header()
        val word = card.word
        if (card.role == Role.IMPOSTOR && word != null) {
            Text(
                stringResource(R.string.out_their_word, word),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Text(
            if (guessResult == false) {
                stringResource(R.string.guess_wrong)
            } else if (card.role == Role.DRIFTER) {
                ""
            } else {
                body
            },
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

/** The eliminated drifter's one shot at the civilians' word. */
@Composable
private fun ColumnScope.GuessInput(onGuess: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text(stringResource(R.string.guess_hint), color = Color.White.copy(alpha = 0.9f)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (text.isNotBlank()) onGuess(text) }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).testTag("guess_text")
    )
    Spacer(Modifier.height(12.dp))
    BigButton(stringResource(R.string.guess_go), {
        onGuess(text)
    }, Modifier.testTag("guess"), enabled = text.isNotBlank(), color = Color.White, contentColor = Neon.Night)
}
