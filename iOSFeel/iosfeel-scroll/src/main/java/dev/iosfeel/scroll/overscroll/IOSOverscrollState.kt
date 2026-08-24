package dev.iosfeel.scroll.overscroll

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.interruption.IOSInterruptibleMotion
import dev.iosfeel.physics.interruption.IOSMotionOwner
import dev.iosfeel.physics.resistance.IOSResistanceSpec
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings
import dev.iosfeel.scroll.IOSScrollConfig
import dev.iosfeel.scroll.IOSScrollPhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/**
 * Observable elastic overscroll state driven by [IOSInterruptibleMotion].
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSOverscrollState(
    val config: IOSScrollConfig = IOSScrollConfig()
) {
    /**
     * Interruptible motion tracking elastic overscroll offset in pixels.
     */
    val motion = IOSInterruptibleMotion(initialValue = 0f)

    /**
     * Current visual overscroll displacement in pixels.
     */
    val overscrollOffset: Float
        get() = motion.state.value

    /**
     * Active scroll lifecycle phase.
     */
    var phase: IOSScrollPhase by mutableStateOf(IOSScrollPhase.Idle)
        internal set

    private var rawPullDistance: Float = 0f

    /**
     * Applies drag delta with progressive nonlinear resistance.
     */
    fun applyPullDelta(delta: Float) {
        if (phase != IOSScrollPhase.Overscrolling) {
            motion.acquireByUser()
            phase = IOSScrollPhase.Overscrolling
            rawPullDistance = 0f
        }

        rawPullDistance += delta
        val sign = sign(rawPullDistance)
        val resisted = config.resistance.apply(abs(rawPullDistance)) * sign

        motion.dragTo(
            value = resisted,
            velocity = 0f
        )
    }

    /**
     * Springs the overscroll displacement back to zero on release.
     */
    suspend fun release(initialVelocity: Float = motion.state.velocity) {
        phase = IOSScrollPhase.Returning
        rawPullDistance = 0f

        motion.releaseToSpring(
            target = 0f,
            initialVelocity = initialVelocity,
            spec = config.returnSpring
        )

        phase = IOSScrollPhase.Idle
    }

    /**
     * Cancels any active spring and reclaims user ownership on re-touch.
     */
    fun acquire() {
        motion.acquireByUser()
        rawPullDistance = motion.state.value
        phase = IOSScrollPhase.Dragging
    }
}

/**
 * Compose OverscrollEffect implementing iOS elastic rubber-banding.
 */
@ExperimentalIOSFeelV2Api
class IOSOverscrollEffect(
    val state: IOSOverscrollState,
    private val scope: CoroutineScope
) : OverscrollEffect {

    override val isInProgress: Boolean
        get() = state.overscrollOffset != 0f || state.phase != IOSScrollPhase.Idle

    override val node: androidx.compose.ui.Modifier.Node
        get() = object : androidx.compose.ui.Modifier.Node() {}

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset
    ): Offset {
        // First let the content scroll normally
        val consumedByScroll = performScroll(delta)
        val unconsumed = delta - consumedByScroll

        // If unconsumed delta exists during drag, apply rubber-band overscroll
        if (source == NestedScrollSource.UserInput && abs(unconsumed.y) > 0f) {
            state.applyPullDelta(unconsumed.y)
            return delta
        }

        return consumedByScroll
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity
    ) {
        val remainingVelocity = performFling(velocity)

        // Return overscroll spring to zero
        if (state.overscrollOffset != 0f) {
            state.release(initialVelocity = remainingVelocity.y)
        }
    }
}

/**
 * Creates and remembers an [IOSOverscrollEffect] and [IOSOverscrollState].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSOverscrollEffect(
    config: IOSScrollConfig = IOSScrollConfig()
): IOSOverscrollEffect {
    val state = remember(config) { IOSOverscrollState(config) }
    val scope = rememberCoroutineScope()
    return remember(state, scope) { IOSOverscrollEffect(state, scope) }
}
