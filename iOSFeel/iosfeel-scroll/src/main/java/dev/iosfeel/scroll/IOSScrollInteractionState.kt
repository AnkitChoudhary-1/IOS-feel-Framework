package dev.iosfeel.scroll

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Stable
class IOSScrollInteractionState(
    private val config: IOSScrollConfig = IOSScrollConfig(),
    private var internalScope: CoroutineScope? = null
) {
    private val overscrollAnimation = Animatable(0f)
    private var springJob: Job? = null

    var overscroll by mutableFloatStateOf(0f)
        internal set

    var phase by mutableStateOf(IOSScrollPhase.Idle)
        internal set

    val animatedOverscroll: Float
        get() = overscrollAnimation.value

    fun updateScope(scope: CoroutineScope) {
        internalScope = scope
    }

    fun consumeOverscroll(delta: Float): Float {
        if (delta == 0f) {
            return 0f
        }

        interrupt()
        phase = IOSScrollPhase.Overscrolling
        val previous = overscroll

        overscroll = applyIOSScrollResistance(
            currentOverscroll = overscroll,
            delta = delta,
            config = config
        )

        return overscroll - previous
    }

    fun consumeOverscrollRecovery(delta: Float): Float {
        if (overscroll == 0f || delta == 0f) {
            return 0f
        }

        /*
         * Only consume input that moves the stretch back toward zero.
         */
        val recovering = (overscroll > 0f && delta < 0f) || (overscroll < 0f && delta > 0f)

        if (!recovering) {
            return 0f
        }

        val previous = overscroll
        val newValue = when {
            overscroll > 0f -> (overscroll + delta).coerceAtLeast(0f)
            else -> (overscroll + delta).coerceAtMost(0f)
        }

        overscroll = newValue
        if (overscroll == 0f) {
            phase = IOSScrollPhase.Dragging
        }

        return newValue - previous
    }

    suspend fun syncAnimationToDrag() {
        springJob?.cancel()
        overscrollAnimation.stop()
        overscrollAnimation.snapTo(overscroll)
    }

    fun releaseOverscroll(velocityY: Float = 0f, scope: CoroutineScope? = internalScope) {
        if (overscroll == 0f) {
            phase = IOSScrollPhase.Idle
            return
        }

        if (scope != null) {
            springJob?.cancel()
            springJob = scope.launch {
                animateToZero(velocityY)
            }
        }
    }

    suspend fun animateToZero(velocityY: Float = 0f) {
        if (overscroll == 0f) {
            phase = IOSScrollPhase.Idle
            return
        }

        phase = IOSScrollPhase.SpringingBack

        try {
            overscrollAnimation.stop()
            overscrollAnimation.snapTo(overscroll)

            overscrollAnimation.animateTo(
                targetValue = 0f,
                initialVelocity = velocityY,
                animationSpec = spring(
                    stiffness = config.springStiffness,
                    dampingRatio = config.springDampingRatio
                )
            ) {
                overscroll = value
            }
        } finally {
            overscroll = 0f
            phase = IOSScrollPhase.Idle
        }
    }

    fun interrupt() {
        springJob?.cancel()
        springJob = null
        if (phase == IOSScrollPhase.SpringingBack) {
            overscroll = overscrollAnimation.value
            phase = IOSScrollPhase.Dragging
        }
    }
}

@Composable
fun rememberIOSScrollInteractionState(
    config: IOSScrollConfig = IOSScrollConfig(),
    scope: CoroutineScope = rememberCoroutineScope()
): IOSScrollInteractionState {
    return remember(config) {
        IOSScrollInteractionState(config = config, internalScope = scope)
    }.also {
        it.updateScope(scope)
    }
}
