package dev.iosfeel.sheet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.physics.detent.IOSDetent
import dev.iosfeel.physics.interruption.IOSInterruptibleMotion
import dev.iosfeel.physics.spring.IOSSpringSpec
import dev.iosfeel.physics.spring.IOSSprings
import dev.iosfeel.sheet.detent.IOSSheetDetent
import dev.iosfeel.sheet.detent.IOSSheetDetentResolver

/**
 * Universal sheet interaction lifecycle phases in iOSFeel V2.
 */
@ExperimentalIOSFeelV2Api
enum class IOSSheetPhase {
    /**
     * Sheet is completely collapsed off-screen.
     */
    Hidden,

    /**
     * Sheet is at rest at its current semantic detent.
     */
    Idle,

    /**
     * User is directly dragging the sheet surface.
     */
    Dragging,

    /**
     * Sheet is actively springing into a target detent.
     */
    Settling,

    /**
     * Sheet is animating off-screen toward complete dismissal.
     */
    Dismissing
}

/**
 * Observable bottom sheet state in iOSFeel V2 powered by [IOSInterruptibleMotion].
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSSheetState(
    initialDetent: IOSSheetDetent = IOSSheetDetent.Hidden,
    val detents: List<IOSSheetDetent> = listOf(IOSSheetDetent.Medium, IOSSheetDetent.Large)
) {
    /**
     * Container height in pixels (updated on layout).
     */
    var containerHeightPx: Float by mutableFloatStateOf(1000f)
        internal set

    /**
     * Content height in pixels.
     */
    var contentHeightPx: Float by mutableFloatStateOf(0f)
        internal set

    /**
     * Underlying interruptible motion tracking top sheet edge offset in pixels.
     * Offset coordinate: 0px = top of container, containerHeightPx = hidden at bottom.
     */
    val motion = IOSInterruptibleMotion(initialValue = containerHeightPx)

    /**
     * Current visual offset in pixels from container top.
     */
    val offset: Float
        get() = motion.state.value

    /**
     * Instantaneous sheet velocity in pixels per second.
     */
    val velocity: Float
        get() = motion.state.velocity

    /**
     * Active semantic detent.
     */
    var currentDetent: IOSSheetDetent by mutableStateOf(initialDetent)
        internal set

    /**
     * Target semantic detent currently being settled into.
     */
    var targetDetent: IOSSheetDetent by mutableStateOf(initialDetent)
        internal set

    /**
     * Current sheet lifecycle phase.
     */
    var phase: IOSSheetPhase by mutableStateOf(if (initialDetent == IOSSheetDetent.Hidden) IOSSheetPhase.Hidden else IOSSheetPhase.Idle)
        internal set

    /**
     * Returns true if sheet is visible on screen.
     */
    val isVisible: Boolean
        get() = phase != IOSSheetPhase.Hidden

    /**
     * Normalized expansion progress [0f, 1f] (0f = hidden, 1f = fully expanded to Large).
     */
    val expansionProgress: Float
        get() {
            val totalSpan = containerHeightPx.coerceAtLeast(1f)
            return (1f - (offset / totalSpan)).coerceIn(0f, 1f)
        }

    /**
     * Resolves detent list into physical pixel coordinates.
     */
    val resolvedDetents: List<IOSDetent<IOSSheetDetent>>
        get() = IOSSheetDetentResolver.resolveDetents(
            detents = detents + IOSSheetDetent.Hidden,
            containerHeightPx = containerHeightPx,
            contentHeightPx = contentHeightPx
        )

    /**
     * Claims user ownership on touch initiation.
     */
    fun acquireByUser() {
        motion.acquireByUser()
        phase = IOSSheetPhase.Dragging
    }

    /**
     * Updates sheet offset during interactive drag.
     */
    fun dragTo(offsetPx: Float, velocityPxPerSec: Float = 0f) {
        val minOffset = resolvedDetents.firstOrNull { it.key != IOSSheetDetent.Hidden }?.value ?: 0f
        val maxOffset = containerHeightPx
        val clamped = offsetPx.coerceIn(minOffset * 0.5f, maxOffset)
        motion.dragTo(value = clamped, velocity = velocityPxPerSec)
    }

    /**
     * Resolves target detent and releases sheet to spring settlement.
     */
    suspend fun release(velocityPxPerSec: Float = this.velocity, spec: IOSSpringSpec = IOSSprings.Sheet) {
        val target = IOSSheetDetentResolver.resolveTarget(
            offsetPx = offset,
            velocityPxPerSec = velocityPxPerSec,
            resolvedDetents = resolvedDetents
        )

        targetDetent = target.key
        phase = if (targetDetent == IOSSheetDetent.Hidden) IOSSheetPhase.Dismissing else IOSSheetPhase.Settling

        motion.releaseToSpring(
            target = target.value,
            initialVelocity = velocityPxPerSec,
            spec = if (targetDetent == IOSSheetDetent.Hidden) IOSSprings.SheetDismiss else spec
        )

        currentDetent = targetDetent
        phase = if (currentDetent == IOSSheetDetent.Hidden) IOSSheetPhase.Hidden else IOSSheetPhase.Idle
    }

    val value: Float
        get() = offset

    val visible: Boolean
        get() = isVisible

    val isSettling: Boolean
        get() = phase == IOSSheetPhase.Settling || phase == IOSSheetPhase.Dismissing

    fun beginDrag() {
        acquireByUser()
    }

    suspend fun settleTo(
        detent: IOSSheetDetent,
        velocity: Float = this.velocity
    ) {
        animateTo(detent, velocity = velocity)
    }

    suspend fun show(
        detent: IOSSheetDetent = IOSSheetDetent.Medium,
        velocity: Float = 0f
    ) {
        animateTo(detent, velocity = velocity)
    }

    suspend fun expand(velocity: Float = this.velocity) {
        animateTo(IOSSheetDetent.Large, velocity = velocity)
    }

    suspend fun collapse(velocity: Float = this.velocity) {
        val target = detents.firstOrNull { it != IOSSheetDetent.Large && it != IOSSheetDetent.Hidden } ?: IOSSheetDetent.Medium
        animateTo(target, velocity = velocity)
    }

    /**
     * Smoothly animates sheet to a semantic detent.
     */
    suspend fun animateTo(
        detent: IOSSheetDetent,
        velocity: Float = this.velocity,
        spec: IOSSpringSpec = IOSSprings.Sheet
    ) {
        val resolved = resolvedDetents.find { it.key == detent } ?: return
        targetDetent = detent
        phase = if (detent == IOSSheetDetent.Hidden) IOSSheetPhase.Dismissing else IOSSheetPhase.Settling

        motion.releaseToSpring(
            target = resolved.value,
            initialVelocity = velocity,
            spec = if (detent == IOSSheetDetent.Hidden) IOSSprings.SheetDismiss else spec
        )

        currentDetent = detent
        phase = if (detent == IOSSheetDetent.Hidden) IOSSheetPhase.Hidden else IOSSheetPhase.Idle
    }

    /**
     * Dismisses sheet off-screen.
     */
    suspend fun dismiss(velocity: Float = this.velocity) {
        animateTo(IOSSheetDetent.Hidden, velocity = velocity, spec = IOSSprings.SheetDismiss)
    }

    /**
     * Snaps sheet directly to detent without animation.
     */
    fun snapTo(detent: IOSSheetDetent) {
        val resolved = resolvedDetents.find { it.key == detent } ?: return
        currentDetent = detent
        targetDetent = detent
        motion.state.update(value = resolved.value, velocity = 0f, target = resolved.value)
        phase = if (detent == IOSSheetDetent.Hidden) IOSSheetPhase.Hidden else IOSSheetPhase.Idle
    }
}

