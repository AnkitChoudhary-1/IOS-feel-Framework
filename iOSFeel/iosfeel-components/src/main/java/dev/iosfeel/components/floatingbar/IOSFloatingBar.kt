package dev.iosfeel.components.floatingbar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialSurface

import dev.iosfeel.material.LocalIOSDarkTheme

@Composable
fun IOSFloatingBar(
    modifier: Modifier = Modifier,
    backdrop: IOSBackdropState? = null,
    materialStyle: IOSFloatingMaterialStyle = IOSFloatingMaterialStyle.Regular,
    shape: Shape = IOSFloatingBarDefaults.Shape,
    elevation: Dp = IOSFloatingBarDefaults.Elevation,
    borderColor: Color = Color.Unspecified,
    content: @Composable () -> Unit
) {
    val isDark = LocalIOSDarkTheme.current
    val resolvedBorderColor = if (borderColor != Color.Unspecified) {
        borderColor
    } else if (isDark) {
        Color.White.copy(alpha = 0.15f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = if (isDark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.12f),
                ambientColor = if (isDark) Color.Black.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.06f)
            )
            .clip(shape)
            .border(
                width = IOSFloatingBarDefaults.BorderWidth,
                color = resolvedBorderColor,
                shape = shape
            )
    ) {
        IOSMaterialSurface(
            backdrop = backdrop,
            config = IOSMaterialConfig(
                style = materialStyle.toMaterialStyle(),
                cornerRadius = 28.dp
            )
        ) {
            content()
        }
    }
}
