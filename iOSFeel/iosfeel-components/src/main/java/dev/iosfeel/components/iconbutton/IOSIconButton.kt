package dev.iosfeel.components.iconbutton

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.interaction.iosPressSurface
import dev.iosfeel.components.interaction.rememberIOSPressSurfaceState
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Standard iOS Icon Button with 44dp+ minimum touch target and V2 physics press.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
@Composable
fun IOSIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    material: Boolean = false,
    contentDescription: String? = null,
    size: Dp = 44.dp,
    icon: @Composable () -> Unit
) {
    val haptics = rememberIOSHaptics()
    val pressState = rememberIOSPressSurfaceState()

    val interactionModifier = Modifier
        .size(size)
        .iosPressSurface(
            state = pressState,
            enabled = enabled,
            pressedScale = 0.92f,
            onClick = {
                haptics.impact(IOSImpact.Light)
                onClick()
            }
        )
        .semantics {
            role = Role.Button
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            }
        }

    if (material) {
        IOSMaterialSurface(
            config = IOSMaterialConfig(
                style = IOSMaterialStyle.Thin,
                cornerRadius = size / 2
            ),
            modifier = modifier.then(interactionModifier)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
        }
    } else {
        Box(
            modifier = modifier.then(interactionModifier),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}
