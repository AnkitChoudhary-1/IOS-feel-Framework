package dev.iosfeel.sonora.feature.search

import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.MusicLibrary
import dev.iosfeel.sonora.core.model.Playlist
import dev.iosfeel.sonora.core.model.Song
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchEngineTest {

    private val sampleSongs = listOf(
        Song(id = 1, title = "Starboy", artist = "The Weeknd", album = "Starboy", durationMs = 230000),
        Song(id = 2, title = "Blinding Lights", artist = "The Weeknd", album = "After Hours", durationMs = 200000),
        Song(id = 3, title = "Starlight", artist = "Muse", album = "Black Holes and Revelations", durationMs = 240000),
        Song(id = 4, title = "Levitating", artist = "Dua Lipa", album = "Future Nostalgia", durationMs = 203000)
    )

    private val sampleAlbums = listOf(
        Album(id = 1, title = "Starboy", artist = "The Weeknd", songs = listOf(sampleSongs[0])),
        Album(id = 2, title = "After Hours", artist = "The Weeknd", songs = listOf(sampleSongs[1])),
        Album(id = 3, title = "Future Nostalgia", artist = "Dua Lipa", songs = listOf(sampleSongs[3]))
    )

    private val sampleArtists = listOf(
        Artist(id = 1, name = "The Weeknd", albums = listOf(sampleAlbums[0], sampleAlbums[1]), songCount = 2),
        Artist(id = 2, name = "Muse", songCount = 1),
        Artist(id = 3, name = "Dua Lipa", albums = listOf(sampleAlbums[2]), songCount = 1)
    )

    private val samplePlaylists = listOf(
        Playlist(id = 1, name = "Star Tracks", songs = listOf(sampleSongs[0], sampleSongs[2])),
        Playlist(id = 2, name = "Pop Vibes", songs = listOf(sampleSongs[3]))
    )

    private val library = MusicLibrary(
        songs = sampleSongs,
        albums = sampleAlbums,
        artists = sampleArtists
    )

    @Test
    fun `empty query returns empty results`() {
        val result = SearchEngine.search("", library, samplePlaylists)
        assertTrue(result.isEmpty)
    }

    @Test
    fun `blank query returns empty results`() {
        val result = SearchEngine.search("   ", library, samplePlaylists)
        assertTrue(result.isEmpty)
    }

    @Test
    fun `exact title match ranks first`() {
        val result = SearchEngine.search("Starboy", library, samplePlaylists)
        assertEquals(1, result.songs.first().id)
        assertEquals("Starboy", result.albums.first().title)
    }

    @Test
    fun `prefix matching works across songs, albums, and playlists`() {
        val result = SearchEngine.search("star", library, samplePlaylists)
        // Songs starting with Star: Starboy, Starlight
        assertEquals(2, result.songs.size)
        assertEquals(listOf(1L, 3L), result.songs.map { it.id })
        // Albums starting with Star: Starboy
        assertEquals(1, result.albums.size)
        // Playlists starting with Star: Star Tracks
        assertEquals(1, result.playlists.size)
        assertEquals("Star Tracks", result.playlists.first().name)
    }

    @Test
    fun `artist match ranks artist entity and songs by that artist`() {
        val result = SearchEngine.search("Weeknd", library, samplePlaylists)
        assertEquals(1, result.artists.size)
        assertEquals("The Weeknd", result.artists.first().name)
        assertEquals(2, result.songs.size)
    }
}
