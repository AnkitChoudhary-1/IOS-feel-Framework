package dev.iosfeel.motion.morph

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings

/**
 * Universal state managing a normalized morph progress (0.0 to 1.0) and spring-driven interruptible animation.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSMorphState(
    initialProgress: Float = 0f,
    val springSpec: IOSSpringSpec = IOSSprings.Selection
) {
    internal val animatable = Animatable(initialProgress.coerceIn(0f, 1f))

    val progress: Float
        get() = animatable.value

    val isRunning: Boolean
        get() = animatable.isRunning

    suspend fun snapTo(targetProgress: Float) {
        animatable.snapTo(targetProgress.coerceIn(0f, 1f))
    }

    suspend fun animateTo(targetProgress: Float, velocity: Float = 0f) {
        animatable.animateTo(
            targetValue = targetProgress.coerceIn(0f, 1f),
            initialVelocity = velocity,
            animationSpec = SpringSpec(
                dampingRatio = springSpec.dampingRatio,
                stiffness = springSpec.stiffness
            )
        )
    }
}

/**
 * Creates and remembers an [IOSMorphState].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSMorphState(
    initialProgress: Float = 0f,
    springSpec: IOSSpringSpec = IOSSprings.Selection
): IOSMorphState {
    return remember(springSpec) {
        IOSMorphState(initialProgress, springSpec)
    }
}
