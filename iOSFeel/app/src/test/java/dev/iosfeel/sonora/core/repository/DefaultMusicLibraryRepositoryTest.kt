package dev.iosfeel.sonora.core.repository

import dev.iosfeel.sonora.core.media.MusicLibrarySource
import dev.iosfeel.sonora.core.media.cleanMetadata
import dev.iosfeel.sonora.core.media.normalizeTrackNumber
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.core.model.SongSort
import dev.iosfeel.sonora.core.model.SortDirection
import dev.iosfeel.sonora.core.model.formatDuration
import dev.iosfeel.sonora.core.model.sorted
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultMusicLibraryRepositoryTest {

    private fun fakeSong(
        id: Long,
        title: String = "Track $id",
        artist: String = "Artist 1",
        artistId: Long? = 10L,
        album: String? = "Album 1",
        albumId: Long? = 100L,
        durationMs: Long = 180000L,
        trackNumber: Int? = 1,
        year: Int? = 2024,
        dateAddedSeconds: Long = 1000L + id
    ): Song {
        return Song(
            id = id,
            title = title,
            artistId = artistId,
            artist = artist,
            albumId = albumId,
            album = album,
            durationMs = durationMs,
            trackNumber = trackNumber,
            year = year,
            dateAddedSeconds = dateAddedSeconds
        )
    }

    @Test
    fun songsWithSameAlbumIdCreateOneAlbum() {
        val songs = listOf(
            fakeSong(id = 1, albumId = 100, trackNumber = 2, title = "Song B"),
            fakeSong(id = 2, albumId = 100, trackNumber = 1, title = "Song A")
        )

        val repository = DefaultMusicLibraryRepository(
            source = object : MusicLibrarySource {
                override suspend fun getSongs(): List<Song> = songs
            }
        )

        val albums = repository.buildAlbums(songs)

        assertEquals(1, albums.size)
        assertEquals(2, albums.first().songCount)
        assertEquals("Song A", albums.first().songs.first().title)
    }

    @Test
    fun albumsWithSameArtistIdCreateOneArtist() {
        val songs = listOf(
            fakeSong(id = 1, albumId = 100, artistId = 50, artist = "Daft Punk"),
            fakeSong(id = 2, albumId = 101, artistId = 50, artist = "Daft Punk")
        )

        val repository = DefaultMusicLibraryRepository(
            source = object : MusicLibrarySource {
                override suspend fun getSongs(): List<Song> = songs
            }
        )

        val albums = repository.buildAlbums(songs)
        val artists = repository.buildArtists(albums)

        assertEquals(1, artists.size)
        assertEquals("Daft Punk", artists.first().name)
        assertEquals(2, artists.first().albumCount)
        assertEquals(2, artists.first().songCount)
    }

    @Test
    fun loadLibrary_returnsCompleteAggregate() = runBlocking {
        val songs = listOf(
            fakeSong(id = 1, title = "Starboy", artist = "The Weeknd", artistId = 1, album = "Starboy", albumId = 10),
            fakeSong(id = 2, title = "Blinding Lights", artist = "The Weeknd", artistId = 1, album = "After Hours", albumId = 20)
        )

        val repository = DefaultMusicLibraryRepository(
            source = object : MusicLibrarySource {
                override suspend fun getSongs(): List<Song> = songs
            }
        )

        val library = repository.loadLibrary()

        assertEquals(2, library.songs.size)
        assertEquals(2, library.albums.size)
        assertEquals(1, library.artists.size)
    }

    @Test
    fun cleanMetadata_replacesNullAndUnknownTags() {
        assertEquals("Unknown Song", null.cleanMetadata("Unknown Song"))
        assertEquals("Unknown Song", "".cleanMetadata("Unknown Song"))
        assertEquals("Unknown Song", "   ".cleanMetadata("Unknown Song"))
        assertEquals("Unknown Artist", "<unknown>".cleanMetadata("Unknown Artist"))
        assertEquals("Valid Title", "Valid Title".cleanMetadata("Fallback"))
    }

    @Test
    fun normalizeTrackNumber_handlesDiscsAndZeros() {
        assertEquals(0, 0.normalizeTrackNumber())
        assertEquals(1, 1.normalizeTrackNumber())
        assertEquals(5, 1005.normalizeTrackNumber()) // Disc 1, Track 5 -> 5
        assertEquals(12, 2012.normalizeTrackNumber()) // Disc 2, Track 12 -> 12
    }

    @Test
    fun durationFormat_formatsSecondsAndMinutes() {
        assertEquals("3:13", 193000L.formatDuration())
        assertEquals("6:01", 361000L.formatDuration())
        assertEquals("0:00", 0L.formatDuration())
    }

    @Test
    fun songSorting_sortsCorrectly() {
        val songs = listOf(
            fakeSong(id = 1, title = "Bravo", artist = "Charlie", durationMs = 200000L),
            fakeSong(id = 2, title = "Alpha", artist = "Delta", durationMs = 100000L)
        )

        val sortedByTitle = songs.sorted(SongSort.Title, SortDirection.Ascending)
        assertEquals("Alpha", sortedByTitle[0].title)
        assertEquals("Bravo", sortedByTitle[1].title)

        val sortedByTitleDesc = songs.sorted(SongSort.Title, SortDirection.Descending)
        assertEquals("Bravo", sortedByTitleDesc[0].title)
        assertEquals("Alpha", sortedByTitleDesc[1].title)

        val sortedByDuration = songs.sorted(SongSort.Duration, SortDirection.Ascending)
        assertEquals(100000L, sortedByDuration[0].durationMs)
    }
}
