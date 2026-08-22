package dev.iosfeel.scroll

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Stable
class IOSScrollState {

    var phase by mutableStateOf(
        IOSScrollPhase.Idle
    )
        internal set

    var position by mutableFloatStateOf(0f)
        internal set

    var velocity by mutableFloatStateOf(0f)
        internal set

    var overscroll by mutableFloatStateOf(0f)
        internal set

    var maxScroll by mutableFloatStateOf(0f)
        internal set

    val isOverscrolled: Boolean
        get() = overscroll != 0f

    val canScrollBackward: Boolean
        get() = position > 0f

    val canScrollForward: Boolean
        get() = position < maxScroll

    fun reset() {
        position = 0f
        overscroll = 0f
        velocity = 0f
        phase = IOSScrollPhase.Idle
    }

    fun scrollTo(newPosition: Float) {
        position = newPosition.coerceIn(0f, maxScroll)
        overscroll = 0f
        velocity = 0f
        phase = IOSScrollPhase.Idle
    }
}
