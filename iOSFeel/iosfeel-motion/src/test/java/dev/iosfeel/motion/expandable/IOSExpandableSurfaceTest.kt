package dev.iosfeel.motion.expandable

import androidx.compose.runtime.MonotonicFrameClock
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class IOSExpandableSurfaceTest {

    private class TestFrameClock : MonotonicFrameClock {
        private var time = 0L
        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            time += 16_000_000L
            return onFrame(time)
        }
    }

    @Test
    fun `expandable surface initializes in Collapsed phase`() {
        val state = IOSExpandableSurfaceState(initialExpanded = false)
        assertEquals(0f, state.progress, 0.001f)
        assertEquals(IOSExpandablePhase.Collapsed, state.phase)
        assertTrue(state.isCollapsed)
    }

    @Test
    fun `dragging surface updates progress and phase`() = runBlocking {
        val state = IOSExpandableSurfaceState(initialExpanded = false)
        state.dragTo(0.4f)
        assertEquals(0.4f, state.progress, 0.001f)
        assertEquals(IOSExpandablePhase.Dragging, state.phase)
    }

    @Test
    fun `velocity upward flick expands surface even below half progress`() = runBlocking {
        withContext(TestFrameClock()) {
            val state = IOSExpandableSurfaceState(initialExpanded = false)
            state.dragTo(0.3f)

            val expanded = state.release(velocity = 2.0f)
            assertTrue(expanded)
            assertEquals(1f, state.progress, 0.001f)
            assertEquals(IOSExpandablePhase.Expanded, state.phase)
        }
    }

    @Test
    fun `velocity downward flick collapses surface even above half progress`() = runBlocking {
        withContext(TestFrameClock()) {
            val state = IOSExpandableSurfaceState(initialExpanded = true)
            state.dragTo(0.7f)

            val expanded = state.release(velocity = -2.0f)
            assertFalse(expanded)
            assertEquals(0f, state.progress, 0.001f)
            assertEquals(IOSExpandablePhase.Collapsed, state.phase)
        }
    }
}
