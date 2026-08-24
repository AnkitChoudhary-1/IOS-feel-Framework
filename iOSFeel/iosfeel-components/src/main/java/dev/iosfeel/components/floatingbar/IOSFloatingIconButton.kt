package dev.iosfeel.components.floatingbar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.interaction.iosPressSurface
import dev.iosfeel.components.interaction.rememberIOSPressSurfaceState
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * High-fidelity floating circular icon button with frosted backdrop and V2 physics press.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
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
    val pressState = rememberIOSPressSurfaceState()

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
            .iosPressSurface(
                state = pressState,
                pressedScale = 0.92f,
                onClick = {
                    if (hapticsEnabled) {
                        haptics.selection()
                    }
                    onClick()
                }
            )
            .semantics { role = Role.Button },
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
