package dev.iosfeel.components.toggle

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.detent.IOSDetent
import dev.iosfeel.physics.detent.IOSDetentResolver
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings

/**
 * Universal state managing interactive toggle / switch drag and animation physics.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSToggleState(
    initialChecked: Boolean = false,
    val springSpec: IOSSpringSpec = IOSSprings.Selection
) {
    internal val animatable = Animatable(if (initialChecked) 1f else 0f)

    val progress: Float
        get() = animatable.value

    var isDragging: Boolean by mutableStateOf(false)
        internal set

    var thumbScaleX: Float by mutableFloatStateOf(1f)
        internal set

    private val detents = listOf(
        IOSDetent(value = 0f, key = false),
        IOSDetent(value = 1f, key = true)
    )

    suspend fun snapTo(checked: Boolean) {
        animatable.snapTo(if (checked) 1f else 0f)
    }

    suspend fun animateTo(checked: Boolean, velocity: Float = 0f) {
        thumbScaleX = 1f
        animatable.animateTo(
            targetValue = if (checked) 1f else 0f,
            initialVelocity = velocity,
            animationSpec = SpringSpec(
                dampingRatio = springSpec.dampingRatio,
                stiffness = springSpec.stiffness
            )
        )
    }

    suspend fun dragTo(targetProgress: Float) {
        isDragging = true
        thumbScaleX = 1.15f
        animatable.snapTo(targetProgress.coerceIn(0f, 1f))
    }

    /**
     * Resolves final toggle state upon release using position + velocity.
     */
    suspend fun release(velocity: Float = 0f): Boolean {
        isDragging = false
        val decision = IOSDetentResolver.resolve(
            position = progress,
            velocity = velocity,
            detents = detents,
            velocityThreshold = 1.5f
        )
        val targetChecked = decision.target.key
        animateTo(targetChecked, velocity = velocity)
        return targetChecked
    }
}

/**
 * Creates and remembers an [IOSToggleState].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSToggleState(
    checked: Boolean = false,
    springSpec: IOSSpringSpec = IOSSprings.Selection
): IOSToggleState {
    return remember(springSpec) {
        IOSToggleState(checked, springSpec)
    }
}
