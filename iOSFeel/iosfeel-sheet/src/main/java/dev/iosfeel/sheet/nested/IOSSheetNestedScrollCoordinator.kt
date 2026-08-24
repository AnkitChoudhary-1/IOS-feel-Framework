package dev.iosfeel.sheet.nested

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import dev.iosfeel.sheet.IOSSheetPhase
import dev.iosfeel.sheet.IOSSheetState
import dev.iosfeel.sheet.detent.IOSSheetDetent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * High-fidelity nested scroll coordinator delivering the seamless iOS Sheet <-> List interaction:
 *
 * 1. Upward drag from Medium expands sheet to Large; leftover delta scrolls list.
 * 2. Downward drag when list hits top collapses sheet toward Medium.
 * 3. Fling momentum transfers across the boundary with zero velocity loss.
 */
@ExperimentalIOSFeelV2Api
class IOSSheetNestedScrollCoordinator(
    val sheetState: IOSSheetState,
    private val scope: CoroutineScope
) : NestedScrollConnection {

    private val largeOffset: Float
        get() = sheetState.resolvedDetents.firstOrNull { it.key == IOSSheetDetent.Large }?.value ?: 0f

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val deltaY = available.y

        // Upward drag: if sheet is not yet at Large, sheet consumes upward drag first
        if (deltaY < 0f && sheetState.offset > largeOffset) {
            val newOffset = (sheetState.offset + deltaY).coerceAtLeast(largeOffset)
            val consumedY = newOffset - sheetState.offset
            sheetState.dragTo(newOffset, 0f)
            return Offset(0f, consumedY)
        }

        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        val deltaY = available.y

        // Downward drag: if child list has reached top (cannot consume more), sheet collapses
        if (deltaY > 0f && source == NestedScrollSource.UserInput) {
            val newOffset = sheetState.offset + deltaY
            sheetState.dragTo(newOffset, 0f)
            return Offset(0f, deltaY)
        }

        return Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val vy = available.y

        // Upward fling when sheet is not yet at Large: sheet consumes fling to spring to Large
        if (vy < 0f && sheetState.offset > largeOffset) {
            sheetState.release(velocityPxPerSec = vy)
            return Velocity(0f, vy)
        }

        return Velocity.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val vy = available.y

        // Downward fling reaching top boundary: sheet receives remaining velocity to collapse
        if (vy > 0f) {
            sheetState.release(velocityPxPerSec = vy)
            return Velocity(0f, vy)
        }

        return Velocity.Zero
    }
}

/**
 * Creates and remembers an [IOSSheetNestedScrollCoordinator].
 */
@Composable
@ExperimentalIOSFeelV2Api
fun rememberIOSSheetNestedScrollCoordinator(
    sheetState: IOSSheetState
): IOSSheetNestedScrollCoordinator {
    val scope = rememberCoroutineScope()
    return remember(sheetState, scope) {
        IOSSheetNestedScrollCoordinator(sheetState, scope)
    }
}
