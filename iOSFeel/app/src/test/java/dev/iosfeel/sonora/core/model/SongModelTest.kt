package dev.iosfeel.sonora.core.model

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SongModelTest {

    @Test
    fun durationFormatted_formatsCorrectly() {
        val song1 = Song(
            id = 1,
            title = "Midnight City",
            artist = "M83",
            album = "Hurry Up, We're Dreaming",
            durationMs = 244000L // 4:04
        )

        assertEquals("4:04", song1.durationFormatted)

        val song2 = Song(
            id = 2,
            title = "Intro",
            artist = "The xx",
            album = "xx",
            durationMs = 127000L // 2:07
        )

        assertEquals("2:07", song2.durationFormatted)
    }

    @Test
    fun playbackState_progressFractionCalculatesCorrectly() {
        val state = PlaybackState(
            positionMs = 60000L,
            durationMs = 120000L
        )

        assertEquals(0.5f, state.progressFraction, 0.001f)
    }
}
