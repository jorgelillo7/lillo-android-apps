package com.jorgelillo.grouppolls.domain

import java.security.SecureRandom
import java.text.Normalizer

object Limits {
    const val NAME_MAX = 24
    const val QUESTION_MIN = 5
    const val QUESTION_MAX = 120
    const val ANSWER_MAX = 30

    /** Reports that hide a public poll from the feed until Jorge reviews it. */
    const val REPORTS_TO_HIDE = 3
}

/** What is wrong with a poll draft; empty when it can be created. */
enum class DraftProblem { QUESTION_TOO_SHORT, QUESTION_TOO_LONG, ANSWER_EMPTY, ANSWER_TOO_LONG, SAME_ANSWERS, BLOCKED_WORDS }

object Drafts {
    fun problems(question: String, red: String, blue: String, visibility: Visibility): Set<DraftProblem> = buildSet {
        val q = question.trim()
        if (q.length < Limits.QUESTION_MIN) add(DraftProblem.QUESTION_TOO_SHORT)
        if (q.length > Limits.QUESTION_MAX) add(DraftProblem.QUESTION_TOO_LONG)
        val answers = listOf(red.trim(), blue.trim())
        if (answers.any { it.isEmpty() }) add(DraftProblem.ANSWER_EMPTY)
        if (answers.any { it.length > Limits.ANSWER_MAX }) add(DraftProblem.ANSWER_TOO_LONG)
        if (answers.all { it.isNotEmpty() } && Text.normalize(answers[0]) == Text.normalize(answers[1])) add(DraftProblem.SAME_ANSWERS)
        // Private polls are between friends who chose to share a link; only public ones are filtered.
        if (visibility == Visibility.PUBLIC && (listOf(q) + answers).any(Moderation::isBlocked)) add(DraftProblem.BLOCKED_WORDS)
    }

    fun cleanName(name: String): String = name.trim().replace(Regex("\\s+"), " ").take(Limits.NAME_MAX)
}

object Text {
    /** Lower case, no accents, single spaces: "  Tortilla  CON cebolla " → "tortilla con cebolla". */
    fun normalize(text: String): String = Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{M}+"), "")
        .replace(Regex("\\s+"), " ")
}

/** Random, unguessable invite codes: the code is the only key to a private poll. */
object InviteCodes {
    const val LENGTH = 12
    private const val ALPHABET = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789" // no 0/O, 1/l/I
    private val random = SecureRandom()

    fun generate(): String = buildString { repeat(LENGTH) { append(ALPHABET[random.nextInt(ALPHABET.length)]) } }

    fun isValid(code: String): Boolean = code.length == LENGTH && code.all { it in ALPHABET }

    /** Reads the code from a shared link (`…/q/?c=CODE`) or a pasted bare code. */
    fun fromLink(text: String): String? {
        val candidate = Regex("[?&]c=([A-Za-z0-9]+)").find(text)?.groupValues?.get(1) ?: text.trim()
        return candidate.takeIf(::isValid)
    }
}
