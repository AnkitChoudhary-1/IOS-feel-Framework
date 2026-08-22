package dev.iosfeel.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Stable
class IOSMotionState(
    initialPosition: Float = 0f
) {

    private val operationMutex = Mutex()

    val position = Animatable(initialPosition)

    var phase by mutableStateOf(IOSMotionPhase.Idle)
        private set

    var velocity by mutableFloatStateOf(0f)
        private set

    var target by mutableFloatStateOf(initialPosition)
        private set

    val isAnimating: Boolean
        get() = position.isRunning

    suspend fun beginDrag() {
        /*
         * Important:
         * Don't hold the mutex while stop() waits for
         * an animation that may itself need state access.
         */
        val animationVelocity = position.velocity

        position.stop()

        operationMutex.withLock {
            velocity = animationVelocity
            phase = IOSMotionPhase.Dragging
        }
    }

    suspend fun dragBy(
        delta: Float,
        gestureVelocity: Float,
        bounds: IOSMotionBounds? = null
    ) {
        operationMutex.withLock {
            phase = IOSMotionPhase.Dragging
            velocity = gestureVelocity

            val proposedPosition = position.value + delta
            val constrainedPosition = bounds?.constrain(proposedPosition) ?: proposedPosition

            position.snapTo(constrainedPosition)
        }
    }

    suspend fun springTo(
        targetPosition: Float,
        initialVelocity: Float,
        spec: IOSSpringSpec
    ) {
        operationMutex.withLock {
            target = targetPosition
            velocity = initialVelocity
            phase = IOSMotionPhase.Springing
        }

        try {
            position.animateTo(
                targetValue = targetPosition,
                animationSpec = spring(
                    stiffness = spec.stiffness,
                    dampingRatio = spec.dampingRatio
                ),
                initialVelocity = initialVelocity
            ) {
                this@IOSMotionState.velocity = this.velocity
            }

            operationMutex.withLock {
                velocity = 0f
                phase = IOSMotionPhase.Idle
            }

        } catch (throwable: Throwable) {
            operationMutex.withLock {
                velocity = position.velocity
                phase = IOSMotionPhase.Cancelled
            }

            throw throwable
        }
    }

    suspend fun cancel() {
        val currentVelocity = position.velocity

        position.stop()

        operationMutex.withLock {
            velocity = currentVelocity
            phase = IOSMotionPhase.Cancelled
        }
    }

    suspend fun snapTo(
        value: Float
    ) {
        position.stop()

        operationMutex.withLock {
            position.snapTo(value)

            target = value
            velocity = 0f
            phase = IOSMotionPhase.Idle
        }
    }

    fun progressBetween(
        start: Float,
        end: Float
    ): Float {
        if (start == end) return 1f

        return (
            (position.value - start) /
                (end - start)
            ).coerceIn(0f, 1f)
    }
}
