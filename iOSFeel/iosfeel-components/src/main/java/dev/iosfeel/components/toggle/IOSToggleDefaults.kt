package dev.iosfeel.components.toggle

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.core.tokens.IOSColors

object IOSToggleDefaults {

    val TrackWidth: Dp = 51.dp
    val TrackHeight: Dp = 31.dp
    val ThumbSize: Dp = 27.dp
    val ThumbPadding: Dp = 2.dp

    val ActiveTrackColor: Color = IOSColors.SystemGreen
    val InactiveTrackColor: Color = Color(0xFFE9E9EA)
    val DarkInactiveTrackColor: Color = Color(0xFF39393D)
    val ThumbColor: Color = Color.White
}
