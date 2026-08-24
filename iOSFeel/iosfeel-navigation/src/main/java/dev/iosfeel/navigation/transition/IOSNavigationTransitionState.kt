package dev.iosfeel.navigation.transition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.interruption.IOSInterruptibleMotion
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings

/**
 * Universal transition state driving screen navigation animations in iOSFeel V2.
 *
 * Backed by [IOSInterruptibleMotion] for instant owner transfer and zero-jump re-grabbing.
 * Normalized progress: 0f = rest / visible, 1f = fully transitioned / popped offscreen.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSNavigationTransitionState(
    initialProgress: Float = 0f
) {
    /**
     * Underlying interruptible motion controller.
     */
    val motion = IOSInterruptibleMotion(initialValue = initialProgress)

    /**
     * Normalized transition progress in range [0f, 1f].
     */
    val progress: Float
        get() = motion.state.value.coerceIn(0f, 1f)

    /**
     * Instantaneous velocity in normalized progress units per second.
     */
    val velocity: Float
        get() = motion.state.velocity

    /**
     * Origin or mechanism that initiated the active transition.
     */
    var source: IOSNavigationTransitionSource by mutableStateOf(IOSNavigationTransitionSource.Programmatic)
        internal set

    /**
     * Direction of the navigation transition.
     */
    var direction: IOSNavigationDirection by mutableStateOf(IOSNavigationDirection.None)
        internal set

    /**
     * Animates transition progress smoothly to [target] using [spec] and initial [velocity].
     */
    suspend fun animateTo(
        target: Float,
        velocity: Float = this.velocity,
        spec: IOSSpringSpec = IOSSprings.Navigation
    ) {
        motion.releaseToSpring(
            target = target,
            initialVelocity = velocity,
            spec = spec
        )
    }

    /**
     * Snaps transition progress immediately without animation.
     */
    fun snapTo(target: Float) {
        motion.state.update(
            value = target,
            velocity = 0f,
            target = target
        )
    }
}

/**
 * Source mechanism driving a navigation transition.
 */
@ExperimentalIOSFeelV2Api
enum class IOSNavigationTransitionSource {
    /**
     * Programmatic push or pop triggered by code/button tap.
     */
    Programmatic,

    /**
     * Interactive left-edge swipe gesture.
     */
    EdgeGesture,

    /**
     * System Android 14+ Predictive Back gesture.
     */
    PredictiveBack
}

/**
 * Direction of the active navigation transition.
 */
@ExperimentalIOSFeelV2Api
enum class IOSNavigationDirection {
    Forward,
    Backward,
    None
}

/**
 * Creates and remembers an [IOSNavigationTransitionState].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSNavigationTransitionState(initialProgress: Float = 0f): IOSNavigationTransitionState {
    return remember { IOSNavigationTransitionState(initialProgress) }
}
