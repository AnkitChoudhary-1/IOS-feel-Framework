package dev.iosfeel.components.search

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.core.tokens.IOSColors

object IOSSearchDefaults {
    val Height: Dp = 36.dp
    val CornerRadius: Dp = 10.dp
    val ContainerColor: Color = Color(0xFF1C1C1E).copy(alpha = 0.6f)
    val TextColor: Color = Color.White
    val PlaceholderColor: Color = Color.White.copy(alpha = 0.5f)
    val CancelTextColor: Color = IOSColors.SystemBlue
}
