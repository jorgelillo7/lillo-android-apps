package com.jorgelillo.slipper.data

import com.jorgelillo.core.platform.JsonStore
import com.jorgelillo.slipper.domain.SlipperSkin
import kotlin.test.assertEquals
import org.junit.Test

/** What users already have on their phones must keep reading after any change to the model. */
class StoredStateTest {
    @Test
    fun theSaveTheReleasedAppWritesStillReads() {
        val stored = """
            {
                "best": 1240,
                "runs": 31,
                "croquetas": 455,
                "dailyDay": 20400,
                "dailyBest": 860,
                "skin": "CHECKED",
                "muted": true
            }
        """.trimIndent()

        assertEquals(
            Saved(
                best = 1240,
                runs = 31,
                croquetas = 455,
                dailyDay = 20_400,
                dailyBest = 860,
                skin = SlipperSkin.CHECKED,
                muted = true
            ),
            JsonStore.decodeStored(Saved.serializer(), stored)
        )
    }
}
