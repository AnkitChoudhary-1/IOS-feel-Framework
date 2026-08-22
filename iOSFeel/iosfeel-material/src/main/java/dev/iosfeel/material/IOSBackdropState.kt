package dev.iosfeel.material

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.layer.GraphicsLayer

@Stable
class IOSBackdropState internal constructor(
    val layer: GraphicsLayer
)

data class IOSBackdropRegion(
    val left: Float = 0f,
    val top: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f
)
