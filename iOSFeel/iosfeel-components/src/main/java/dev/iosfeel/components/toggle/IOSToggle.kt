package dev.iosfeel.components.toggle

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.iosfeel.core.tokens.IOSMotionTokens
import dev.iosfeel.core.tokens.IOSShapes
import dev.iosfeel.haptics.rememberIOSHaptics
import kotlin.math.roundToInt

@Composable
fun IOSToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    activeColor: Color = IOSToggleDefaults.ActiveTrackColor,
    inactiveColor: Color = IOSToggleDefaults.DarkInactiveTrackColor,
    hapticsEnabled: Boolean = true
) {
    val haptics = rememberIOSHaptics()
    val density = LocalDensity.current

    val progress = remember {
        Animatable(if (checked) 1f else 0f)
    }

    LaunchedEffect(checked) {
        progress.animateTo(
            targetValue = if (checked) 1f else 0f,
            animationSpec = spring(
                stiffness = IOSMotionTokens.ToggleStiffness,
                dampingRatio = IOSMotionTokens.ToggleDampingRatio
            )
        )
    }

    val maxTravelPx = with(density) {
        (IOSToggleDefaults.TrackWidth - IOSToggleDefaults.ThumbSize - (IOSToggleDefaults.ThumbPadding * 2)).toPx()
    }

    val trackColor = lerp(inactiveColor, activeColor, progress.value)
    val thumbOffsetPx = (maxTravelPx * progress.value).roundToInt()

    Box(
        modifier = modifier
            .semantics { role = Role.Switch }
            .size(
                width = IOSToggleDefaults.TrackWidth,
                height = IOSToggleDefaults.TrackHeight
            )
            .clip(IOSShapes.Pill)
            .background(if (enabled) trackColor else trackColor.copy(alpha = 0.38f))
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (hapticsEnabled) {
                    haptics.selection()
                }
                onCheckedChange(!checked)
            }
            .padding(IOSToggleDefaults.ThumbPadding),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(x = thumbOffsetPx, y = 0) }
                .size(IOSToggleDefaults.ThumbSize)
                .shadow(elevation = 2.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(IOSToggleDefaults.ThumbColor)
        )
    }
}
