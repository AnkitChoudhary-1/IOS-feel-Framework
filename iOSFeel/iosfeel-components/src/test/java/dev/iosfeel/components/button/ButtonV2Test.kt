package dev.iosfeel.components.button

import androidx.compose.runtime.MonotonicFrameClock
import dev.iosfeel.components.interaction.IOSPressSurfaceState
import dev.iosfeel.interaction.IOSInteractionPhase
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class ButtonV2Test {

    private class TestFrameClock : MonotonicFrameClock {
        private var time = 0L
        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            time += 16_000_000L
            return onFrame(time)
        }
    }

    @Test
    fun `press surface starts idle at 0 progress`() {
        val state = IOSPressSurfaceState()
        assertEquals(0f, state.progress, 0.001f)
        assertEquals(IOSInteractionPhase.Idle, state.phase)
    }

    @Test
    fun `press transitions phase to Pressed`() = runBlocking {
        withContext(TestFrameClock()) {
            val state = IOSPressSurfaceState()
            state.press()
            assertTrue(state.isPressed)
            assertEquals(1f, state.progress, 0.001f)
        }
    }

    @Test
    fun `release transitions phase back to Idle and 0 progress`() = runBlocking {
        withContext(TestFrameClock()) {
            val state = IOSPressSurfaceState()
            state.press()
            state.release()
            assertEquals(0f, state.progress, 0.001f)
            assertEquals(IOSInteractionPhase.Idle, state.phase)
        }
    }

    @Test
    fun `cancel smoothly resets progress`() = runBlocking {
        withContext(TestFrameClock()) {
            val state = IOSPressSurfaceState()
            state.press()
            state.cancel()
            assertEquals(0f, state.progress, 0.001f)
            assertEquals(IOSInteractionPhase.Idle, state.phase)
        }
    }
}
