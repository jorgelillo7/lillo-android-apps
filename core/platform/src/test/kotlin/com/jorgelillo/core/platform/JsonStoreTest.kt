package com.jorgelillo.core.platform

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsonStoreTest {
    @Serializable
    data class Notes(val items: List<String> = emptyList(), val version: Int = 1)

    // One DataStore per file and process: every test uses its own file.
    private fun store(name: String) = JsonStore(ApplicationProvider.getApplicationContext(), name, Notes.serializer()) { Notes() }

    @Test
    fun startsFromTheDefault() = runTest {
        assertEquals(Notes(), store("defaults").state.first())
    }

    @Test
    fun anUpdateIsReadBack() = runTest {
        val store = store("update")
        store.update { it.copy(items = it.items + "milk") }
        store.update { it.copy(items = it.items + "eggs") }
        assertEquals(listOf("milk", "eggs"), store.state.first().items)
    }

    @Test
    fun updateAndGetReturnsWhatTheEditComputed() = runTest {
        val store = store("updateAndGet")
        val size = store.updateAndGet { notes -> (notes.copy(items = listOf("a", "b"))).let { it to it.items.size } }
        assertEquals(2, size)
        assertEquals(listOf("a", "b"), store.state.first().items)
    }
}
