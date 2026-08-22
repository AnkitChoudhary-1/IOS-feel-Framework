package dev.iosfeel.gesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberIOSGestureState(): IOSGestureState {
    return remember {
        IOSGestureState()
    }
}
