package dev.iosfeel.interaction.pointer

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.unit.Velocity
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * A single timestamped pointer position sample.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSPointerSample(
    val position: Offset,
    val uptimeMillis: Long
)

/**
 * Low-level tracker for pointer gestures, positions, deltas, and velocity.
 */
@Stable
@ExperimentalIOSFeelV2Api
class IOSPointerState {

    /**
     * The position where the initial pointer down event occurred.
     */
    var downPosition: Offset = Offset.Unspecified
        internal set

    /**
     * Current pointer position.
     */
    var currentPosition: Offset = Offset.Unspecified
        internal set

    /**
     * Pointer position during previous frame.
     */
    var previousPosition: Offset = Offset.Unspecified
        internal set

    /**
     * Movement delta during the last frame.
     */
    var delta: Offset = Offset.Zero
        internal set

    /**
     * Instantaneous velocity vector in px/second.
     */
    var velocity: Velocity = Velocity.Zero
        internal set

    /**
     * Total elapsed time since touch down in milliseconds.
     */
    var elapsedMillis: Long = 0L
        internal set

    /**
     * Pointer device type (Touch, Mouse, Stylus).
     */
    var pointerType: PointerType = PointerType.Touch
        internal set

    private val samples = mutableListOf<IOSPointerSample>()

    /**
     * Cumulative translation from down position to current position.
     */
    val translation: Offset
        get() = if (downPosition != Offset.Unspecified && currentPosition != Offset.Unspecified) {
            currentPosition - downPosition
        } else {
            Offset.Zero
        }

    /**
     * Euclidean distance moved from down position.
     */
    val totalDistance: Float
        get() = sqrt(translation.x * translation.x + translation.y * translation.y)

    /**
     * Initializes tracking on pointer down.
     */
    fun onDown(position: Offset, uptimeMillis: Long, type: PointerType = PointerType.Touch) {
        downPosition = position
        currentPosition = position
        previousPosition = position
        delta = Offset.Zero
        velocity = Velocity.Zero
        elapsedMillis = 0L
        pointerType = type
        samples.clear()
        samples.add(IOSPointerSample(position, uptimeMillis))
    }

    /**
     * Updates tracking on pointer movement.
     */
    fun onMove(position: Offset, uptimeMillis: Long) {
        previousPosition = currentPosition
        currentPosition = position
        delta = position - previousPosition
        val firstSampleTime = samples.firstOrNull()?.uptimeMillis ?: uptimeMillis
        elapsedMillis = uptimeMillis - firstSampleTime

        samples.add(IOSPointerSample(position, uptimeMillis))
        // Keep last 100ms of samples for smooth velocity calculation
        while (samples.size > 2 && uptimeMillis - samples.first().uptimeMillis > 120L) {
            samples.removeAt(0)
        }

        computeVelocity()
    }

    /**
     * Finalizes tracking on pointer up.
     */
    fun onUp(uptimeMillis: Long) {
        val firstSampleTime = samples.firstOrNull()?.uptimeMillis ?: uptimeMillis
        elapsedMillis = uptimeMillis - firstSampleTime
        computeVelocity()
    }

    /**
     * Clears all pointer tracking data.
     */
    fun reset() {
        downPosition = Offset.Unspecified
        currentPosition = Offset.Unspecified
        previousPosition = Offset.Unspecified
        delta = Offset.Zero
        velocity = Velocity.Zero
        elapsedMillis = 0L
        samples.clear()
    }

    private fun computeVelocity() {
        if (samples.size < 2) {
            velocity = Velocity.Zero
            return
        }
        val first = samples.first()
        val last = samples.last()
        val dt = (last.uptimeMillis - first.uptimeMillis).coerceAtLeast(1L) / 1000f
        val vx = (last.position.x - first.position.x) / dt
        val vy = (last.position.y - first.position.y) / dt
        velocity = Velocity(vx, vy)
    }
}
