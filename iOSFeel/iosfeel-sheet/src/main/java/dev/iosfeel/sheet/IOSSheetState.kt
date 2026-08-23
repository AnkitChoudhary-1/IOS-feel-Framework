package dev.iosfeel.sheet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import dev.iosfeel.motion.IOSMotionPreset
import dev.iosfeel.motion.IOSSpringSpec
import kotlinx.coroutines.CancellationException

enum class IOSSheetPhase {
    Idle,
    Dragging,
    Settling
}

@Stable
class IOSSheetState internal constructor(
    initialDetent: IOSSheetDetent = IOSSheetDetent.Medium,
    initialVisible: Boolean = false
) {
    val offset = Animatable(0f)

    var currentDetent by mutableStateOf(initialDetent)
        internal set

    var targetDetent by mutableStateOf(initialDetent)
        internal set

    var phase by mutableStateOf(IOSSheetPhase.Idle)
        internal set

    var velocity by mutableFloatStateOf(0f)
        internal set

    var visible by mutableStateOf(initialVisible)
        internal set

    internal var resolved: List<IOSResolvedDetent> = emptyList()
    internal var containerHeightPx: Float = 2400f

    val isDragging: Boolean
        get() = phase == IOSSheetPhase.Dragging

    val isSettling: Boolean
        get() = phase == IOSSheetPhase.Settling

    suspend fun beginDrag() {
        velocity = offset.velocity
        offset.stop()
        phase = IOSSheetPhase.Dragging
    }

    suspend fun dragTo(newOffset: Float) {
        phase = IOSSheetPhase.Dragging
        offset.snapTo(newOffset)
    }

    suspend fun dragBy(
        deltaY: Float,
        minOffset: Float,
        maxOffset: Float,
        gestureVelocity: Float
    ) {
        phase = IOSSheetPhase.Dragging
        velocity = gestureVelocity
        val next = (offset.value + deltaY).coerceIn(minOffset, maxOffset)
        offset.snapTo(next)
    }

    suspend fun settleTo(
        target: IOSResolvedDetent,
        initialVelocity: Float = 0f,
        springSpec: IOSSpringSpec = IOSMotionPreset.Smooth
    ) {
        targetDetent = target.detent
        phase = IOSSheetPhase.Settling
        velocity = initialVelocity
        visible = true

        try {
            offset.animateTo(
                targetValue = target.offsetPx,
                initialVelocity = initialVelocity,
                animationSpec = spring(
                    stiffness = springSpec.stiffness,
                    dampingRatio = springSpec.dampingRatio
                )
            ) {
                this@IOSSheetState.velocity = this.velocity
            }
            currentDetent = target.detent
            velocity = 0f
            phase = IOSSheetPhase.Idle
        } catch (cancellation: CancellationException) {
            phase = IOSSheetPhase.Idle
            throw cancellation
        }
    }

    suspend fun animateTo(
        detent: IOSSheetDetent,
        initialVelocity: Float = 0f,
        springSpec: IOSSpringSpec = IOSMotionPreset.Smooth
    ) {
        val target = resolved.firstOrNull { it.detent.id == detent.id }
            ?: findResolvedDetent(detent, resolved)
            ?: return
        settleTo(target, initialVelocity, springSpec)
    }

    suspend fun expand(springSpec: IOSSpringSpec = IOSMotionPreset.Smooth) {
        visible = true
        resolved.firstOrNull()?.let {
            settleTo(it, 0f, springSpec)
        }
    }

    suspend fun collapse(springSpec: IOSSpringSpec = IOSMotionPreset.Smooth) {
        resolved.lastOrNull()?.let {
            settleTo(it, 0f, springSpec)
        }
    }

    suspend fun dismiss(
        containerHeightPx: Float = this.containerHeightPx,
        springSpec: IOSSpringSpec = IOSMotionPreset.Snappy
    ) {
        phase = IOSSheetPhase.Settling
        try {
            offset.animateTo(
                targetValue = containerHeightPx,
                initialVelocity = velocity,
                animationSpec = spring(
                    stiffness = springSpec.stiffness,
                    dampingRatio = springSpec.dampingRatio
                )
            ) {
                this@IOSSheetState.velocity = this.velocity
            }
            visible = false
            velocity = 0f
            phase = IOSSheetPhase.Idle
        } catch (cancellation: CancellationException) {
            phase = IOSSheetPhase.Idle
            throw cancellation
        }
    }

    suspend fun hide(springSpec: IOSSpringSpec = IOSMotionPreset.Snappy) {
        dismiss(containerHeightPx, springSpec)
    }

    suspend fun show(
        initialDetent: IOSSheetDetent = currentDetent,
        springSpec: IOSSpringSpec = IOSMotionPreset.Smooth
    ) {
        visible = true
        if (offset.value <= 0f || offset.value >= containerHeightPx * 0.95f) {
            offset.snapTo(containerHeightPx)
        }
        animateTo(initialDetent, 0f, springSpec)
    }

    suspend fun interrupt() {
        val current = offset.value
        offset.stop()
        offset.snapTo(current)
        phase = IOSSheetPhase.Dragging
    }

    suspend fun snapTo(newOffset: Float) {
        offset.stop()
        offset.snapTo(newOffset)
        phase = IOSSheetPhase.Idle
    }
}

val IOSSheetStateSaver = Saver<IOSSheetState, List<Any>>(
    save = { listOf(it.currentDetent.id, it.visible) },
    restore = { list ->
        val id = list.getOrNull(0) as? String ?: "medium"
        val isVisible = list.getOrNull(1) as? Boolean ?: false
        val detent = when (id) {
            "compact" -> IOSSheetDetent.Compact
            "medium" -> IOSSheetDetent.Medium
            "large" -> IOSSheetDetent.Large
            else -> IOSSheetDetent.Medium
        }
        IOSSheetState(initialDetent = detent, initialVisible = isVisible)
    }
)

@Composable
fun rememberIOSSheetState(
    initialDetent: IOSSheetDetent = IOSSheetDetent.Medium,
    initialVisible: Boolean = false
): IOSSheetState {
    return rememberSaveable(saver = IOSSheetStateSaver) {
        IOSSheetState(initialDetent = initialDetent, initialVisible = initialVisible)
    }
}
