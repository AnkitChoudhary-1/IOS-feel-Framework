package dev.iosfeel.gesture

data class IOSGestureConfig(
    val direction: IOSGestureDirection = IOSGestureDirection.Any,
    val activationSlopPx: Float = 12f,
    val progressDistancePx: Float = 300f,
    val requiredStartMaxX: Float? = null,
    val enabled: Boolean = true
)

internal fun calculateGestureProgress(
    translation: Float,
    distance: Float
): Float {
    if (distance <= 0f) {
        return 0f
    }

    return (translation / distance).coerceIn(0f, 1f)
}

internal fun shouldAcceptGesture(
    dx: Float,
    dy: Float,
    config: IOSGestureConfig
): Boolean {
    return when (config.direction) {
        IOSGestureDirection.Horizontal ->
            kotlin.math.abs(dx) > kotlin.math.abs(dy)

        IOSGestureDirection.Vertical ->
            kotlin.math.abs(dy) > kotlin.math.abs(dx)

        IOSGestureDirection.Any ->
            true
    }
}
