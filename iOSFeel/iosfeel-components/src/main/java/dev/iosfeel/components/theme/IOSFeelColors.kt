package dev.iosfeel.components.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class IOSFeelColors(
    val background: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val labelPrimary: Color,
    val labelSecondary: Color,
    val labelTertiary: Color,
    val separator: Color,
    val accent: Color,
    val destructive: Color,
    val success: Color,
    val warning: Color
)

val IOSFeelLightColors = IOSFeelColors(
    background = Color(0xFFF2F2F7),
    surface = Color.White,
    elevatedSurface = Color(0xFFFFFFFF),
    labelPrimary = Color(0xFF000000),
    labelSecondary = Color(0xFF8E8E93),
    labelTertiary = Color(0xFFC7C7CC),
    separator = Color(0xFF3C3C43).copy(alpha = 0.29f),
    accent = Color(0xFF007AFF),
    destructive = Color(0xFFFF3B30),
    success = Color(0xFF34C759),
    warning = Color(0xFFFF9500)
)

val IOSFeelDarkColors = IOSFeelColors(
    background = Color(0xFF000000),
    surface = Color(0xFF1C1C1E),
    elevatedSurface = Color(0xFF2C2C2E),
    labelPrimary = Color.White,
    labelSecondary = Color(0xFF8E8E93),
    labelTertiary = Color(0xFF48484A),
    separator = Color(0xFF545458).copy(alpha = 0.65f),
    accent = Color(0xFF0A84FF),
    destructive = Color(0xFFFF453A),
    success = Color(0xFF30D158),
    warning = Color(0xFFFF9F0A)
)

val LocalIOSFeelColors = staticCompositionLocalOf {
    IOSFeelDarkColors
}
