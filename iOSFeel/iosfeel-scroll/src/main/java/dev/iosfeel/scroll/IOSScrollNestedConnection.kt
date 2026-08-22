package dev.iosfeel.scroll

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope

class IOSScrollNestedConnection(
    private val state: IOSScrollInteractionState,
    private val coroutineScope: CoroutineScope? = null
) : NestedScrollConnection {

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (state.phase == IOSScrollPhase.SpringingBack && source == NestedScrollSource.UserInput) {
            state.interrupt()
        }

        /*
         * If an existing elastic displacement is being pulled back toward zero,
         * consume that first in pre-scroll pass.
         */
        val consumedY = state.consumeOverscrollRecovery(available.y)

        return Offset(x = 0f, y = consumedY)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (available.y == 0f) {
            return Offset.Zero
        }

        /*
         * Child couldn't consume this movement (hit list boundary).
         * Elastic overscroll begins here.
         */
        val consumedY = state.consumeOverscroll(available.y)

        return Offset(x = 0f, y = consumedY)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (state.overscroll != 0f) {
            state.releaseOverscroll(velocityY = available.y, scope = coroutineScope)
            return available
        }
        return Velocity.Zero
    }

    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity
    ): Velocity {
        if (state.overscroll != 0f || available.y != 0f) {
            state.releaseOverscroll(velocityY = available.y, scope = coroutineScope)
        }

        return Velocity.Zero
    }
}
