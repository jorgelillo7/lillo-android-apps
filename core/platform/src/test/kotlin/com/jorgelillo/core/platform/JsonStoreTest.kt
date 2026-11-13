package com.jorgelillo.core.platform

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsonStoreTest {
    enum class Kind { Shopping, Todo }

    @Serializable
    data class Notes(val items: List<String> = emptyList(), val version: Int = 1, val kind: Kind = Kind.Todo)

    @Serializable
    data class Strict(val title: String, val count: Int)

    private val context: Context = ApplicationProvider.getApplicationContext()

    // One DataStore per file and process: every test uses its own file.
    private fun store(name: String) = JsonStore(context, name, Notes.serializer()) { Notes() }

    /** A store whose file already holds [raw] under "state", as an older or broken version left it. */
    private suspend fun <T> storeHolding(
        name: String,
        raw: String,
        serializer: kotlinx.serialization.KSerializer<T>,
        default: () -> T
    ): JsonStore<T> {
        val dataStore = PreferenceDataStoreFactory.create { context.preferencesDataStoreFile(name) }
        dataStore.edit { it[stringPreferencesKey("state")] = raw }
        return JsonStore(dataStore, serializer, default)
    }

    private suspend fun notesHolding(name: String, raw: String) = storeHolding(name, raw, Notes.serializer()) {
        Notes()
    }

    @Test
    fun startsFromTheDefault() = runTest {
        val store = store("defaults")
        assertEquals(Notes(), store.state.first())
        assertEquals(emptyList(), store.unreadable.first())
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

    /** What was written is on disk: a store opened afresh over the same file, as after a restart, reads it. */
    @Test
    fun theStateSurvivesARestart() = runTest {
        val name = "restart"
        fun open(scope: CoroutineScope) = JsonStore(
            PreferenceDataStoreFactory.create(scope = scope) { context.preferencesDataStoreFile(name) },
            Notes.serializer()
        ) { Notes() }

        val before = CoroutineScope(Dispatchers.IO + SupervisorJob())
        open(before).update { it.copy(items = listOf("milk"), kind = Kind.Shopping) }
        // The first "process" ends: its DataStore is gone, only the file is left.
        before.coroutineContext.job.cancelAndJoin()
        assertTrue(context.preferencesDataStoreFile(name).length() > 0)

        val after = CoroutineScope(Dispatchers.IO + SupervisorJob())
        assertEquals(Notes(items = listOf("milk"), kind = Kind.Shopping), open(after).state.first())
        after.coroutineContext.job.cancelAndJoin()
    }

    @Test
    fun fieldsAddedLaterAndFieldsRemovedSinceStillRead() = runTest {
        val store = notesHolding("compatible", """{"items":["milk"],"dropped":true}""")
        assertEquals(Notes(items = listOf("milk")), store.state.first())
        assertEquals(emptyList(), store.unreadable.first())
    }

    @Test
    fun corruptJsonReadsAsTheDefaultAndSurvivesTheNextWrite() = runTest {
        val corrupt = """{"items":["milk","eg"""
        val store = notesHolding("corrupt", corrupt)
        assertEquals(Notes(), store.state.first())

        store.update { it.copy(items = listOf("bread")) }

        assertEquals(listOf("bread"), store.state.first().items)
        assertEquals(listOf(corrupt), store.unreadable.first())
    }

    @Test
    fun anEnumValueThisVersionDoesNotKnowIsKeptAside() = runTest {
        val future = """{"items":["milk"],"kind":"Recipe"}"""
        val store = notesHolding("unknownEnum", future)
        assertEquals(Notes(), store.state.first())

        val size = store.updateAndGet { notes -> notes.copy(items = listOf("x")).let { it to it.items.size } }

        assertEquals(1, size)
        assertEquals(listOf(future), store.unreadable.first())
    }

    @Test
    fun aMissingRequiredFieldIsKeptAside() = runTest {
        val partial = """{"title":"Groceries"}"""
        val store = storeHolding("missingField", partial, Strict.serializer()) { Strict("new", 0) }
        assertEquals(Strict("new", 0), store.state.first())

        store.update { it.copy(count = 1) }

        assertEquals(Strict("new", 1), store.state.first())
        assertEquals(listOf(partial), store.unreadable.first())
    }

    @Test
    fun everyUnreadableDocumentIsKeptNotJustTheFirst() = runTest {
        val name = "twice"
        val dataStore = PreferenceDataStoreFactory.create { context.preferencesDataStoreFile(name) }
        val key = stringPreferencesKey("state")
        dataStore.edit { it[key] = "first broken" }
        val store = JsonStore(dataStore, Notes.serializer()) { Notes() }
        store.update { it.copy(items = listOf("a")) }
        dataStore.edit { it[key] = "second broken" }
        store.update { it.copy(items = listOf("b")) }

        assertEquals(listOf("first broken", "second broken"), store.unreadable.first())
        assertEquals(listOf("b"), store.state.first().items)
    }

    @Test
    fun aReadableDocumentIsNeverCopiedAside() = runTest {
        val store = notesHolding("readable", """{"items":["milk"]}""")
        store.update { it.copy(items = it.items + "eggs") }
        assertEquals(listOf("milk", "eggs"), store.state.first().items)
        assertTrue(store.unreadable.first().isEmpty())
    }

    @Test
    fun decodeStoredThrowsInsteadOfFallingBack() {
        assertEquals(Notes(listOf("a")), JsonStore.decodeStored(Notes.serializer(), """{"items":["a"]}"""))
        assertFailsWith<SerializationException> { JsonStore.decodeStored(Notes.serializer(), """{"kind":"Recipe"}""") }
    }
}
