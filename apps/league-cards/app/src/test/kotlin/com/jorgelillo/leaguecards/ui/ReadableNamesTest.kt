package com.jorgelillo.leaguecards.ui

import androidx.compose.ui.graphics.Color
import com.jorgelillo.leaguecards.ui.theme.Chat
import com.jorgelillo.leaguecards.ui.theme.contrast
import com.jorgelillo.leaguecards.ui.theme.readableOn
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

/** A deck's character colours, picked for the dark chat, still read as names by day. */
class ReadableNamesTest {
    private val pale = listOf(Color(0xFFE5B33E), Color(0xFF2EC4B6), Color(0xFFF4B6C8), Color(0xFFFFFF00))

    @Test
    fun paleColoursAreDarkenedToReadOnTheDayChat() {
        for (surface in listOf(Chat.Day.bubble, Chat.Day.ink)) {
            pale.forEach { assertTrue(contrast(readableOn(it, surface), surface) >= 4.5f, "$it on $surface") }
        }
    }

    @Test
    fun aColourThatAlreadyReadsIsLeftAlone() {
        pale.forEach { assertEquals(it, readableOn(it, Chat.Night.bubble)) }
        assertEquals(Chat.Day.text, readableOn(Chat.Day.text, Chat.Day.bubble))
    }
}
