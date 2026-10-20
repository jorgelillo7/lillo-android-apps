package com.jorgelillo.grouppolls.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jorgelillo.grouppolls.R
import com.jorgelillo.grouppolls.domain.DraftProblem
import com.jorgelillo.grouppolls.domain.Drafts
import com.jorgelillo.grouppolls.domain.Duration
import com.jorgelillo.grouppolls.domain.Limits
import com.jorgelillo.grouppolls.domain.Visibility
import com.jorgelillo.grouppolls.ui.theme.Polls
import kotlinx.coroutines.launch

/** New poll: question, red and blue answers, public or private, and how long it stays open. */
@Composable
fun CreateScreen(
    acceptedTerms: Boolean,
    onAcceptTerms: () -> Unit,
    onCreate: suspend (String, String, String, Visibility, Duration) -> String?,
    onCreated: (String) -> Unit,
    onBack: () -> Unit
) {
    var question by rememberSaveable { mutableStateOf("") }
    var red by rememberSaveable { mutableStateOf("") }
    var blue by rememberSaveable { mutableStateOf("") }
    var visibility by rememberSaveable { mutableStateOf(Visibility.PRIVATE) }
    var duration by rememberSaveable { mutableStateOf(Duration.SEVEN_DAYS) }
    var showTerms by rememberSaveable { mutableStateOf(false) }
    var busy by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val problems = Drafts.problems(question, red, blue, visibility)
    val touched = question.isNotBlank() || red.isNotBlank() || blue.isNotBlank()

    fun submit() {
        if (visibility == Visibility.PUBLIC && !acceptedTerms) {
            showTerms = true
            return
        }
        busy = true
        scope.launch {
            val code = onCreate(question, red, blue, visibility, duration)
            busy = false
            if (code != null) onCreated(code)
        }
    }

    Page(
        title = stringResource(R.string.create_title),
        onBack = onBack,
        modifier = Modifier.imePadding(),
        bottom = {
            BigButton(
                stringResource(R.string.create_button),
                ::submit,
                enabled = problems.isEmpty() && !busy,
                modifier = Modifier.testTag("submit")
            )
        }
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = question,
                onValueChange = { question = it.take(Limits.QUESTION_MAX) },
                label = { Text(stringResource(R.string.create_question)) },
                placeholder = { Text(stringResource(R.string.create_question_hint)) },
                modifier = Modifier.fillMaxWidth().testTag("question_input")
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AnswerField(red, { red = it.take(Limits.ANSWER_MAX) }, stringResource(R.string.create_red), Polls.Red, Modifier.weight(1f).testTag("red_input"))
                AnswerField(blue, { blue = it.take(Limits.ANSWER_MAX) }, stringResource(R.string.create_blue), Polls.Blue, Modifier.weight(1f).testTag("blue_input"))
            }
            if (touched && problems.isNotEmpty()) {
                Text(problemText(problems.first()), color = Polls.Red, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            }

            SectionTitle(stringResource(R.string.create_who))
            Choice(stringResource(R.string.create_private), stringResource(R.string.create_private_desc), "🔗", visibility == Visibility.PRIVATE, "visibility_private") { visibility = Visibility.PRIVATE }
            Choice(stringResource(R.string.create_public), stringResource(R.string.create_public_desc), "🌍", visibility == Visibility.PUBLIC, "visibility_public") { visibility = Visibility.PUBLIC }

            SectionTitle(stringResource(R.string.create_duration))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Duration.entries.forEach { d ->
                    val label = when (d) {
                        Duration.ONE_DAY -> R.string.duration_day
                        Duration.SEVEN_DAYS -> R.string.duration_week
                        Duration.NO_LIMIT -> R.string.duration_none
                    }
                    Pill(stringResource(label), d == duration, Modifier.weight(1f).testTag("duration_${d.name.lowercase()}")) { duration = d }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showTerms) {
        AlertDialog(
            modifier = DialogTags,
            onDismissRequest = { showTerms = false },
            title = { Text(stringResource(R.string.terms_title)) },
            text = { Text(stringResource(R.string.terms_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showTerms = false
                    onAcceptTerms()
                    submit()
                }, modifier = Modifier.testTag("accept_terms")) {
                    Text(stringResource(R.string.terms_accept), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { showTerms = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun problemText(problem: DraftProblem): String = stringResource(
    when (problem) {
        DraftProblem.QUESTION_TOO_SHORT -> R.string.problem_short
        DraftProblem.QUESTION_TOO_LONG -> R.string.problem_long
        DraftProblem.ANSWER_EMPTY -> R.string.problem_answer_empty
        DraftProblem.ANSWER_TOO_LONG -> R.string.problem_answer_long
        DraftProblem.SAME_ANSWERS -> R.string.problem_same
        DraftProblem.BLOCKED_WORDS -> R.string.problem_blocked
    }
)

@Composable
private fun AnswerField(value: String, onChange: (String) -> Unit, label: String, color: Color, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, color = color) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = color, unfocusedBorderColor = color.copy(alpha = 0.5f), cursorColor = color),
        modifier = modifier
    )
}

@Composable
private fun Choice(title: String, desc: String, emoji: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(MaterialTheme.shapes.large)
            .background(if (selected) Polls.BlueSoft else Polls.Card)
            .border(2.dp, if (selected) Polls.Blue else Polls.Line, MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag(tag)
    ) {
        Text(emoji, style = MaterialTheme.typography.headlineSmall)
        Column(Modifier.padding(start = 12.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = Polls.Muted)
        }
    }
}

@Composable
private fun Pill(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Text(
        text,
        modifier = modifier
            .clip(CircleShape)
            .background(if (selected) Polls.Ink else Polls.Card)
            .border(1.dp, if (selected) Polls.Ink else Polls.Line, CircleShape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        color = if (selected) Color.White else Polls.Ink,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}
