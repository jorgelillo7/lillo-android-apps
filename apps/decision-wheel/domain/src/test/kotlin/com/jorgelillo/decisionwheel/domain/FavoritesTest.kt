package com.jorgelillo.decisionwheel.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class FavoritesTest {

    private val a = Wheel("a", "A", listOf("1", "2"))
    private val b = Wheel("b", "B", listOf("1", "2"))
    private val c = Wheel("c", "C", listOf("1", "2"))
    private val d = Wheel("d", "D", listOf("1", "2"))
    private val state = AppState(wheels = listOf(a, b, c, d))

    @Test
    fun favouritesComeFirstKeepingTheUsersOrder() {
        val s = state.toggleFavorite("c").toggleFavorite("b")
        assertEquals(listOf("b", "c", "a", "d"), s.sortedWheels.map { it.id })
        assertFalse(s.toggleFavorite("b").wheel("b")!!.favorite)
    }

    @Test
    fun shortcutsAreFavouritesThenRecent() {
        val s = state.toggleFavorite("d")
            .withDecision(Decision("a", "1", 100))
            .withDecision(Decision("c", "1", 300))
            .withDecision(Decision("a", "2", 200))
            .withDecision(Decision("d", "1", 400))
        assertEquals(listOf("d", "c", "a"), s.shortcutWheels().map { it.id })
        assertEquals(emptyList(), state.shortcutWheels())
    }

    @Test
    fun restoringBringsBackOnlyDeletedPresets() {
        val presets = listOf(Wheel("preset-1", "Eat", listOf("x", "y")), Wheel("preset-2", "Play", listOf("x", "y")))
        val edited = presets[0].copy(name = "Lunch", favorite = true)
        val s = AppState(wheels = listOf(edited, a)).restorePresets(presets)
        assertEquals(listOf("preset-1", "a", "preset-2"), s.wheels.map { it.id })
        assertEquals("Lunch", s.wheel("preset-1")!!.name)
        assertTrue(s.wheel("preset-1")!!.favorite)
    }

    @Test
    fun stateSavedByVersion1StillLoads() {
        // 1.0 saved wheels without the favorite field.
        val v1 = """{"wheels":[{"id":"a","name":"A","options":["1","2"]}],"history":[]}"""
        val s = Json { ignoreUnknownKeys = true }.decodeFromString(AppState.serializer(), v1)
        assertFalse(s.wheels.single().favorite)
    }
}
