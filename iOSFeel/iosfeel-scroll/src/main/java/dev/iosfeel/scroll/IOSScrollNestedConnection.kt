package dev.iosfeel.scroll

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope

class IOSScrollNestedConnection(
    private val state: IOSScrollInteractionState,
    private val coroutineScope: CoroutineScope? = null,
    private val orientation: IOSScrollOrientation = IOSScrollOrientation.Vertical
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
        return when (orientation) {
            IOSScrollOrientation.Vertical -> {
                val consumedY = state.consumeOverscrollRecovery(available.y)
                Offset(x = 0f, y = consumedY)
            }
            IOSScrollOrientation.Horizontal -> {
                val consumedX = state.consumeOverscrollRecovery(available.x)
                Offset(x = consumedX, y = 0f)
            }
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        return when (orientation) {
            IOSScrollOrientation.Vertical -> {
                if (available.y == 0f) return Offset.Zero
                val consumedY = state.consumeOverscroll(available.y)
                Offset(x = 0f, y = consumedY)
            }
            IOSScrollOrientation.Horizontal -> {
                if (available.x == 0f) return Offset.Zero
                val consumedX = state.consumeOverscroll(available.x)
                Offset(x = consumedX, y = 0f)
            }
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (state.overscroll != 0f) {
            val velocity = when (orientation) {
                IOSScrollOrientation.Vertical -> available.y
                IOSScrollOrientation.Horizontal -> available.x
            }
            state.releaseOverscroll(velocityY = velocity, scope = coroutineScope)
            return available
        }
        return Velocity.Zero
    }

    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity
    ): Velocity {
        val velocity = when (orientation) {
            IOSScrollOrientation.Vertical -> available.y
            IOSScrollOrientation.Horizontal -> available.x
        }
        if (state.overscroll != 0f || velocity != 0f) {
            state.releaseOverscroll(velocityY = velocity, scope = coroutineScope)
        }

        return Velocity.Zero
    }
}
