package dev.iosfeel.gesture

import androidx.compose.ui.Modifier

data class IOSEdgeSwipeConfig(
    val edgeWidthPx: Float = 48f,
    val progressDistancePx: Float = 300f,
    val enabled: Boolean = true
)

fun Modifier.iosEdgeSwipe(
    state: IOSGestureState,
    edgeWidthPx: Float = 48f,
    progressDistancePx: Float = 300f,
    onStarted: (() -> Unit)? = null,
    onChanged: ((IOSGestureState) -> Unit)? = null,
    onEnded: ((IOSGestureState) -> Unit)? = null,
    onCancelled: (() -> Unit)? = null
): Modifier {
    return iosGesture(
        state = state,
        config = IOSGestureConfig(
            direction = IOSGestureDirection.Horizontal,
            progressDistancePx = progressDistancePx,
            requiredStartMaxX = edgeWidthPx
        ),
        onStarted = onStarted,
        onChanged = onChanged,
        onEnded = onEnded,
        onCancelled = onCancelled
    )
}
