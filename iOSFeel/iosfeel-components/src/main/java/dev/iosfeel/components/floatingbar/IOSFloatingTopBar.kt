package dev.iosfeel.components.floatingbar

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface

@Composable
fun IOSFloatingTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    titleAlpha: Float = 1f,
    navigation: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    backdrop: IOSBackdropState? = null,
    titleColor: Color = Color.Unspecified,
    showTitleAsPill: Boolean = false,
    titleIcon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (!showTitleAsPill && title != null && titleAlpha > 0.01f) {
            val resolvedTitleColor = if (titleColor != Color.Unspecified) {
                titleColor.copy(alpha = titleAlpha.coerceIn(0f, 1f))
            } else {
                Color.Unspecified
            }
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = resolvedTitleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 56.dp)
                    .graphicsLayer {
                        if (titleColor == Color.Unspecified) {
                            alpha = titleAlpha.coerceIn(0f, 1f)
                        }
                    }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                navigation?.invoke()

                if (showTitleAsPill && title != null && titleAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .graphicsLayer {
                                alpha = titleAlpha
                                scaleX = 0.9f + (0.1f * titleAlpha)
                                scaleY = 0.9f + (0.1f * titleAlpha)
                            }
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                spotColor = Color.Black.copy(alpha = 0.18f)
                            )
                            .clip(CircleShape)
                            .border(
                                width = 0.5.dp,
                                color = Color.White.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    ) {
                        IOSMaterialSurface(
                            backdrop = backdrop,
                            config = IOSMaterialConfig(
                                style = IOSMaterialStyle.Regular,
                                cornerRadius = 20.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .height(40.dp)
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                titleIcon?.invoke()
                                Text(
                                    text = title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (titleColor != Color.Unspecified) titleColor else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Box(contentAlignment = Alignment.CenterEnd) {
                actions?.invoke()
            }
        }
    }
}

