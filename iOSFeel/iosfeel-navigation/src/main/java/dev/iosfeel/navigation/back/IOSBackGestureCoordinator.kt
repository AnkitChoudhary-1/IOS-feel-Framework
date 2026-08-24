package dev.iosfeel.navigation.back

import dev.iosfeel.navigation.transition.IOSNavigationDirection
import dev.iosfeel.navigation.transition.IOSNavigationTransitionSource
import dev.iosfeel.navigation.transition.IOSNavigationTransitionState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.interruption.IOSMotionOwner
import dev.iosfeel.physics.spring.IOSSprings

/**
 * Coordinates interactive edge-swipe and predictive back gestures with physics-based settlement.
 */
@ExperimentalIOSFeelV2Api
object IOSBackGestureCoordinator {

    /**
     * Evaluates whether an interactive back gesture should commit (1f) or cancel (0f)
     * based on both displacement progress and release velocity.
     */
    fun decideBackTarget(
        progress: Float,
        velocity: Float,
        threshold: Float = 0.45f,
        velocityThreshold: Float = 1.1f
    ): Float {
        // High forward velocity -> always commit pop even from low progress
        if (velocity >= velocityThreshold) {
            return 1f
        }
        // High backward velocity -> always cancel even from high progress
        if (velocity <= -velocityThreshold) {
            return 0f
        }
        // Neutral velocity -> evaluate positional threshold
        return if (progress >= threshold) 1f else 0f
    }

    /**
     * Starts an interactive edge back gesture, claiming motion ownership.
     */
    fun startBackGesture(transition: IOSNavigationTransitionState, source: IOSNavigationTransitionSource = IOSNavigationTransitionSource.EdgeGesture) {
        transition.source = source
        transition.direction = IOSNavigationDirection.Backward
        transition.motion.acquireByUser()
    }

    /**
     * Updates back gesture progress with current drag position and normalized velocity.
     */
    fun updateBackGesture(
        transition: IOSNavigationTransitionState,
        dragDistancePx: Float,
        containerWidthPx: Float,
        normalizedVelocity: Float = 0f
    ) {
        val width = containerWidthPx.coerceAtLeast(1f)
        val rawProgress = (dragDistancePx / width).coerceIn(0f, 1f)
        transition.motion.dragTo(value = rawProgress, velocity = normalizedVelocity)
    }

    /**
     * Releases active back gesture into spring animation to either 0f (cancel) or 1f (pop).
     */
    suspend fun releaseBackGesture(
        transition: IOSNavigationTransitionState,
        containerWidthPx: Float,
        releaseVelocity: Float,
        onPopComplete: (() -> Unit)? = null
    ): Float {
        val target = decideBackTarget(
            progress = transition.progress,
            velocity = releaseVelocity
        )

        val spec = if (target == 1f) IOSSprings.NavigationPop else IOSSprings.NavigationCancel

        transition.animateTo(
            target = target,
            velocity = releaseVelocity,
            spec = spec
        )

        if (target == 1f) {
            onPopComplete?.invoke()
        }

        return target
    }
}
