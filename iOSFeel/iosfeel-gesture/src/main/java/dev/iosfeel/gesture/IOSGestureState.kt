package dev.iosfeel.gesture

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class IOSGestureState {

    var phase by mutableStateOf(
        IOSGesturePhase.Idle
    )
        internal set

    var translationX by mutableFloatStateOf(0f)
        internal set

    var translationY by mutableFloatStateOf(0f)
        internal set

    var velocityX by mutableFloatStateOf(0f)
        internal set

    var velocityY by mutableFloatStateOf(0f)
        internal set

    var progress by mutableFloatStateOf(0f)
        internal set

    fun reset() {
        phase = IOSGesturePhase.Idle

        translationX = 0f
        translationY = 0f

        velocityX = 0f
        velocityY = 0f

        progress = 0f
    }
}
