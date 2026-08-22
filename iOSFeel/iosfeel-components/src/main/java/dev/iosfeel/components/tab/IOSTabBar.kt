package dev.iosfeel.components.tab

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.components.badge.IOSBadge
import dev.iosfeel.components.interaction.IOSPressConfig
import dev.iosfeel.components.interaction.iosPressEffect
import dev.iosfeel.core.tokens.IOSMotionTokens
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface

@Composable
fun <T> IOSTabBar(
    items: List<IOSTabItem<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    onReselect: ((T) -> Unit)? = null,
    backdrop: IOSBackdropState? = null,
    activeColor: Color = Color(0xFF007AFF),
    inactiveColor: Color = Color(0xFF8E8E93),
    cornerRadius: Dp = 0.dp,
    hapticsEnabled: Boolean = true
) {
    val haptics = rememberIOSHaptics()

    IOSMaterialSurface(
        backdrop = backdrop,
        config = IOSMaterialConfig(
            style = IOSMaterialStyle.Regular,
            cornerRadius = cornerRadius
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(54.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = item.value == selected
                val interactionSource = remember { MutableInteractionSource() }

                val scaleAnim = remember { Animatable(1f) }
                LaunchedEffect(isSelected) {
                    if (isSelected) {
                        scaleAnim.animateTo(
                            targetValue = 1.12f,
                            animationSpec = spring(
                                stiffness = IOSMotionTokens.TabBarStiffness,
                                dampingRatio = IOSMotionTokens.TabBarDampingRatio
                            )
                        )
                        scaleAnim.animateTo(
                            targetValue = 1.0f,
                            animationSpec = spring(
                                stiffness = IOSMotionTokens.TabBarStiffness,
                                dampingRatio = IOSMotionTokens.TabBarDampingRatio
                            )
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            role = Role.Tab
                            this.selected = isSelected
                        }
                        .iosPressEffect(
                            interactionSource = interactionSource,
                            config = IOSPressConfig(pressedScale = 0.94f)
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            if (isSelected) {
                                onReselect?.invoke(item.value)
                            } else {
                                if (hapticsEnabled) {
                                    haptics.selection()
                                }
                                onSelected(item.value)
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier.graphicsLayer {
                            scaleX = scaleAnim.value
                            scaleY = scaleAnim.value
                        },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            item.icon(isSelected)

                            if (item.badgeCount != null && item.badgeCount > 0) {
                                IOSBadge(
                                    count = item.badgeCount,
                                    modifier = Modifier.offset(x = 8.dp, y = (-4).dp)
                                )
                            } else if (item.showBadgeDot) {
                                IOSBadge(
                                    count = null,
                                    modifier = Modifier.offset(x = 4.dp, y = (-2).dp)
                                )
                            }
                        }

                        if (item.label != null) {
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) activeColor else inactiveColor,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
