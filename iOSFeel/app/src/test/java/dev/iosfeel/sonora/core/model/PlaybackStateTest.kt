package dev.iosfeel.sonora.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackStateTest {

    @Test
    fun defaultPlaybackStateHasNoActiveMedia() {
        val state = PlaybackState()
        assertFalse(state.hasActiveMedia)
        assertEquals(0f, state.progress, 0.001f)
    }

    @Test
    fun progressFractionCalculatesCorrectly() {
        val song = Song(
            id = 1L,
            title = "Song",
            artist = "Artist",
            album = "Album",
            durationMs = 100_000L
        )

        val state = PlaybackState(
            currentSong = song,
            positionMs = 50_000L,
            durationMs = 100_000L
        )

        assertTrue(state.hasActiveMedia)
        assertEquals(0.5f, state.progress, 0.001f)
    }

    @Test
    fun progressFractionClampsBounds() {
        val song = Song(
            id = 1L,
            title = "Song",
            artist = "Artist",
            album = "Album",
            durationMs = 100_000L
        )

        val stateOver = PlaybackState(
            currentSong = song,
            positionMs = 150_000L,
            durationMs = 100_000L
        )
        assertEquals(1.0f, stateOver.progress, 0.001f)

        val stateZeroDuration = PlaybackState(
            currentSong = song,
            positionMs = 10_000L,
            durationMs = 0L
        )
        assertEquals(0.0f, stateZeroDuration.progress, 0.001f)
    }
}
