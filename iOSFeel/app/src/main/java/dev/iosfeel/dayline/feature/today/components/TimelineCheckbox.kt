package dev.iosfeel.dayline.feature.today.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.iosfeel.dayline.core.design.DaylineTheme
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.IOSNotification
import dev.iosfeel.haptics.rememberIOSHaptics

@Composable
fun TimelineCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = DaylineTheme.colors.accent
) {
    val colors = DaylineTheme.colors
    val haptics = rememberIOSHaptics()

    val scale by animateFloatAsState(
        targetValue = if (checked) 1.0f else 0.95f,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.7f),
        label = "checkboxScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (checked) accentColor else Color.Transparent,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
        label = "checkboxBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) accentColor else colors.textTertiary.copy(alpha = 0.5f),
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.8f),
        label = "checkboxBorder"
    )

    Box(
        modifier = modifier
            .size(24.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.5.dp, borderColor, CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (!checked) {
                    haptics.notification(IOSNotification.Success)
                } else {
                    haptics.impact(IOSImpact.Light)
                }
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Completed",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
