package dev.iosfeel.scroll

import kotlin.math.exp

data class IOSScrollResult(
    val consumed: Float,
    val unconsumed: Float
)

fun consumeIOSScrollDelta(
    state: IOSScrollState,
    delta: Float,
    config: IOSScrollConfig
): IOSScrollResult {
    if (delta == 0f) {
        return IOSScrollResult(
            consumed = 0f,
            unconsumed = 0f
        )
    }

    val proposed = state.position - delta

    /*
     * Inside normal scroll bounds.
     */
    if (proposed >= 0f && proposed <= state.maxScroll) {
        state.position = proposed
        state.overscroll = 0f
        state.phase = IOSScrollPhase.Dragging

        return IOSScrollResult(
            consumed = delta,
            unconsumed = 0f
        )
    }

    /*
     * Reached top boundary.
     */
    if (proposed < 0f) {
        state.position = 0f
        state.phase = IOSScrollPhase.Overscrolling

        state.overscroll = applyIOSScrollResistance(
            currentOverscroll = state.overscroll,
            delta = delta,
            config = config
        )

        return IOSScrollResult(
            consumed = delta,
            unconsumed = 0f
        )
    }

    /*
     * Reached bottom boundary.
     */
    state.position = state.maxScroll
    state.phase = IOSScrollPhase.Overscrolling

    state.overscroll = applyIOSScrollResistance(
        currentOverscroll = state.overscroll,
        delta = delta,
        config = config
    )

    return IOSScrollResult(
        consumed = delta,
        unconsumed = 0f
    )
}

fun calculateDeceleratedVelocity(
    velocity: Float,
    deltaSeconds: Float,
    decelerationRate: Float = 3f
): Float {
    if (deltaSeconds <= 0f) {
        return velocity
    }

    return velocity * exp(-decelerationRate * deltaSeconds)
}

fun calculateFrameDisplacement(
    velocity: Float,
    deltaSeconds: Float
): Float {
    return velocity * deltaSeconds
}
