package dev.iosfeel.components.floatingbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.iosfeel.material.IOSBackdropState
import dev.iosfeel.material.IOSMaterialConfig
import dev.iosfeel.material.IOSMaterialStyle
import dev.iosfeel.material.IOSMaterialSurface

/**
 * Modern iOS Frosted Floating Title Pill.
 *
 * Provides a glassmorphic floating pill with smooth spring scale and alpha animation,
 * ideal for inline headers, category chips, and collapsing scroll titles.
 *
 * @param title The primary text displayed within the pill.
 * @param visible Controls the animated visibility of the pill.
 * @param modifier Modifier applied to the outer layout.
 * @param icon Optional leading icon or artwork composable.
 * @param height Height of the pill (defaults to 40.dp).
 * @param cornerRadius Corner radius of the pill capsule (defaults to 20.dp).
 * @param backdrop Optional backdrop state for real-time backdrop blur rendering.
 * @param style Frosted glass material style (defaults to [IOSMaterialStyle.Regular]).
 * @param textColor Color of the title text.
 * @param onClick Optional tap callback.
 */
@Composable
fun IOSAnimatedTitlePill(
    title: String,
    visible: Boolean,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    height: Dp = 40.dp,
    cornerRadius: Dp = 20.dp,
    backdrop: IOSBackdropState? = null,
    style: IOSMaterialStyle = IOSMaterialStyle.Regular,
    textColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.85f),
        label = "ios_animated_pill_alpha"
    )

    if (animatedAlpha > 0.01f) {
        val clickModifier = if (onClick != null) {
            Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
        } else {
            Modifier
        }

        Box(
            modifier = modifier
                .height(height)
                .graphicsLayer {
                    alpha = animatedAlpha
                    scaleX = 0.88f + (0.12f * animatedAlpha)
                    scaleY = 0.88f + (0.12f * animatedAlpha)
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
                .then(clickModifier)
        ) {
            IOSMaterialSurface(
                backdrop = backdrop,
                config = IOSMaterialConfig(
                    style = style,
                    cornerRadius = cornerRadius
                )
            ) {
                Row(
                    modifier = Modifier
                        .height(height)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    icon?.invoke()
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (textColor != Color.Unspecified) textColor else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
