package dev.iosfeel.components.interaction

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Modifier.iosPressEffect(
    interactionSource: MutableInteractionSource,
    config: IOSPressConfig = IOSPressConfig()
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val progress = remember { Animatable(0f) }

    LaunchedEffect(pressed) {
        progress.animateTo(
            targetValue = if (pressed) 1f else 0f,
            animationSpec = spring(
                stiffness = if (pressed) config.pressStiffness else config.releaseStiffness,
                dampingRatio = if (pressed) config.pressDampingRatio else config.releaseDampingRatio
            )
        )
    }

    val scale = calculateIOSPressScale(
        progress = progress.value,
        pressedScale = config.pressedScale
    )

    val alpha = calculateIOSPressAlpha(
        progress = progress.value,
        pressedAlpha = config.pressedAlpha
    )

    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}
