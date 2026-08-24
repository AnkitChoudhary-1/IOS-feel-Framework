package dev.iosfeel.motion.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.iosfeel.motion.morph.IOSMorphBounds

/**
 * State tracking continuous shared element transition progress, coordinates, and lifecycle phase.
 */
@Stable
class IOSSharedElementState(
    val key: IOSSharedElementKey,
    initialProgress: Float = 0f
) {
    var progress: Float by mutableFloatStateOf(initialProgress)
        internal set

    var sourceBounds: IOSMorphBounds? by mutableStateOf(null)
        internal set

    var targetBounds: IOSMorphBounds? by mutableStateOf(null)
        internal set

    var phase: IOSSharedElementPhase by mutableStateOf(IOSSharedElementPhase.Idle)
        internal set

    val interpolatedBounds: IOSMorphBounds?
        get() {
            val src = sourceBounds ?: return null
            val tgt = targetBounds ?: return null
            return IOSMorphBounds.lerp(src, tgt, progress)
        }
}

/**
 * Creates and remembers an [IOSSharedElementState].
 */
@Composable
fun rememberIOSSharedElementState(
    key: IOSSharedElementKey,
    initialProgress: Float = 0f
): IOSSharedElementState {
    return remember(key) {
        IOSSharedElementState(key, initialProgress)
    }
}
