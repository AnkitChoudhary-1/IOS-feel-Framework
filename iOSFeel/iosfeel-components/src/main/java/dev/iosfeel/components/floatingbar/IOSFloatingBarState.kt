package dev.iosfeel.components.floatingbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.iosfeel.core.tokens.IOSMotionTokens

@Stable
class IOSFloatingBarState(
    initialProgress: Float = 0f
) {
    private val _progress = Animatable(initialProgress.coerceIn(0f, 1f))

    val progress: Float
        get() = _progress.value

    val isExpanded: Boolean by derivedStateOf {
        progress <= 0.05f
    }

    val isCompact: Boolean by derivedStateOf {
        progress >= 0.95f
    }

    suspend fun expand() {
        _progress.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                stiffness = IOSMotionTokens.TabBarStiffness,
                dampingRatio = IOSMotionTokens.TabBarDampingRatio
            )
        )
    }

    suspend fun minimize() {
        _progress.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                stiffness = IOSMotionTokens.TabBarStiffness,
                dampingRatio = IOSMotionTokens.TabBarDampingRatio
            )
        )
    }

    suspend fun snapTo(value: Float) {
        _progress.snapTo(value.coerceIn(0f, 1f))
    }

    suspend fun onScrollDelta(deltaY: Float, sensitivity: Float = 300f) {
        val current = _progress.value
        val delta = -deltaY / sensitivity
        val newTarget = (current + delta).coerceIn(0f, 1f)
        _progress.snapTo(newTarget)
    }
}

@Composable
fun rememberIOSFloatingBarState(
    initialProgress: Float = 0f
): IOSFloatingBarState {
    return remember {
        IOSFloatingBarState(initialProgress = initialProgress)
    }
}
