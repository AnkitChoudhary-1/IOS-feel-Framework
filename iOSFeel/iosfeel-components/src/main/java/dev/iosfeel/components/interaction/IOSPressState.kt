package dev.iosfeel.components.interaction

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

@Stable
class IOSPressState {
    val progress: Animatable<Float, AnimationVector1D> = Animatable(0f)

    val isPressed: Boolean
        get() = progress.value > 0f
}

@Composable
fun rememberIOSPressState(): IOSPressState {
    return remember { IOSPressState() }
}
