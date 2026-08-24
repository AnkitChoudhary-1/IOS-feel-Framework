package dev.iosfeel.sonora.feature.search

import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.MusicLibrary
import dev.iosfeel.sonora.core.model.Playlist
import dev.iosfeel.sonora.core.model.Song

enum class SearchMatch(val score: Int) {
    Exact(100),
    Prefix(75),
    Contains(50),
    Secondary(25),
    None(0)
}

data class SearchResult(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList()
) {
    val isEmpty: Boolean
        get() = songs.isEmpty() && albums.isEmpty() && artists.isEmpty() && playlists.isEmpty()
}

object SearchEngine {

    fun search(
        query: String,
        library: MusicLibrary,
        playlists: List<Playlist> = emptyList()
    ): SearchResult {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return SearchResult()
        }

        val normalizedQuery = normalize(trimmed)

        // 1. Search Songs
        val matchedSongs = library.songs.mapNotNull { song ->
            val match = scoreSong(song, normalizedQuery)
            if (match != SearchMatch.None) {
                song to match
            } else null
        }.sortedWith(
            compareByDescending<Pair<Song, SearchMatch>> { it.second.score }
                .thenBy { it.first.title.lowercase() }
        ).map { it.first }

        // 2. Search Albums
        val matchedAlbums = library.albums.mapNotNull { album ->
            val match = scoreAlbum(album, normalizedQuery)
            if (match != SearchMatch.None) {
                album to match
            } else null
        }.sortedWith(
            compareByDescending<Pair<Album, SearchMatch>> { it.second.score }
                .thenBy { it.first.title.lowercase() }
        ).map { it.first }

        // 3. Search Artists
        val matchedArtists = library.artists.mapNotNull { artist ->
            val match = scoreArtist(artist, normalizedQuery)
            if (match != SearchMatch.None) {
                artist to match
            } else null
        }.sortedWith(
            compareByDescending<Pair<Artist, SearchMatch>> { it.second.score }
                .thenBy { it.first.name.lowercase() }
        ).map { it.first }

        // 4. Search Playlists
        val matchedPlaylists = playlists.mapNotNull { playlist ->
            val match = scorePlaylist(playlist, normalizedQuery)
            if (match != SearchMatch.None) {
                playlist to match
            } else null
        }.sortedWith(
            compareByDescending<Pair<Playlist, SearchMatch>> { it.second.score }
                .thenBy { it.first.name.lowercase() }
        ).map { it.first }

        return SearchResult(
            songs = matchedSongs,
            albums = matchedAlbums,
            artists = matchedArtists,
            playlists = matchedPlaylists
        )
    }

    private fun normalize(value: String?): String =
        value?.trim()?.lowercase() ?: ""

    private fun scoreSong(song: Song, query: String): SearchMatch {
        val title = normalize(song.title)
        val artist = normalize(song.artist)
        val album = normalize(song.album)

        return when {
            title == query -> SearchMatch.Exact
            title.startsWith(query) -> SearchMatch.Prefix
            title.contains(query) -> SearchMatch.Contains
            artist.startsWith(query) || album.startsWith(query) -> SearchMatch.Secondary
            artist.contains(query) || album.contains(query) -> SearchMatch.Secondary
            else -> SearchMatch.None
        }
    }

    private fun scoreAlbum(album: Album, query: String): SearchMatch {
        val title = normalize(album.title)
        val artist = normalize(album.artist)

        return when {
            title == query -> SearchMatch.Exact
            title.startsWith(query) -> SearchMatch.Prefix
            title.contains(query) -> SearchMatch.Contains
            artist.startsWith(query) || artist.contains(query) -> SearchMatch.Secondary
            else -> SearchMatch.None
        }
    }

    private fun scoreArtist(artist: Artist, query: String): SearchMatch {
        val name = normalize(artist.name)

        return when {
            name == query -> SearchMatch.Exact
            name.startsWith(query) -> SearchMatch.Prefix
            name.contains(query) -> SearchMatch.Contains
            else -> SearchMatch.None
        }
    }

    private fun scorePlaylist(playlist: Playlist, query: String): SearchMatch {
        val name = normalize(playlist.name)

        return when {
            name == query -> SearchMatch.Exact
            name.startsWith(query) -> SearchMatch.Prefix
            name.contains(query) -> SearchMatch.Contains
            else -> SearchMatch.None
        }
    }
}
