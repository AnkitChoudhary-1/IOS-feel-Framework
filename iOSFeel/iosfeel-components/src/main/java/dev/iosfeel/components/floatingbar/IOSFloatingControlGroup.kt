package dev.iosfeel.components.floatingbar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
fun IOSFloatingControlGroup(
    modifier: Modifier = Modifier,
    height: Dp = 42.dp,
    backdrop: IOSBackdropState? = null,
    materialStyle: IOSFloatingMaterialStyle = IOSFloatingMaterialStyle.Regular,
    shape: Shape = IOSFloatingShapes.Group,
    elevation: Dp = 4.dp,
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    content: @Composable RowScope.() -> Unit
) {
    Box(
        modifier = modifier
            .height(height)
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = Color.Black.copy(alpha = 0.18f)
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
                cornerRadius = 24.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .height(height)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                content()
            }
        }
    }
}
