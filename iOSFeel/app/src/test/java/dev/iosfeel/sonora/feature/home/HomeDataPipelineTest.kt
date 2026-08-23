package dev.iosfeel.sonora.feature.home

import dev.iosfeel.sonora.core.media.history.PlaybackHistoryTracker
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.MusicLibrary
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.core.model.dateAddedSeconds
import dev.iosfeel.sonora.core.model.findAlbum
import dev.iosfeel.sonora.core.model.findArtist
import dev.iosfeel.sonora.core.model.resolveSongs
import dev.iosfeel.sonora.core.repository.PlaybackHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class HomeDataPipelineTest {

    private val testSongs = listOf(
        Song(id = 1L, title = "Song A", artist = "Artist X", album = "Album 1", albumId = 101L, durationMs = 200_000L, dateAddedSeconds = 100L),
        Song(id = 2L, title = "Song B", artist = "Artist Y", album = "Album 2", albumId = 102L, durationMs = 180_000L, dateAddedSeconds = 300L),
        Song(id = 3L, title = "Song C", artist = "Artist X", album = "Album 1", albumId = 101L, durationMs = 240_000L, dateAddedSeconds = 200L)
    )

    private val testAlbums = listOf(
        Album(id = 101L, title = "Album 1", artist = "Artist X", songs = listOf(testSongs[0], testSongs[2])),
        Album(id = 102L, title = "Album 2", artist = "Artist Y", songs = listOf(testSongs[1]))
    )

    private val testArtists = listOf(
        Artist(id = 1L, name = "Artist X", albums = listOf(testAlbums[0]), songCount = 2),
        Artist(id = 2L, name = "Artist Y", albums = listOf(testAlbums[1]), songCount = 1)
    )

    private val testLibrary = MusicLibrary(
        songs = testSongs,
        albums = testAlbums,
        artists = testArtists
    )

    @Test
    fun resolveSongsPreservesOrderAndIgnoresMissing() {
        val requestedIds = listOf(2L, 999L, 1L)
        val resolved = testLibrary.resolveSongs(requestedIds)

        assertEquals(2, resolved.size)
        assertEquals(2L, resolved[0].id)
        assertEquals(1L, resolved[1].id)
    }

    @Test
    fun findAlbumReturnsCorrectAlbum() {
        val album = testLibrary.findAlbum(101L)
        assertNotNull(album)
        assertEquals("Album 1", album?.title)

        val missing = testLibrary.findAlbum(999L)
        assertNull(missing)
    }

    @Test
    fun findArtistReturnsCorrectArtist() {
        val artist = testLibrary.findArtist(1L)
        assertNotNull(artist)
        assertEquals("Artist X", artist?.name)

        val missing = testLibrary.findArtist(999L)
        assertNull(missing)
    }

    @Test
    fun albumDateAddedSecondsCalculatedCorrectly() {
        assertEquals(200L, testAlbums[0].dateAddedSeconds)
        assertEquals(300L, testAlbums[1].dateAddedSeconds)
    }

    @Test
    fun playbackHistoryTrackerEnforcesListeningThreshold() = runBlocking {
        var recordedRecentSongId: Long? = null
        var incrementedCountSongId: Long? = null

        val fakeHistoryRepo = object : PlaybackHistoryRepository {
            override fun observeRecentlyPlayedIds(limit: Int): Flow<List<Long>> = flowOf(emptyList())
            override fun observeMostPlayedIds(limit: Int): Flow<List<Long>> = flowOf(emptyList())
            override suspend fun recordPlayback(songId: Long) {
                recordedRecentSongId = songId
            }
            override suspend fun incrementPlayCount(songId: Long) {
                incrementedCountSongId = songId
            }
        }

        val tracker = PlaybackHistoryTracker(fakeHistoryRepo)

        // 1. Song starts: recorded immediately
        tracker.onSongStarted(testSongs[0])
        assertEquals(1L, recordedRecentSongId)
        assertEquals(null, incrementedCountSongId)

        // 2. Position < 30s and < 50% (duration is 200s, position is 15s)
        tracker.onPositionUpdated(songId = 1L, positionMs = 15_000L, durationMs = 200_000L)
        assertEquals(null, incrementedCountSongId)

        // 3. Position >= 30s (duration is 200s, position is 31s)
        tracker.onPositionUpdated(songId = 1L, positionMs = 31_000L, durationMs = 200_000L)
        assertEquals(1L, incrementedCountSongId)

        // 4. Repeated update does not double-increment
        incrementedCountSongId = null
        tracker.onPositionUpdated(songId = 1L, positionMs = 45_000L, durationMs = 200_000L)
        assertEquals(null, incrementedCountSongId)
    }
}
