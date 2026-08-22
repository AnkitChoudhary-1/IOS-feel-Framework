package dev.iosfeel.scroll

import androidx.compose.runtime.MonotonicFrameClock
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IOSScrollInteractionStateTest {

    private class TestFrameClock : MonotonicFrameClock {
        private var time = 0L
        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            time += 16_000_000L
            return onFrame(time)
        }
    }

    @Test
    fun consumeOverscrollAppliesResistanceAndUpdatesPhase() {
        val state = IOSScrollInteractionState(IOSScrollConfig())
        assertEquals(0f, state.overscroll, 0.001f)
        assertEquals(IOSScrollPhase.Idle, state.phase)

        val consumed = state.consumeOverscroll(100f)
        assertTrue(consumed > 0f)
        assertEquals(IOSScrollPhase.Overscrolling, state.phase)
        assertTrue(state.overscroll > 0f)
    }

    @Test
    fun overscrollRecoveryConsumesOnlyOppositeDirection() {
        val state = IOSScrollInteractionState(IOSScrollConfig())
        state.consumeOverscroll(100f)
        val initialOverscroll = state.overscroll

        // Pulling in same direction should not recover
        val wrongDirection = state.consumeOverscrollRecovery(50f)
        assertEquals(0f, wrongDirection, 0.001f)
        assertEquals(initialOverscroll, state.overscroll, 0.001f)

        // Pulling back toward zero should recover
        val recovered = state.consumeOverscrollRecovery(-20f)
        assertEquals(-20f, recovered, 0.001f)
        assertEquals(initialOverscroll - 20f, state.overscroll, 0.001f)
    }

    @Test
    fun zeroDeltaReturnsZero() {
        val state = IOSScrollInteractionState(IOSScrollConfig())
        val consumed = state.consumeOverscroll(0f)
        assertEquals(0f, consumed, 0.001f)
    }

    @Test
    fun animateToZeroRestoresZeroAndIdlePhase() = runBlocking {
        withContext(TestFrameClock()) {
            val state = IOSScrollInteractionState(IOSScrollConfig())
            state.consumeOverscroll(120f)
            assertTrue(state.overscroll > 0f)
            assertEquals(IOSScrollPhase.Overscrolling, state.phase)

            state.animateToZero(0f)
            assertEquals(0f, state.overscroll, 0.001f)
            assertEquals(IOSScrollPhase.Idle, state.phase)
        }
    }

    @Test
    fun interruptCancelsAndSetsDragging() {
        val state = IOSScrollInteractionState(IOSScrollConfig())
        state.consumeOverscroll(120f)
        state.phase = IOSScrollPhase.SpringingBack

        state.interrupt()
        assertEquals(IOSScrollPhase.Dragging, state.phase)
    }
}
