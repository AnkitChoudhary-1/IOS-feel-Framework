package dev.iosfeel.dayline.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import dev.iosfeel.components.theme.IOSFeelTheme
import dev.iosfeel.dayline.core.datastore.AppTheme

@Composable
fun DaylineTheme(
    appTheme: AppTheme = AppTheme.System,
    darkTheme: Boolean = when (appTheme) {
        AppTheme.System -> isSystemInDarkTheme()
        AppTheme.Light -> false
        AppTheme.Dark -> true
    },
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DaylineColors.Dark else DaylineColors.Light
    val typography = DaylineTypographyScheme()

    IOSFeelTheme(
        darkTheme = darkTheme
    ) {
        CompositionLocalProvider(
            LocalDaylineColors provides colors,
            LocalDaylineTypography provides typography,
            content = content
        )
    }
}

object DaylineTheme {
    val colors: DaylineColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalDaylineColors.current

    val typography: DaylineTypographyScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalDaylineTypography.current
}
