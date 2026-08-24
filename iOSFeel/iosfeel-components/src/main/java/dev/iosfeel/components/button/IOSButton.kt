package dev.iosfeel.components.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.iosfeel.components.interaction.IOSComponentShapes
import dev.iosfeel.components.interaction.iosPressSurface
import dev.iosfeel.components.interaction.rememberIOSPressSurfaceState
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Standard iOS Button V2 with physics press surface and gesture arena integration.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
@Composable
fun IOSButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: IOSButtonStyle = IOSButtonStyle.Filled,
    enabled: Boolean = true,
    hapticFeedback: Boolean = true,
    backdrop: IOSBackdropState? = null,
    content: @Composable () -> Unit
) {
    val haptics = rememberIOSHaptics()
    val pressState = rememberIOSPressSurfaceState()

    val interactionModifier = Modifier
        .defaultMinSize(minHeight = IOSButtonDefaults.MinHeight)
        .iosPressSurface(
            state = pressState,
            enabled = enabled,
            pressedScale = 0.96f,
            pressedAlpha = if (style == IOSButtonStyle.Plain) 0.5f else 1.0f,
            onClick = {
                if (hapticFeedback) {
                    haptics.impact(IOSImpact.Light)
                }
                onClick()
            }
        )
        .semantics { role = Role.Button }

    val contentColor = IOSButtonDefaults.contentColorFor(style)

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        when (style) {
            IOSButtonStyle.Material -> {
                IOSMaterialSurface(
                    backdrop = backdrop,
                    config = IOSMaterialConfig(
                        style = IOSMaterialStyle.Thin,
                        cornerRadius = IOSButtonDefaults.CornerRadius
                    ),
                    modifier = modifier.then(interactionModifier)
                ) {
                    Box(
                        modifier = Modifier.padding(IOSButtonDefaults.ContentPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        content()
                    }
                }
            }
            else -> {
                val containerColor = IOSButtonDefaults.containerColorFor(style)
                Box(
                    modifier = modifier
                        .then(interactionModifier)
                        .clip(IOSComponentShapes.Control)
                        .background(containerColor)
                        .padding(IOSButtonDefaults.ContentPadding),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * Text convenience overload for [IOSButton].
 */
@Composable
fun IOSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: IOSButtonStyle = IOSButtonStyle.Filled,
    enabled: Boolean = true,
    hapticFeedback: Boolean = true,
    backdrop: IOSBackdropState? = null
) {
    IOSButton(
        onClick = onClick,
        modifier = modifier,
        style = style,
        enabled = enabled,
        hapticFeedback = hapticFeedback,
        backdrop = backdrop
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
