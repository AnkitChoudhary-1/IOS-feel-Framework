package dev.iosfeel.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import dev.iosfeel.motion.IOSMotionPreset
import dev.iosfeel.motion.IOSSpringSpec

@Stable
class IOSPushTransitionState {

    val progress = Animatable(1f)

    val isPushing: Boolean
        get() = progress.value < 0.999f

    suspend fun prepare() {
        progress.stop()
        progress.snapTo(0f)
    }

    suspend fun animate(
        spec: IOSSpringSpec = IOSMotionPreset.Smooth
    ) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                stiffness = spec.stiffness,
                dampingRatio = spec.dampingRatio
            )
        )
    }

    suspend fun finish() {
        progress.stop()
        progress.snapTo(1f)
    }
}
