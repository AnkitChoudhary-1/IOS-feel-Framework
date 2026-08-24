package dev.iosfeel.components.segmented

import androidx.compose.runtime.MonotonicFrameClock
import dev.iosfeel.physics.ExperimentalIOSFeelV2Api
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalIOSFeelV2Api::class)
class SegmentedV2Test {

    private class TestFrameClock : MonotonicFrameClock {
        private var time = 0L
        override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
            time += 16_000_000L
            return onFrame(time)
        }
    }

    private val items = listOf("Songs", "Albums", "Artists")

    @Test
    fun `segmented state initializes to correct index`() {
        val state = IOSSegmentedState(items = items, initialSelected = "Albums")
        assertEquals(1, state.selectedIndex)
        assertEquals(1f, state.indexProgress, 0.001f)
    }

    @Test
    fun `select animates to targeted item index`() = runBlocking {
        withContext(TestFrameClock()) {
            val state = IOSSegmentedState(items = items, initialSelected = "Songs")
            state.select("Artists")
            assertEquals(2, state.selectedIndex)
            assertEquals(2f, state.indexProgress, 0.001f)
        }
    }

    @Test
    fun `scrubbing updates candidate index continuous progress`() = runBlocking {
        val state = IOSSegmentedState(items = items, initialSelected = "Songs")
        state.scrubTo(1.2f)
        assertTrue(state.isScrubbing)
        assertEquals(1.2f, state.indexProgress, 0.001f)
        assertEquals(1, state.candidateIndex) // Closest to 1 (Albums)
    }

    @Test
    fun `release commits candidate item`() = runBlocking {
        withContext(TestFrameClock()) {
            val state = IOSSegmentedState(items = items, initialSelected = "Songs")
            state.scrubTo(1.8f)
            val selected = state.release()
            assertEquals("Artists", selected)
            assertEquals(2, state.selectedIndex)
            assertFalse(state.isScrubbing)
        }
    }
}
