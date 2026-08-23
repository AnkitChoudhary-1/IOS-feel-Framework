package dev.iosfeel.sonora.feature.player

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class PlayerLayoutConfig(
    val miniPlayerHeight: Dp = 64.dp,
    val miniArtworkSize: Dp = 48.dp,
    val expandedArtworkMaxSize: Dp = 340.dp,
    val expandedHorizontalPadding: Dp = 24.dp,
    val miniCornerRadius: Dp = 16.dp,
    val expandedCornerRadius: Dp = 0.dp
)
