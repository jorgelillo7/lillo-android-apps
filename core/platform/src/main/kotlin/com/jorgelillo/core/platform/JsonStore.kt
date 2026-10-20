package com.jorgelillo.core.platform

import android.content.Context
import androidx.datastore.core.DataStore
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
 * the storage every app uses. Unknown fields are ignored and an unreadable document falls back to
 * [default], so adding fields never breaks an update. Create one per file, as an app singleton.
 */
class JsonStore<T>(
    context: Context,
    name: String,
    private val serializer: KSerializer<T>,
    private val default: () -> T
) {
    private val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.applicationContext.preferencesDataStoreFile(name) }
    private val key = stringPreferencesKey("state")

    val state: Flow<T> = dataStore.data.map { decode(it[key]) }

    suspend fun update(transform: (T) -> T) {
        dataStore.edit { prefs -> prefs[key] = json.encodeToString(serializer, transform(decode(prefs[key]))) }
    }

    /** Like [update], also returning something computed in the same atomic edit. */
    suspend fun <R> updateAndGet(transform: (T) -> Pair<T, R>): R {
        var result: Result<R>? = null
        dataStore.edit { prefs ->
            val (state, r) = transform(decode(prefs[key]))
            prefs[key] = json.encodeToString(serializer, state)
            result = Result.success(r)
        }
        return result!!.getOrThrow()
    }

    private fun decode(raw: String?): T = raw?.let { runCatching { json.decodeFromString(serializer, it) }.getOrNull() } ?: default()

    private companion object {
        val json = Json { ignoreUnknownKeys = true }
    }
}
