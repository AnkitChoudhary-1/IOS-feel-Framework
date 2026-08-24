package dev.iosfeel.interaction.debug

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import dev.iosfeel.interaction.IOSInteractionPhase
import dev.iosfeel.interaction.gesture.IOSGestureState
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api

/**
 * Immutable debug telemetry snapshot of active gesture arena and interaction state.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSGestureSnapshot(
    val phase: IOSInteractionPhase,
    val pointerPosition: Offset,
    val translation: Offset,
    val velocity: Velocity,
    val winnerId: Any?,
    val candidateIds: Set<Any>,
    val timestampNs: Long = System.nanoTime()
)

/**
 * Debug snapshot of an individual gesture recognizer.
 */
@Immutable
@ExperimentalIOSFeelV2Api
data class IOSRecognizerSnapshot(
    val id: Any,
    val state: IOSGestureState,
    val priority: String,
    val compatibility: String
)
