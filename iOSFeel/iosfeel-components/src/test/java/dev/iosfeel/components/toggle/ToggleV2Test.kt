package dev.iosfeel.components.toggle

import androidx.compose.runtime.MonotonicFrameClock
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class ToggleV2Test {

    private class TestFrameClock : MonotonicFrameClock {
        private var time = 0L
        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            time += 16_000_000L
            return onFrame(time)
        }
    }

    @Test
    fun `toggle initializes at correct progress`() {
        val stateOff = IOSToggleState(initialChecked = false)
        assertEquals(0f, stateOff.progress, 0.001f)

        val stateOn = IOSToggleState(initialChecked = true)
        assertEquals(1f, stateOn.progress, 0.001f)
    }

    @Test
    fun `dragging toggle updates progress and thumb stretch`() = runBlocking {
        val state = IOSToggleState(initialChecked = false)
        state.dragTo(0.45f)
        assertTrue(state.isDragging)
        assertEquals(0.45f, state.progress, 0.001f)
        assertEquals(1.15f, state.thumbScaleX, 0.001f)
    }

    @Test
    fun `velocity decision selects On even if progress below half`() = runBlocking {
        withContext(TestFrameClock()) {
            val state = IOSToggleState(initialChecked = false)
            state.dragTo(0.35f)

            // Strong rightward flick (+2.5) -> commits to True (On)
            val result = state.release(velocity = 2.5f)
            assertTrue(result)
            assertEquals(1f, state.progress, 0.001f)
            assertFalse(state.isDragging)
        }
    }

    @Test
    fun `velocity decision selects Off even if progress above half`() = runBlocking {
        withContext(TestFrameClock()) {
            val state = IOSToggleState(initialChecked = true)
            state.dragTo(0.65f)

            // Strong leftward flick (-2.5) -> commits to False (Off)
            val result = state.release(velocity = -2.5f)
            assertFalse(result)
            assertEquals(0f, state.progress, 0.001f)
            assertFalse(state.isDragging)
        }
    }
}
