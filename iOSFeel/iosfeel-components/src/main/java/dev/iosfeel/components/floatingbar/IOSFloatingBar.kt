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

@Composable
fun IOSFloatingBar(
    modifier: Modifier = Modifier,
    backdrop: IOSBackdropState? = null,
    materialStyle: IOSFloatingMaterialStyle = IOSFloatingMaterialStyle.Regular,
    shape: Shape = IOSFloatingBarDefaults.Shape,
    elevation: Dp = IOSFloatingBarDefaults.Elevation,
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = Color.Black.copy(alpha = 0.22f),
                ambientColor = Color.Black.copy(alpha = 0.10f)
            )
            .clip(shape)
            .border(
                width = IOSFloatingBarDefaults.BorderWidth,
                color = borderColor,
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
