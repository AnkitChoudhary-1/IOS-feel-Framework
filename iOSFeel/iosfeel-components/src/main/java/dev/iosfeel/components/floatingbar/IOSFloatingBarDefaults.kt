package dev.iosfeel.components.floatingbar

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.material.IOSMaterialStyle

enum class IOSFloatingMaterialStyle {
    Thin,
    Regular,
    Thick;

    fun toMaterialStyle(): IOSMaterialStyle {
        return when (this) {
            Thin -> IOSMaterialStyle.UltraThin
            Regular -> IOSMaterialStyle.Regular
            Thick -> IOSMaterialStyle.Thick
        }
    }
}

enum class IOSFloatingBarMinimizeBehavior {
    Never,
    OnScrollDown,
    AlwaysCompact
}

object IOSFloatingBarDefaults {
    val Height: Dp = 60.dp
    val CompactHeight: Dp = 52.dp
    val HorizontalPadding: Dp = 16.dp
    val BottomPadding: Dp = 12.dp
    val TopPadding: Dp = 8.dp
    val Elevation: Dp = 8.dp
    val BorderWidth: Dp = 0.5.dp

    val Shape: Shape = IOSFloatingShapes.Bar
    val ControlShape: Shape = IOSFloatingShapes.Control
    val GroupShape: Shape = IOSFloatingShapes.Group

    val BorderColorLight: Color = Color.Black.copy(alpha = 0.08f)
    val BorderColorDark: Color = Color.White.copy(alpha = 0.12f)
}
