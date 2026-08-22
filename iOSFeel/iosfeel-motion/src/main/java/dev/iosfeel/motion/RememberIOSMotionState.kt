package dev.iosfeel.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberIOSMotionState(
    initialPosition: Float = 0f
): IOSMotionState {
    return remember {
        IOSMotionState(
            initialPosition = initialPosition
        )
    }
}
