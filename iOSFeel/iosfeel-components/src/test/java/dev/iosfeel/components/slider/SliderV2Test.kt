package dev.iosfeel.components.slider

import androidx.compose.runtime.MonotonicFrameClock
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class SliderV2Test {

    private class TestFrameClock : MonotonicFrameClock {
        private var time = 0L
        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            time += 16_000_000L
            return onFrame(time)
        }
    }

    @Test
    fun `slider drag expands thumb scale`() = runBlocking {
        val state = IOSSliderState(initialNormalized = 0.2f)
        assertEquals(1f, state.thumbScale, 0.001f)

        state.dragTo(0.5f)
        assertTrue(state.isDragging)
        assertEquals(1.25f, state.thumbScale, 0.001f)
        assertEquals(0.5f, state.progress, 0.001f)
    }

    @Test
    fun `detent snapping snaps to nearest detent when close`() = runBlocking {
        val detents = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
        val state = IOSSliderState(initialNormalized = 0f, detents = detents)

        // Dragging to 0.26f snaps to 0.25f
        val snapped = state.dragTo(0.26f)
        assertEquals(0.25f, snapped, 0.001f)
    }

    @Test
    fun `release resets thumb scale and snaps to detent`() = runBlocking {
        withContext(TestFrameClock()) {
            val detents = listOf(0f, 0.5f, 1f)
            val state = IOSSliderState(initialNormalized = 0f, detents = detents)

            state.dragTo(0.48f)
            val finalVal = state.release()
            assertEquals(0.5f, finalVal, 0.001f)
            assertEquals(1f, state.thumbScale, 0.001f)
        }
    }
}
