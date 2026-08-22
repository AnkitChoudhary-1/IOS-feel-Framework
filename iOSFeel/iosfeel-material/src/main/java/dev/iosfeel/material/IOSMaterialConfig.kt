package dev.iosfeel.material

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class IOSMaterialConfig(
    val style: IOSMaterialStyle = IOSMaterialStyle.Regular,
    val tint: Color? = null,
    val cornerRadius: Dp = 20.dp,
    val borderStroke: Dp = 0.5.dp,
    val borderColor: Color? = null,
    val enabled: Boolean = true
)
