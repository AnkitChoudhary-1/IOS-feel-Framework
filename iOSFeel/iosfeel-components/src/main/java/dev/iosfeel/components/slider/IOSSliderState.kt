package dev.iosfeel.components.slider

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
 * Behavior mode for value updates during interactive drag.
 */
enum class IOSSliderBehavior {
    /**
     * Value commits immediately on every drag frame.
     */
    Immediate,

    /**
     * Value updates local preview during drag, and only commits on touch release (ideal for media scrubbers).
     */
    DeferredCommit
}

/**
 * Universal slider state managing normalized progress, detent snapping, and press expansion.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSSliderState(
    initialNormalized: Float = 0f,
    val detents: List<Float> = emptyList(),
    val springSpec: IOSSpringSpec = IOSSprings.Selection
) {
    internal val animatable = Animatable(initialNormalized)

    val progress: Float
        get() = animatable.value

    var isDragging: Boolean by mutableStateOf(false)
        internal set

    var lastSnappedDetent: Float? by mutableStateOf(null)
        internal set

    var thumbScale: Float by mutableFloatStateOf(1f)
        internal set

    suspend fun dragTo(targetNormalized: Float): Float {
        isDragging = true
        thumbScale = 1.25f

        val clamped = targetNormalized.coerceIn(0f, 1f)
        val finalVal = if (detents.isNotEmpty()) {
            val resolvedDetents = detents.map { IOSDetent(it, it) }
            val decision = IOSDetentResolver.resolve(clamped, 0f, resolvedDetents)
            val snapped = decision.target.value
            if (kotlin.math.abs(clamped - snapped) < 0.05f) snapped else clamped
        } else {
            clamped
        }

        animatable.snapTo(finalVal)
        return finalVal
    }

    suspend fun release(velocity: Float = 0f): Float {
        isDragging = false
        thumbScale = 1.0f

        val finalVal = if (detents.isNotEmpty()) {
            val resolvedDetents = detents.map { IOSDetent(it, it) }
            val decision = IOSDetentResolver.resolve(progress, velocity, resolvedDetents)
            decision.target.value
        } else {
            progress
        }

        animatable.animateTo(
            targetValue = finalVal,
            animationSpec = SpringSpec(
                dampingRatio = springSpec.dampingRatio,
                stiffness = springSpec.stiffness
            )
        )
        return finalVal
    }

    suspend fun snapTo(targetNormalized: Float) {
        animatable.snapTo(targetNormalized.coerceIn(0f, 1f))
    }
}

/**
 * Remembers an [IOSSliderState].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSSliderState(
    initialNormalized: Float = 0f,
    detents: List<Float> = emptyList(),
    springSpec: IOSSpringSpec = IOSSprings.Selection
): IOSSliderState {
    return remember(detents, springSpec) {
        IOSSliderState(initialNormalized, detents, springSpec)
    }
}

fun normalizeSliderValue(
    value: Float,
    range: ClosedFloatingPointRange<Float>
): Float {
    val distance = range.endInclusive - range.start
    if (distance == 0f) return 0f
    return ((value - range.start) / distance).coerceIn(0f, 1f)
}

fun denormalizeSliderValue(
    normalized: Float,
    range: ClosedFloatingPointRange<Float>
): Float {
    val distance = range.endInclusive - range.start
    return (range.start + normalized.coerceIn(0f, 1f) * distance).coerceIn(range.start, range.endInclusive)
}

fun snapToStep(
    normalized: Float,
    steps: Int
): Float {
    if (steps <= 0) return normalized
    val stepCount = steps + 1
    val stepSize = 1f / stepCount
    val stepIndex = kotlin.math.round(normalized / stepSize)
    return (stepIndex * stepSize).coerceIn(0f, 1f)
}
