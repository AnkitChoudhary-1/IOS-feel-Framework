package dev.iosfeel.sonora.core.design.sheet

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.iosfeel.components.interaction.iosPressSurface
import dev.iosfeel.components.interaction.rememberIOSPressSurfaceState
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.sonora.core.design.LocalSonoraColors
import dev.iosfeel.sonora.core.design.LocalSonoraTypography

import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class SonoraActionItem(
    val title: String,
    val icon: ImageVector? = null,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalIOSFeelV2Api::class)
@Composable
fun SonoraActionSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String? = null,
    subtitle: String? = null,
    actions: List<SonoraActionItem>,
    cancelText: String = "Cancel",
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val colors = LocalSonoraColors.current
    val typography = LocalSonoraTypography.current
    val haptics = rememberIOSHaptics()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(spring(stiffness = 500f, dampingRatio = 0.9f)),
            exit = fadeOut(spring(stiffness = 500f, dampingRatio = 0.9f)),
            modifier = modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .animateEnterExit(
                            enter = slideInVertically(
                                initialOffsetY = { it },
                                animationSpec = spring(stiffness = 450f, dampingRatio = 0.86f)
                            ),
                            exit = slideOutVertically(
                                targetOffsetY = { it },
                                animationSpec = spring(stiffness = 450f, dampingRatio = 0.86f)
                            )
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                // Actions container card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceElevated)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (title != null || subtitle != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (title != null) {
                                    Text(
                                        text = title,
                                        style = typography.subheadline.copy(fontWeight = FontWeight.SemiBold),
                                        color = colors.textPrimary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                if (subtitle != null) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = subtitle,
                                        style = typography.caption1,
                                        color = colors.textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(colors.separator.copy(alpha = 0.3f))
                            )
                        }

                        actions.forEachIndexed { index, action ->
                            val pressState = rememberIOSPressSurfaceState()
                            val textColor = if (action.isDestructive) Color(0xFFFF3B30) else colors.textPrimary
                            val iconColor = if (action.isDestructive) Color(0xFFFF3B30) else colors.accent

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .iosPressSurface(
                                        state = pressState,
                                        pressedScale = 0.97f,
                                        onClick = {
                                            if (action.isDestructive) {
                                                haptics.impact(IOSImpact.Heavy)
                                            } else {
                                                haptics.impact(IOSImpact.Light)
                                            }
                                            onDismiss()
                                            action.onClick()
                                        }
                                    )
                                    .padding(horizontal = 20.dp, vertical = 15.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (action.icon != null) {
                                    Icon(
                                        imageVector = action.icon,
                                        contentDescription = action.title,
                                        tint = iconColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                }

                                Text(
                                    text = action.title,
                                    style = typography.body.copy(
                                        fontWeight = if (action.isDestructive) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = textColor,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (index < actions.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = if (action.icon != null) 56.dp else 20.dp)
                                        .height(0.5.dp)
                                        .background(colors.separator.copy(alpha = 0.3f))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Separate Cancel button
                val cancelPressState = rememberIOSPressSurfaceState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceElevated)
                        .iosPressSurface(
                            state = cancelPressState,
                            pressedScale = 0.97f,
                            onClick = {
                                haptics.impact(IOSImpact.Light)
                                onDismiss()
                            }
                        )
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cancelText,
                        style = typography.headline.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.accent
                    )
                }
            }
        }
    }
}
}
