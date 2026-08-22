package dev.iosfeel.navigation

data class IOSNavigationTransform(
    val currentTranslationFraction: Float,
    val previousTranslationFraction: Float,
    val shadowAlpha: Float
)

data class IOSPushTransform(
    val previousTranslationFraction: Float,
    val currentTranslationFraction: Float
)

fun calculateIOSBackTransform(
    progress: Float
): IOSNavigationTransform {
    val p = progress.coerceIn(0f, 1f)

    return IOSNavigationTransform(
        currentTranslationFraction = p,
        previousTranslationFraction = -0.25f + (0.25f * p),
        shadowAlpha = 1f - p
    )
}

fun calculateIOSPushTransform(
    progress: Float
): IOSPushTransform {
    val p = progress.coerceIn(0f, 1f)

    return IOSPushTransform(
        previousTranslationFraction = -0.25f * p,
        currentTranslationFraction = 1f - p
    )
}

fun normalizeGestureVelocity(
    velocityPxPerSecond: Float,
    distancePx: Float
): Float {
    if (distancePx <= 0f) {
        return 0f
    }

    return velocityPxPerSecond / distancePx
}

fun mapRegrabProgress(
    startProgress: Float,
    gestureProgress: Float
): Float {
    val start = startProgress.coerceIn(0f, 1f)
    val gesture = gestureProgress.coerceIn(0f, 1f)

    return start + gesture * (1f - start)
}
