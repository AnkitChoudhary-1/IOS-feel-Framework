package dev.iosfeel.dayline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.iosfeel.dayline.core.datastore.AppTheme
import dev.iosfeel.dayline.core.datastore.DaylinePreferences
import dev.iosfeel.dayline.core.design.DaylineTheme
import dev.iosfeel.dayline.navigation.DaylineNavigationShell

@Composable
fun DaylineApp() {
    val context = LocalContext.current
    val preferences = remember { DaylinePreferences(context) }
    val currentTheme by preferences.theme.collectAsState(initial = AppTheme.System)

    DaylineTheme(appTheme = currentTheme) {
        DaylineNavigationShell(preferences = preferences)
    }
}
