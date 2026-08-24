package dev.iosfeel.motion.morph

/**
 * Normalizes a continuous progress (0f..1f) into a sub-interval [start]..[end].
 *
 * Example: For a fade-in that should happen only between 0.5f and 0.8f progress:
 * ```kotlin
 * val alpha = intervalProgress(progress, start = 0.5f, end = 0.8f)
 * ```
 */
fun intervalProgress(
    progress: Float,
    start: Float,
    end: Float
): Float {
    if (end <= start) return if (progress >= end) 1f else 0f
    val clamped = progress.coerceIn(start, end)
    return (clamped - start) / (end - start)
}
