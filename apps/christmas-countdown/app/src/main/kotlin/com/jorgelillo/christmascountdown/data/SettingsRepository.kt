package com.jorgelillo.christmascountdown.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/** User preferences and advent progress, persisted with DataStore. */
class SettingsRepository(private val context: Context) {

    private val sleepsModeKey = booleanPreferencesKey("sleeps_mode")
    private val musicEnabledKey = booleanPreferencesKey("music_enabled")
    private val reminderPromptDismissedKey = booleanPreferencesKey("reminder_prompt_dismissed")

    // Opened doors are stored per year so the calendar starts fresh every December.
    private fun openedDoorsKey(year: Int) = stringSetPreferencesKey("advent_opened_$year")

    val sleepsMode: Flow<Boolean> = context.dataStore.data.map { it[sleepsModeKey] ?: false }

    val musicEnabled: Flow<Boolean> = context.dataStore.data.map { it[musicEnabledKey] ?: false }

    /** The "shall we remind you?" card was answered (or the permission denied): don't ask again. */
    val reminderPromptDismissed: Flow<Boolean> = context.dataStore.data.map { it[reminderPromptDismissedKey] ?: false }

    suspend fun dismissReminderPrompt() {
        context.dataStore.edit { it[reminderPromptDismissedKey] = true }
    }

    fun openedDoors(year: Int): Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        prefs[openedDoorsKey(year)].orEmpty().mapNotNull { it.toIntOrNull() }.toSet()
    }

    suspend fun setSleepsMode(enabled: Boolean) {
        context.dataStore.edit { it[sleepsModeKey] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { it[musicEnabledKey] = enabled }
    }

    suspend fun markDoorOpened(year: Int, door: Int) {
        context.dataStore.edit { it[openedDoorsKey(year)] = it[openedDoorsKey(year)].orEmpty() + door.toString() }
    }
}
