package dev.iosfeel.dayline.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppTheme {
    System,
    Light,
    Dark
}

val Context.daylineDataStore: DataStore<Preferences> by preferencesDataStore(name = "dayline_settings")

class DaylinePreferences(private val context: Context) {

    companion object {
        val KEY_THEME = stringPreferencesKey("app_theme")
        val KEY_DEV_MODE = booleanPreferencesKey("developer_mode_enabled")
    }

    val theme: Flow<AppTheme> = context.daylineDataStore.data.map { preferences ->
        val themeName = preferences[KEY_THEME] ?: AppTheme.System.name
        try {
            AppTheme.valueOf(themeName)
        } catch (_: Exception) {
            AppTheme.System
        }
    }

    val isDeveloperModeEnabled: Flow<Boolean> = context.daylineDataStore.data.map { preferences ->
        preferences[KEY_DEV_MODE] ?: false
    }

    suspend fun setTheme(theme: AppTheme) {
        context.daylineDataStore.edit { preferences ->
            preferences[KEY_THEME] = theme.name
        }
    }

    suspend fun setDeveloperModeEnabled(enabled: Boolean) {
        context.daylineDataStore.edit { preferences ->
            preferences[KEY_DEV_MODE] = enabled
        }
    }
}
