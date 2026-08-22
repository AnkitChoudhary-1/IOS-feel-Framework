package dev.iosfeel.scroll

import kotlin.math.abs
import kotlin.math.pow

fun calculateIOSResistanceMultiplier(
    overscroll: Float,
    config: IOSScrollConfig
): Float {
    val fraction = (abs(overscroll) / config.maxOverscrollPx).coerceIn(0f, 1f)

    return (config.resistanceFactor * (1f - fraction.pow(config.resistanceExponent))).coerceAtLeast(0.05f)
}

fun applyIOSScrollResistance(
    currentOverscroll: Float,
    delta: Float,
    config: IOSScrollConfig
): Float {
    if (delta == 0f) {
        return currentOverscroll
    }

    val multiplier = calculateIOSResistanceMultiplier(
        overscroll = currentOverscroll,
        config = config
    )

    return (currentOverscroll + delta * multiplier).coerceIn(
        -config.maxOverscrollPx,
        config.maxOverscrollPx
    )
}
