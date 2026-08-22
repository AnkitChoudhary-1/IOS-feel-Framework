package dev.iosfeel.components.interaction

import androidx.compose.runtime.Immutable
import dev.iosfeel.core.tokens.IOSMotionTokens

@Immutable
data class IOSPressConfig(
    val pressedScale: Float = 0.975f,
    val pressedAlpha: Float = 1.0f,
    val pressStiffness: Float = IOSMotionTokens.PressStiffness,
    val pressDampingRatio: Float = IOSMotionTokens.PressDampingRatio,
    val releaseStiffness: Float = IOSMotionTokens.ReleaseStiffness,
    val releaseDampingRatio: Float = IOSMotionTokens.ReleaseDampingRatio
) {
    init {
        require(pressedScale in 0.5f..1.5f) { "pressedScale must be within reasonable bounds" }
        require(pressedAlpha in 0f..1f) { "pressedAlpha must be within 0..1" }
    }
}

fun calculateIOSPressScale(
    progress: Float,
    pressedScale: Float
): Float {
    val p = progress.coerceIn(0f, 1f)
    return 1f + (pressedScale - 1f) * p
}

fun calculateIOSPressAlpha(
    progress: Float,
    pressedAlpha: Float
): Float {
    val p = progress.coerceIn(0f, 1f)
    return 1f + (pressedAlpha - 1f) * p
}
