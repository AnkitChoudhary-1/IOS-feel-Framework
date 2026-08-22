package dev.iosfeel.components.slider

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
