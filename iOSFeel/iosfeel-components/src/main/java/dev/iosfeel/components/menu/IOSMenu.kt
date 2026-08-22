package dev.iosfeel.components.menu

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.iosfeel.components.interaction.IOSPressConfig
import dev.iosfeel.components.interaction.iosPressEffect
import dev.iosfeel.core.tokens.IOSActionRole
import dev.iosfeel.core.tokens.IOSMotionTokens
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface

@Composable
fun IOSMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: IOSMenuScope.() -> Unit
) {
    if (!expanded) return

    val haptics = rememberIOSHaptics()
    val scope = remember(content) {
        IOSMenuScopeImpl().apply(content)
    }

    val visibility = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        visibility.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                stiffness = IOSMotionTokens.PressStiffness,
                dampingRatio = IOSMotionTokens.PressDampingRatio
            )
        )
    }

    Popup(
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f * visibility.value))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onDismissRequest()
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = modifier
                    .widthIn(min = 220.dp, max = 280.dp)
                    .graphicsLayer {
                        alpha = visibility.value
                        scaleX = 0.94f + 0.06f * visibility.value
                        scaleY = scaleX
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Prevent click through */ }
            ) {
                IOSMaterialSurface(
                    config = IOSMaterialConfig(
                        style = IOSMaterialStyle.Thick,
                        cornerRadius = 16.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        scope.items.forEach { element ->
                            when (element) {
                                is MenuElement.Action -> {
                                    val item = element.item
                                    val itemInteraction = remember { MutableInteractionSource() }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics { role = Role.Button }
                                            .iosPressEffect(
                                                interactionSource = itemInteraction,
                                                config = IOSPressConfig(pressedScale = 0.985f, pressedAlpha = 0.8f)
                                            )
                                            .clickable(
                                                interactionSource = itemInteraction,
                                                indication = null
                                            ) {
                                                haptics.impact(IOSImpact.Light)
                                                onDismissRequest()
                                                item.onClick()
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.label,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = when (item.role) {
                                                IOSActionRole.Destructive -> Color(0xFFFF453A)
                                                IOSActionRole.Normal -> Color.White
                                            }
                                        )

                                        if (item.icon != null) {
                                            Text(
                                                text = item.icon,
                                                fontSize = 16.sp,
                                                color = when (item.role) {
                                                    IOSActionRole.Destructive -> Color(0xFFFF453A)
                                                    IOSActionRole.Normal -> Color.White.copy(alpha = 0.8f)
                                                }
                                            )
                                        }
                                    }
                                }
                                is MenuElement.Separator -> {
                                    HorizontalDivider(
                                        modifier = Modifier.fillMaxWidth(),
                                        thickness = 0.5.dp,
                                        color = Color.White.copy(alpha = 0.15f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
