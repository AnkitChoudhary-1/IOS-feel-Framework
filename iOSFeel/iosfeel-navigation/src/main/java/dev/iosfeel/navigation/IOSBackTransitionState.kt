package dev.iosfeel.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.iosfeel.motion.IOSSpringSpec
import kotlinx.coroutines.CancellationException

enum class IOSBackTransitionPhase {
    Idle,
    Interactive,
    Completing,
    Cancelling
}

@Stable
class IOSBackTransitionState {

    val progress = Animatable(0f)

    var interactiveProgress by mutableFloatStateOf(0f)
        private set

    var phase by mutableStateOf(
        IOSBackTransitionPhase.Idle
    )
        private set

    var velocity by mutableFloatStateOf(0f)
        private set

    val isInteractive: Boolean
        get() = phase == IOSBackTransitionPhase.Interactive

    val currentProgress: Float
        get() = if (isInteractive) interactiveProgress else progress.value

    companion object {
        val DefaultCompleteSpec = IOSSpringSpec(
            stiffness = 380f,
            dampingRatio = 1.0f
        )

        val DefaultCancelSpec = IOSSpringSpec(
            stiffness = 360f,
            dampingRatio = 1.0f
        )
    }

    fun beginInteractive(startValue: Float = progress.value) {
        interactiveProgress = startValue
        phase = IOSBackTransitionPhase.Interactive
    }

    fun updateInteractive(
        progressValue: Float,
        progressVelocity: Float
    ) {
        phase = IOSBackTransitionPhase.Interactive
        velocity = progressVelocity
        interactiveProgress = progressValue.coerceIn(0f, 1f)
    }

    suspend fun complete(
        initialVelocity: Float,
        spec: IOSSpringSpec = DefaultCompleteSpec
    ) {
        val startVal = if (isInteractive) interactiveProgress else progress.value
        progress.snapTo(startVal)
        phase = IOSBackTransitionPhase.Completing
        velocity = initialVelocity

        try {
            progress.animateTo(
                targetValue = 1f,
                initialVelocity = initialVelocity.coerceIn(-8f, 8f),
                animationSpec = spring(
                    stiffness = spec.stiffness,
                    dampingRatio = spec.dampingRatio
                )
            ) {
                this@IOSBackTransitionState.velocity = this.velocity
            }
        } catch (cancellation: CancellationException) {
            phase = IOSBackTransitionPhase.Idle
            throw cancellation
        } finally {
            phase = IOSBackTransitionPhase.Idle
        }
    }

    suspend fun cancel(
        initialVelocity: Float,
        spec: IOSSpringSpec = DefaultCancelSpec
    ) {
        val startVal = if (isInteractive) interactiveProgress else progress.value
        progress.snapTo(startVal)
        phase = IOSBackTransitionPhase.Cancelling
        velocity = initialVelocity

        try {
            progress.animateTo(
                targetValue = 0f,
                initialVelocity = initialVelocity.coerceIn(-8f, 8f),
                animationSpec = spring(
                    stiffness = spec.stiffness,
                    dampingRatio = spec.dampingRatio
                )
            ) {
                this@IOSBackTransitionState.velocity = this.velocity
            }
        } catch (cancellation: CancellationException) {
            phase = IOSBackTransitionPhase.Idle
            throw cancellation
        } finally {
            phase = IOSBackTransitionPhase.Idle
            velocity = 0f
        }
    }

    suspend fun reset() {
        progress.stop()
        progress.snapTo(0f)
        interactiveProgress = 0f
        velocity = 0f
        phase = IOSBackTransitionPhase.Idle
    }
}
