package dev.iosfeel.components.segmented

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.SpringSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.detent.IOSDetent
import dev.iosfeel.physics.detent.IOSDetentResolver
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings

/**
 * Universal state managing segmented control selection pill animation and scrub physics.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSSegmentedState<T>(
    val items: List<T>,
    initialSelected: T,
    val springSpec: IOSSpringSpec = IOSSprings.Selection
) {
    private val initialIndex = items.indexOf(initialSelected).coerceAtLeast(0)
    internal val animatable = Animatable(initialIndex.toFloat())

    val indexProgress: Float
        get() = animatable.value

    var selectedIndex: Int by mutableIntStateOf(initialIndex)
        internal set

    var isScrubbing: Boolean by mutableStateOf(false)
        internal set

    var candidateIndex: Int by mutableIntStateOf(initialIndex)
        internal set

    private val detents: List<IOSDetent<Int>>
        get() = items.indices.map { IOSDetent(value = it.toFloat(), key = it) }

    suspend fun select(item: T, velocity: Float = 0f) {
        val idx = items.indexOf(item)
        if (idx >= 0) {
            selectedIndex = idx
            candidateIndex = idx
            animatable.animateTo(
                targetValue = idx.toFloat(),
                initialVelocity = velocity,
                animationSpec = SpringSpec(
                    dampingRatio = springSpec.dampingRatio,
                    stiffness = springSpec.stiffness
                )
            )
        }
    }

    suspend fun scrubTo(progress: Float): Int {
        isScrubbing = true
        val clamped = progress.coerceIn(0f, (items.size - 1).toFloat())
        animatable.snapTo(clamped)

        val decision = IOSDetentResolver.resolve(
            position = clamped,
            velocity = 0f,
            detents = detents
        )
        candidateIndex = decision.target.key
        return candidateIndex
    }

    suspend fun release(velocity: Float = 0f): T {
        isScrubbing = false
        val decision = IOSDetentResolver.resolve(
            position = indexProgress,
            velocity = velocity,
            detents = detents,
            velocityThreshold = 1.0f
        )
        val finalIdx = decision.target.key
        selectedIndex = finalIdx
        candidateIndex = finalIdx
        animatable.animateTo(
            targetValue = finalIdx.toFloat(),
            initialVelocity = velocity,
            animationSpec = SpringSpec(
                dampingRatio = springSpec.dampingRatio,
                stiffness = springSpec.stiffness
            )
        )
        return items[finalIdx]
    }
}

/**
 * Creates and remembers an [IOSSegmentedState].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun <T> rememberIOSSegmentedState(
    items: List<T>,
    selected: T,
    springSpec: IOSSpringSpec = IOSSprings.Selection
): IOSSegmentedState<T> {
    return remember(items, springSpec) {
        IOSSegmentedState(items, selected, springSpec)
    }
}
