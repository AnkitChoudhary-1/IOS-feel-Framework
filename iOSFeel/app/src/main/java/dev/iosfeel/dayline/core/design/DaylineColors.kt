package dev.iosfeel.dayline.core.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class DaylineColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceSecondary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val success: Color,
    val destructive: Color,
    val separator: Color,
    val border: Color
)

object DaylineColors {
    val Light = DaylineColorScheme(
        background = Color(0xFFFBFBFD),
        surface = Color(0xFFFFFFFF),
        surfaceSecondary = Color(0xFFF2F2F7),
        textPrimary = Color(0xFF111113),
        textSecondary = Color(0xFF6C6C70),
        textTertiary = Color(0xFF8E8E93),
        accent = Color(0xFF007AFF),
        success = Color(0xFF34C759),
        destructive = Color(0xFFFF3B30),
        separator = Color(0x1F000000),
        border = Color(0x12000000)
    )

    val Dark = DaylineColorScheme(
        background = Color(0xFF0A0A0C),
        surface = Color(0xFF161618),
        surfaceSecondary = Color(0xFF1C1C1E),
        textPrimary = Color(0xFFF5F5F7),
        textSecondary = Color(0xFF8E8E93),
        textTertiary = Color(0xFF636366),
        accent = Color(0xFF0A84FF),
        success = Color(0xFF30D158),
        destructive = Color(0xFFFF453A),
        separator = Color(0x2EFFFFFF),
        border = Color(0x20FFFFFF)
    )
}