/**
 * Saver for [IOSSheetState] to persist across configuration changes and process death.
 */
@OptIn(ExperimentalIOSFeelV2Api::class)
val IOSSheetStateSaver: Saver<IOSSheetState, Any> = listSaver(
    save = { listOf(it.currentDetent.id, it.isVisible) },
    restore = { list ->
        val id = list[0] as String
        val isVisible = list[1] as Boolean
        val detent = when (id) {
            "large" -> IOSSheetDetent.Large
            "medium" -> IOSSheetDetent.Medium
            "compact" -> IOSSheetDetent.Compact
            "content" -> IOSSheetDetent.Content
            else -> if (isVisible) IOSSheetDetent.Medium else IOSSheetDetent.Hidden
        }
        IOSSheetState(
            initialDetent = if (isVisible) detent else IOSSheetDetent.Hidden
        )
    }
)

/**
 * Creates and remembers an [IOSSheetState].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSSheetState(
    initialDetent: IOSSheetDetent = IOSSheetDetent.Hidden,
    detents: List<IOSSheetDetent> = listOf(IOSSheetDetent.Medium, IOSSheetDetent.Large)
): IOSSheetState {
    return rememberSaveable(saver = IOSSheetStateSaver) {
        IOSSheetState(initialDetent, detents)
    }
}
