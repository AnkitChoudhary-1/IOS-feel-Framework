package dev.iosfeel.components.iconbutton

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.interaction.IOSPressConfig
import dev.iosfeel.components.interaction.iosPressEffect
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface

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
    val interactions = remember { MutableInteractionSource() }
    val haptics = rememberIOSHaptics()

    val interactionModifier = Modifier
        .size(size)
        .iosPressEffect(
            interactionSource = interactions,
            config = IOSPressConfig(pressedScale = 0.91f)
        )
        .clickable(
            enabled = enabled,
            interactionSource = interactions,
            indication = null
        ) {
            haptics.impact(IOSImpact.Light)
            onClick()
        }
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
