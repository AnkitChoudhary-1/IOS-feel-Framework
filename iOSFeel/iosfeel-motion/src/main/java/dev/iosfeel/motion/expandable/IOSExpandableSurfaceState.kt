package dev.iosfeel.motion.expandable

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.detent.IOSDetent
import dev.iosfeel.physics.detent.IOSDetentResolver
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings

/**
 * Universal state managing expandable surface progress, interactive dragging, re-grab, and velocity snapping.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSExpandableSurfaceState(
    initialExpanded: Boolean = false,
    val springSpec: IOSSpringSpec = IOSSprings.PlayerExpansion
) {
    internal val animatable = Animatable(if (initialExpanded) 1f else 0f)

    val progress: Float
        get() = animatable.value

    var phase: IOSExpandablePhase by mutableStateOf(
        if (initialExpanded) IOSExpandablePhase.Expanded else IOSExpandablePhase.Collapsed
    )
        internal set

    val isExpanded: Boolean
        get() = phase == IOSExpandablePhase.Expanded || (phase != IOSExpandablePhase.Collapsed && progress >= 0.5f)

    val isCollapsed: Boolean
        get() = phase == IOSExpandablePhase.Collapsed || progress <= 0.01f

    private val detents = listOf(
        IOSDetent(value = 0f, key = IOSExpandablePhase.Collapsed),
        IOSDetent(value = 1f, key = IOSExpandablePhase.Expanded)
    )

    suspend fun snapTo(targetProgress: Float) {
        animatable.snapTo(targetProgress.coerceIn(0f, 1f))
    }

    suspend fun dragTo(targetProgress: Float) {
        phase = IOSExpandablePhase.Dragging
        animatable.snapTo(targetProgress.coerceIn(0f, 1f))
    }

    suspend fun expand(velocity: Float = 0f) {
        phase = IOSExpandablePhase.Expanding
        animatable.animateTo(
            targetValue = 1f,
            initialVelocity = velocity,
            animationSpec = SpringSpec(
                dampingRatio = springSpec.dampingRatio,
                stiffness = springSpec.stiffness
            )
        )
        phase = IOSExpandablePhase.Expanded
    }

    suspend fun collapse(velocity: Float = 0f) {
        phase = IOSExpandablePhase.Collapsing
        animatable.animateTo(
            targetValue = 0f,
            initialVelocity = velocity,
            animationSpec = SpringSpec(
                dampingRatio = springSpec.dampingRatio,
                stiffness = springSpec.stiffness
            )
        )
        phase = IOSExpandablePhase.Collapsed
    }

    /**
     * Resolves final rest state using detent position + velocity threshold.
     */
    suspend fun release(velocity: Float = 0f): Boolean {
        phase = IOSExpandablePhase.Settling
        val decision = IOSDetentResolver.resolve(
            position = progress,
            velocity = velocity,
            detents = detents,
            velocityThreshold = 1.2f
        )
        val shouldExpand = decision.target.key == IOSExpandablePhase.Expanded
        if (shouldExpand) {
            expand(velocity)
        } else {
            collapse(velocity)
        }
        return shouldExpand
    }
}

/**
 * Creates and remembers an [IOSExpandableSurfaceState].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSExpandableSurfaceState(
    initialExpanded: Boolean = false,
    springSpec: IOSSpringSpec = IOSSprings.PlayerExpansion
): IOSExpandableSurfaceState {
    return remember(springSpec) {
        IOSExpandableSurfaceState(initialExpanded, springSpec)
    }
}
