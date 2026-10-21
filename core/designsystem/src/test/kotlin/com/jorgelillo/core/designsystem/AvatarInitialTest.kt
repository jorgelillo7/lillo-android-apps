package com.jorgelillo.core.designsystem

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AvatarInitialTest {
    @Test
    fun aLetterUpperCased() {
        assertEquals("A", avatarInitial("ana"))
        assertEquals("Á", avatarInitial("  álvaro"))
        assertEquals("Ñ", avatarInitial("ñoño"))
    }

    @Test
    fun aBlankNameIsAQuestionMark() {
        assertEquals("?", avatarInitial(""))
        assertEquals("?", avatarInitial("   "))
    }

    @Test
    fun aWholeEmojiNeverHalfOfOne() {
        assertEquals("🦁", avatarInitial("🦁 Leones"))
        // Man cook: three code points joined by a zero-width joiner.
        assertEquals("👨‍🍳", avatarInitial("👨‍🍳 Chef"))
        // Flag: two regional indicators.
        assertEquals("🇪🇸", avatarInitial("🇪🇸"))
    }
}
