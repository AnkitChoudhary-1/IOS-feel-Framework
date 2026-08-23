package dev.iosfeel.components.floatingbar

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.interaction.IOSPressConfig
import dev.iosfeel.components.interaction.iosPressEffect
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialSurface

@Composable
fun IOSFloatingIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp,
    backdrop: IOSBackdropState? = null,
    materialStyle: IOSFloatingMaterialStyle = IOSFloatingMaterialStyle.Regular,
    shape: Shape = CircleShape,
    elevation: Dp = 4.dp,
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    hapticsEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val haptics = rememberIOSHaptics()
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
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
            .iosPressEffect(
                interactionSource = interactionSource,
                config = IOSPressConfig(pressedScale = 0.92f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button
            ) {
                if (hapticsEnabled) {
                    haptics.selection()
                }
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        IOSMaterialSurface(
            backdrop = backdrop,
            config = IOSMaterialConfig(
                style = materialStyle.toMaterialStyle(),
                cornerRadius = 24.dp
            )
        ) {
            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}
