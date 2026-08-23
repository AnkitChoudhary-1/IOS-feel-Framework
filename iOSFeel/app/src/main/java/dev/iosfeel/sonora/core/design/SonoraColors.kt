package dev.iosfeel.sonora.core.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class SonoraColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceSecondary: Color,
    val surfaceTertiary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentSecondary: Color,
    val separator: Color,
    val border: Color,
    val playerBackground: Color
)

object SonoraColors {
    val Light = SonoraColorScheme(
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFF6F6F9),
        surfaceSecondary = Color(0xFFEBEBF0),
        surfaceTertiary = Color(0xFFDFDFE5),
        textPrimary = Color(0xFF000000),
        textSecondary = Color(0xFF6C6C70),
        textTertiary = Color(0xFF8E8E93),
        accent = Color(0xFFFA233B), // Apple Music Red/Pink
        accentSecondary = Color(0xFFFF453A),
        separator = Color(0x1F000000),
        border = Color(0x14000000),
        playerBackground = Color(0xFFF9F9FB)
    )

    val Dark = SonoraColorScheme(
        background = Color(0xFF000000),
        surface = Color(0xFF161618),
        surfaceSecondary = Color(0xFF242426),
        surfaceTertiary = Color(0xFF323236),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF8E8E93),
        textTertiary = Color(0xFF636366),
        accent = Color(0xFFFF2D55), // Apple Music Vivid Red/Pink
        accentSecondary = Color(0xFFFF375F),
        separator = Color(0x2EFFFFFF),
        border = Color(0x1FFFFFFF),
        playerBackground = Color(0xFF141416)
    )
}
