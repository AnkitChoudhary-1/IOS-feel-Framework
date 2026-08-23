package dev.iosfeel.sonora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import dev.iosfeel.sonora.core.datastore.SonoraPreferences
import dev.iosfeel.sonora.core.datastore.ThemeMode
import dev.iosfeel.sonora.core.design.SonoraTheme
import dev.iosfeel.sonora.core.di.SonoraContainer
import dev.iosfeel.sonora.navigation.SonoraNavigationShell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SonoraApp()
        }
    }
}

@Composable
fun SonoraApp() {
    val context = LocalContext.current
    val container = remember(context) { SonoraContainer.getInstance(context) }
    val preferences = container.preferences

    val themeMode by preferences.themeMode.collectAsState(initial = ThemeMode.System)
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.System -> isSystemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    SonoraTheme(darkTheme = isDark) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = SonoraTheme.colors.background
        ) {
            SonoraNavigationShell(preferences = preferences)
        }
    }
}
