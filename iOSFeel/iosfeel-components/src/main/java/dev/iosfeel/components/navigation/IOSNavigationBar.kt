package dev.iosfeel.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.components.interaction.IOSPressConfig
import dev.iosfeel.components.interaction.iosPressEffect
import dev.iosfeel.haptics.IOSImpact
import dev.iosfeel.haptics.rememberIOSHaptics
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface

@Composable
fun IOSNavigationBar(
    title: String,
    modifier: Modifier = Modifier,
    backButtonVisible: Boolean = false,
    backButtonLabel: String = "Back",
    onBack: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    backdrop: IOSBackdropState? = null,
    material: Boolean = true,
    backgroundColor: Color = Color(0xFF1C1C1E)
) {
    val haptics = rememberIOSHaptics()

    val content = @Composable {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(44.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Leading / Back button slot
            Box(
                modifier = Modifier.width(80.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (backButtonVisible && onBack != null) {
                    val backInteraction = remember { MutableInteractionSource() }

                    Row(
                        modifier = Modifier
                            .semantics { role = Role.Button }
                            .iosPressEffect(
                                interactionSource = backInteraction,
                                config = IOSPressConfig(pressedScale = 0.94f, pressedAlpha = 0.6f)
                            )
                            .clickable(
                                interactionSource = backInteraction,
                                indication = null
                            ) {
                                haptics.impact(IOSImpact.Light)
                                onBack()
                            }
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "‹",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Light,
                            color = Color(0xFF007AFF),
                            modifier = Modifier.padding(end = 2.dp)
                        )
                        Text(
                            text = backButtonLabel,
                            fontSize = 17.sp,
                            color = Color(0xFF007AFF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Center Title Slot
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Trailing Actions Slot
            Box(
                modifier = Modifier.width(80.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (trailing != null) {
                    trailing()
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }

    if (material) {
        IOSMaterialSurface(
            backdrop = backdrop,
            config = IOSMaterialConfig(
                style = IOSMaterialStyle.Regular,
                cornerRadius = 0.dp
            ),
            modifier = modifier.fillMaxWidth()
        ) {
            content()
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundColor)
        ) {
            content()
        }
    }
}
