package dev.iosfeel.components.expandable

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import dev.iosfeel.motion.IOSMotionBounds
import dev.iosfeel.motion.IOSMotionPhase
import dev.iosfeel.motion.IOSMotionState

@Stable
class IOSExpandableSurfaceState(
    initialProgress: Float = 0f,
    private val config: IOSExpandableSurfaceConfig = IOSExpandableSurfaceConfig()
) {
    val motion = IOSMotionState(initialPosition = initialProgress.coerceIn(0f, 1f))

    private val progressBounds = IOSMotionBounds(min = 0f, max = 1f)

    val progress: Float
        get() = motion.position.value.coerceIn(0f, 1f)

    val phase: IOSMotionPhase
        get() = motion.phase

    val velocity: Float
        get() = motion.velocity

    val isExpanded: Boolean by derivedStateOf {
        progress >= 0.99f
    }

    val isCollapsed: Boolean by derivedStateOf {
        progress <= 0.01f
    }

    suspend fun beginDrag() {
        motion.beginDrag()
    }

    suspend fun dragBy(deltaProgress: Float, velocity: Float) {
        motion.dragBy(
            delta = deltaProgress,
            gestureVelocity = velocity,
            bounds = progressBounds
        )
    }

    suspend fun settle(velocity: Float) {
        val target = decideTarget(
            progress = progress,
            velocity = velocity,
            config = config
        )
        motion.springTo(
            targetPosition = target,
            initialVelocity = velocity,
            spec = config.springSpec
        )
    }

    suspend fun expand(initialVelocity: Float = 0f) {
        motion.springTo(
            targetPosition = 1f,
            initialVelocity = initialVelocity,
            spec = config.springSpec
        )
    }

    suspend fun collapse(initialVelocity: Float = 0f) {
        motion.springTo(
            targetPosition = 0f,
            initialVelocity = initialVelocity,
            spec = config.springSpec
        )
    }

    suspend fun setProgress(value: Float) {
        motion.snapTo(value.coerceIn(0f, 1f))
    }

    companion object {
        fun decideTarget(
            progress: Float,
            velocity: Float,
            config: IOSExpandableSurfaceConfig
        ): Float {
            if (velocity >= config.velocityThreshold) {
                return 1f
            }
            if (velocity <= -config.velocityThreshold) {
                return 0f
            }
            return if (progress >= config.expansionThreshold) {
                1f
            } else {
                0f
            }
        }
    }
}
