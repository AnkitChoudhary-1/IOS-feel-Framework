package dev.iosfeel.components.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

object IOSFeelTheme {
    val colors: IOSFeelColors
        @Composable
        @ReadOnlyComposable
        get() = LocalIOSFeelColors.current

    val typography: IOSFeelTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalIOSFeelTypography.current

    val shapes: IOSFeelShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalIOSFeelShapes.current
}

@Composable
fun IOSFeelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) IOSFeelDarkColors else IOSFeelLightColors

    CompositionLocalProvider(
        LocalIOSFeelColors provides colors,
        LocalIOSFeelTypography provides IOSFeelTypography.Default,
        LocalIOSFeelShapes provides IOSFeelShapes.Default
    ) {
        content()
    }
}
