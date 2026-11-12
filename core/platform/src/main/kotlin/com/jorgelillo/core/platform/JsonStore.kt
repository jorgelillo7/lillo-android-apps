package com.jorgelillo.core.platform

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * A whole app state as one JSON document in a Preferences DataStore file `name` (key "state"),
 * the storage every app uses. Create one per file, as an app singleton.
 *
 * Unknown fields are ignored, so adding fields never breaks an update. A document this version
 * cannot read (a removed enum value, a changed type, a corrupt file) reads as [default], and the
 * first write moves it, in the same atomic edit, to an `unreadable.N` key instead of overwriting
 * it: the user's data stays in the file (and in backups) for a later version to recover, and
 * [unreadable] reports it.
 */
class JsonStore<T> internal constructor(
    private val dataStore: DataStore<Preferences>,
    private val serializer: KSerializer<T>,
    private val default: () -> T
) {
    constructor(context: Context, name: String, serializer: KSerializer<T>, default: () -> T) : this(
        PreferenceDataStoreFactory.create { context.applicationContext.preferencesDataStoreFile(name) },
        serializer,
        default
    )

    val state: Flow<T> = dataStore.data.map { decodeOrDefault(it[KEY]) }

    /** Documents that could not be read and were set aside, oldest first; empty when all is well. */
    val unreadable: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs.asMap().entries
            .mapNotNull { (key, value) -> unreadableIndex(key.name)?.let { it to value as String } }
            .sortedBy { it.first }
            .map { it.second }
    }

    suspend fun update(transform: (T) -> T) {
        dataStore.edit { prefs -> write(prefs, transform(readForWrite(prefs))) }
    }

    /** Like [update], also returning something computed in the same atomic edit. */
    suspend fun <R> updateAndGet(transform: (T) -> Pair<T, R>): R {
        var result: Result<R>? = null
        dataStore.edit { prefs ->
            val (state, r) = transform(readForWrite(prefs))
            write(prefs, state)
            result = Result.success(r)
        }
        return result!!.getOrThrow()
    }

    private fun write(prefs: MutablePreferences, state: T) {
        prefs[KEY] = json.encodeToString(serializer, state)
    }

    /** The current state; an unreadable document is moved aside first so the write can't destroy it. */
    private fun readForWrite(prefs: MutablePreferences): T {
        val raw = prefs[KEY] ?: return default()
        return decodeOrNull(raw) ?: default().also {
            val next = (prefs.asMap().keys.mapNotNull { unreadableIndex(it.name) }.maxOrNull() ?: 0) + 1
            prefs[stringPreferencesKey("$UNREADABLE_PREFIX$next")] = raw
            Log.w(TAG, "Stored state could not be read; kept as $UNREADABLE_PREFIX$next")
        }
    }

    private fun decodeOrDefault(raw: String?): T = raw?.let(::decodeOrNull) ?: default()

    // SerializationException is an IllegalArgumentException, as are the errors of init checks.
    private fun decodeOrNull(raw: String): T? = try {
        decodeStored(serializer, raw)
    } catch (_: IllegalArgumentException) {
        null
    }

    companion object {
        private const val TAG = "JsonStore"
        private const val UNREADABLE_PREFIX = "unreadable."
        private val KEY = stringPreferencesKey("state")
        private val json = Json { ignoreUnknownKeys = true }

        private fun unreadableIndex(key: String): Int? = key.removePrefix(UNREADABLE_PREFIX)
            .takeIf { key.startsWith(UNREADABLE_PREFIX) }
            ?.toIntOrNull()

        /**
         * Decodes a stored document exactly as [JsonStore] does, throwing if it can't: lets each
         * app pin, in a unit test, that the JSON its released versions wrote still reads.
         */
        fun <T> decodeStored(serializer: KSerializer<T>, raw: String): T = json.decodeFromString(serializer, raw)
    }
}
