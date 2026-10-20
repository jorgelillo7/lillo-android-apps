package com.jorgelillo.decisionwheel.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class ModelAndSynthTest {

    @Test
    fun stateSurvivesAJsonRoundTrip() {
        val state = AppState(
            wheels = listOf(Wheel("w1", "Dinner", listOf("Pizza", "Sushi"))),
            history = listOf(Decision("w1", "Pizza", 123L)),
            soundOn = false
        )
        assertEquals(state, Json.decodeFromString<AppState>(Json.encodeToString(state)))
    }

    @Test
    fun historyIsNewestFirstAndCapped() {
        var state = AppState()
        repeat(AppState.MAX_HISTORY + 10) { state = state.withDecision(Decision("w", "o$it", it.toLong())) }
        assertEquals(AppState.MAX_HISTORY, state.history.size)
        assertEquals("o${AppState.MAX_HISTORY + 9}", state.historyOf("w").first().option)
    }

    @Test
    fun spinsTurnSeveralTimesForwardAndLongerWhenFlungHarder() {
        val soft = Spin.plan(30.0, Random(1), flingDegreesPerSecond = 500f)
        val hard = Spin.plan(30.0, Random(1), flingDegreesPerSecond = 9000f)
        assertTrue(soft.targetDegrees > 30.0 + 4 * 360)
        assertTrue(hard.durationMillis > soft.durationMillis)
        assertEquals(1f, Spin.easeOut(1f))
        assertEquals(0f, Spin.easeOut(0f))
    }

    @Test
    fun clickIsShortAndProducesAValidWav() {
        val pcm = ClickSynth.render()
        assertTrue(pcm.size in 500..1500)
        val wav = ClickSynth.wav(pcm)
        assertEquals("RIFF", String(wav.copyOfRange(0, 4)))
        assertEquals(44 + pcm.size * 2, wav.size)
    }
}
