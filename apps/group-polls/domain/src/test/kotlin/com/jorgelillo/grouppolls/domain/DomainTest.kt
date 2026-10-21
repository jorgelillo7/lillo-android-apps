package com.jorgelillo.grouppolls.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DomainTest {

    private fun poll(red: Int = 0, blue: Int = 0, closesAt: Long? = null) = Poll(
        code = "abcdefghijkm", visibility = Visibility.PUBLIC, question = "¿Tortilla con cebolla?",
        red = "Con", blue = "Sin", creatorId = "u1", creatorName = "Jorge", language = "es", createdAt = 0, closesAt = closesAt,
        redVotes = red, blueVotes = blue
    )

    @Test
    fun durationSetsTheClosingTime() {
        assertEquals(24L * 60 * 60 * 1000, Duration.ONE_DAY.closesAt(0))
        assertNull(Duration.NO_LIMIT.closesAt(123))
        assertTrue(poll(closesAt = 10).isOpen(9))
        assertFalse(poll(closesAt = 10).isOpen(10))
        assertTrue(poll(closesAt = null).isOpen(Long.MAX_VALUE))
    }

    @Test
    fun feedShowsLastWeekPublicUnhiddenPolls() {
        val day = 24L * 60 * 60 * 1000
        assertTrue(poll().inFeed(now = 6 * day))
        assertFalse(poll().inFeed(now = 7 * day))
        assertFalse(poll().copy(hidden = true).inFeed(now = 0))
        assertFalse(poll().copy(visibility = Visibility.PRIVATE).inFeed(now = 0))
    }

    @Test
    fun draftsAreValidated() {
        assertTrue(Drafts.problems("¿Playa o montaña?", "Playa", "Montaña", Visibility.PUBLIC).isEmpty())
        assertEquals(setOf(DraftProblem.QUESTION_TOO_SHORT), Drafts.problems("¿Sí?", "Sí", "No", Visibility.PUBLIC))
        assertTrue(DraftProblem.SAME_ANSWERS in Drafts.problems("¿Playa o playa?", "Playa", " PLAYÁ ", Visibility.PRIVATE))
        assertTrue(DraftProblem.ANSWER_EMPTY in Drafts.problems("¿Playa o montaña?", "Playa", " ", Visibility.PRIVATE))
        assertTrue(DraftProblem.ANSWER_TOO_LONG in Drafts.problems("¿Playa o montaña?", "x".repeat(31), "No", Visibility.PRIVATE))
    }

    @Test
    fun onlyPublicPollsAreFiltered() {
        assertTrue(DraftProblem.BLOCKED_WORDS in Drafts.problems("¿Quién es más gilipollas?", "Tú", "Yo", Visibility.PUBLIC))
        assertTrue(Drafts.problems("¿Quién es más gilipollas?", "Tú", "Yo", Visibility.PRIVATE).isEmpty())
    }

    @Test
    fun blocklistMatchesWholeWordsIgnoringCaseAndAccents() {
        assertTrue(Moderation.isBlocked("Qué MIERDA de pregunta"))
        assertTrue(Moderation.isBlocked("cocaína"))
        assertFalse(Moderation.isBlocked("Sexto piso o ático")) // "sex" inside "sexto" is fine
        assertFalse(Moderation.isBlocked("Esto es una computadora")) // "puta" inside a word is fine
    }

    @Test
    fun splitAlwaysAddsUpTo100() {
        assertEquals(Split(0, 0), Split.of(0, 0))
        assertEquals(Split(67, 33), Split.of(2, 1))
        assertEquals(100, Split.of(1, 2).let { it.red + it.blue })
        assertEquals(Split(50, 50), Split.of(5, 5))
    }

    @Test
    fun predictionsResolveOnlyOnClosedPollsWithAWinner() {
        val record = PredictionRecord()
        assertEquals(record, record.add(Side.RED, poll(red = 3, blue = 1, closesAt = 100), now = 50)) // still open
        assertEquals(record, record.add(Side.RED, poll(red = 3, blue = 1, closesAt = null), now = 1_000)) // never closes
        assertEquals(record, record.add(Side.RED, poll(red = 2, blue = 2, closesAt = 10), now = 50)) // tie
        val after = record.add(Side.RED, poll(red = 3, blue = 1, closesAt = 10), now = 50)
            .add(Side.RED, poll(red = 1, blue = 3, closesAt = 10), now = 50)
        assertEquals(PredictionRecord(hits = 1, resolved = 2), after)
        assertEquals(50, after.accuracy)
    }

    @Test
    fun inviteCodesAreLongRandomAndParsedFromLinks() {
        val codes = List(200) { InviteCodes.generate() }
        assertEquals(200, codes.toSet().size)
        assertTrue(codes.all(InviteCodes::isValid))
        val code = codes.first()
        assertEquals(code, InviteCodes.fromLink("Vota aquí: https://jorgelillo7.github.io/q/?c=$code"))
        assertEquals(code, InviteCodes.fromLink("  $code "))
        assertNull(InviteCodes.fromLink("https://jorgelillo7.github.io/q/?c=short"))
    }

    @Test
    fun namesAreTidied() {
        assertEquals("Jorge Lillo", Drafts.cleanName("  Jorge    Lillo "))
        assertEquals(Limits.NAME_MAX, Drafts.cleanName("x".repeat(50)).length)
    }
}
