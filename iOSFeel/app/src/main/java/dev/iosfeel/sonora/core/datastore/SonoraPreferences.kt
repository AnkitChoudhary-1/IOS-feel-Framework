package dev.iosfeel.sonora.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sonoraDataStore: DataStore<Preferences> by preferencesDataStore(name = "sonora_preferences")

enum class ThemeMode {
    System,
    Light,
    Dark
}

data class DeveloperSettings(
    val blurRadius: Float = 24f,
    val tintAlpha: Float = 0.78f,
    val cornerRadius: Float = 24f,
    val borderStroke: Float = 0.5f,
    val borderAlpha: Float = 0.20f,
    val materialStyle: String = "Regular",
    val tintColorArgb: Long = 0L,
    val backdropBlurEnabled: Boolean = true,
    val playerStiffness: Float = 400f,
    val playerDamping: Float = 0.85f,
    val completionThreshold: Float = 0.38f,
    val velocityThreshold: Float = 900f
)

val LocalDeveloperSettings = androidx.compose.runtime.compositionLocalOf { DeveloperSettings() }

class SonoraPreferences(
    private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DEVELOPER_MODE_ENABLED = booleanPreferencesKey("developer_mode_enabled")
        val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
        val REPEAT_MODE = stringPreferencesKey("repeat_mode")
        val LAST_PLAYED_SONG_ID = longPreferencesKey("last_played_song_id")
        val LAST_PLAYED_POSITION_MS = longPreferencesKey("last_played_position_ms")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")

        // Developer Settings Keys
        val DEV_BLUR_RADIUS = floatPreferencesKey("dev_blur_radius")
        val DEV_TINT_ALPHA = floatPreferencesKey("dev_tint_alpha")
        val DEV_CORNER_RADIUS = floatPreferencesKey("dev_corner_radius")
        val DEV_BORDER_STROKE = floatPreferencesKey("dev_border_stroke")
        val DEV_BORDER_ALPHA = floatPreferencesKey("dev_border_alpha")
        val DEV_MATERIAL_STYLE = stringPreferencesKey("dev_material_style")
        val DEV_TINT_COLOR_ARGB = longPreferencesKey("dev_tint_color_argb")
        val DEV_BACKDROP_BLUR_ENABLED = booleanPreferencesKey("dev_backdrop_blur_enabled")
        val DEV_PLAYER_STIFFNESS = floatPreferencesKey("dev_player_stiffness")
        val DEV_PLAYER_DAMPING = floatPreferencesKey("dev_player_damping")
        val DEV_COMPLETION_THRESHOLD = floatPreferencesKey("dev_completion_threshold")
        val DEV_VELOCITY_THRESHOLD = floatPreferencesKey("dev_velocity_threshold")
    }

    val themeMode: Flow<ThemeMode> = context.sonoraDataStore.data.map { prefs ->
        val modeName = prefs[Keys.THEME_MODE] ?: ThemeMode.System.name
        try {
            ThemeMode.valueOf(modeName)
        } catch (_: Exception) {
            ThemeMode.System
        }
    }

    val isDeveloperModeEnabled: Flow<Boolean> = context.sonoraDataStore.data.map { prefs ->
        prefs[Keys.DEVELOPER_MODE_ENABLED] ?: false
    }

    val isHapticsEnabled: Flow<Boolean> = context.sonoraDataStore.data.map { prefs ->
        prefs[Keys.HAPTICS_ENABLED] ?: true
    }

    val developerSettings: Flow<DeveloperSettings> = context.sonoraDataStore.data.map { prefs ->
        DeveloperSettings(
            blurRadius = prefs[Keys.DEV_BLUR_RADIUS] ?: 24f,
            tintAlpha = prefs[Keys.DEV_TINT_ALPHA] ?: 0.78f,
            cornerRadius = prefs[Keys.DEV_CORNER_RADIUS] ?: 24f,
            borderStroke = prefs[Keys.DEV_BORDER_STROKE] ?: 0.5f,
            borderAlpha = prefs[Keys.DEV_BORDER_ALPHA] ?: 0.20f,
            materialStyle = prefs[Keys.DEV_MATERIAL_STYLE] ?: "Regular",
            tintColorArgb = prefs[Keys.DEV_TINT_COLOR_ARGB] ?: 0L,
            backdropBlurEnabled = prefs[Keys.DEV_BACKDROP_BLUR_ENABLED] ?: true,
            playerStiffness = prefs[Keys.DEV_PLAYER_STIFFNESS] ?: 400f,
            playerDamping = prefs[Keys.DEV_PLAYER_DAMPING] ?: 0.85f,
            completionThreshold = prefs[Keys.DEV_COMPLETION_THRESHOLD] ?: 0.38f,
            velocityThreshold = prefs[Keys.DEV_VELOCITY_THRESHOLD] ?: 900f
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.sonoraDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDeveloperModeEnabled(enabled: Boolean) {
        context.sonoraDataStore.edit { prefs ->
            prefs[Keys.DEVELOPER_MODE_ENABLED] = enabled
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.sonoraDataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun updateDeveloperSettings(settings: DeveloperSettings) {
        context.sonoraDataStore.edit { prefs ->
            prefs[Keys.DEV_BLUR_RADIUS] = settings.blurRadius
            prefs[Keys.DEV_TINT_ALPHA] = settings.tintAlpha
            prefs[Keys.DEV_CORNER_RADIUS] = settings.cornerRadius
            prefs[Keys.DEV_BORDER_STROKE] = settings.borderStroke
            prefs[Keys.DEV_BORDER_ALPHA] = settings.borderAlpha
            prefs[Keys.DEV_MATERIAL_STYLE] = settings.materialStyle
            prefs[Keys.DEV_TINT_COLOR_ARGB] = settings.tintColorArgb
            prefs[Keys.DEV_BACKDROP_BLUR_ENABLED] = settings.backdropBlurEnabled
            prefs[Keys.DEV_PLAYER_STIFFNESS] = settings.playerStiffness
            prefs[Keys.DEV_PLAYER_DAMPING] = settings.playerDamping
            prefs[Keys.DEV_COMPLETION_THRESHOLD] = settings.completionThreshold
            prefs[Keys.DEV_VELOCITY_THRESHOLD] = settings.velocityThreshold
        }
    }

    suspend fun resetDeveloperSettings() {
        context.sonoraDataStore.edit { prefs ->
            prefs.remove(Keys.DEV_BLUR_RADIUS)
            prefs.remove(Keys.DEV_TINT_ALPHA)
            prefs.remove(Keys.DEV_CORNER_RADIUS)
            prefs.remove(Keys.DEV_BORDER_STROKE)
            prefs.remove(Keys.DEV_BORDER_ALPHA)
            prefs.remove(Keys.DEV_MATERIAL_STYLE)
            prefs.remove(Keys.DEV_TINT_COLOR_ARGB)
            prefs.remove(Keys.DEV_BACKDROP_BLUR_ENABLED)
            prefs.remove(Keys.DEV_PLAYER_STIFFNESS)
            prefs.remove(Keys.DEV_PLAYER_DAMPING)
            prefs.remove(Keys.DEV_COMPLETION_THRESHOLD)
            prefs.remove(Keys.DEV_VELOCITY_THRESHOLD)
        }
    }
}
