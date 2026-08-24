package dev.iosfeel.sonora.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

import dev.iosfeel.material.LocalIOSDarkTheme

val LocalSonoraColors = staticCompositionLocalOf { SonoraColors.Dark }
val LocalSonoraTypography = staticCompositionLocalOf { SonoraTypography.Default }

object SonoraTheme {
    val colors: SonoraColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalSonoraColors.current

    val typography: SonoraTypographyScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalSonoraTypography.current
}

@Composable
fun SonoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SonoraColors.Dark else SonoraColors.Light

    CompositionLocalProvider(
        LocalSonoraColors provides colorScheme,
        LocalSonoraTypography provides SonoraTypography.Default,
        LocalIOSDarkTheme provides darkTheme,
        content = content
    )
}
