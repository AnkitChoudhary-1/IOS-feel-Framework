package dev.iosfeel.sonora.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class MusicLibrary(
    val songs: List<Song>,
    val albums: List<Album>,
    val artists: List<Artist>
) {
    companion object {
        val Empty = MusicLibrary(
            songs = emptyList(),
            albums = emptyList(),
            artists = emptyList()
        )
    }
}

@Immutable
data class LibraryStats(
    val songs: Int,
    val albums: Int,
    val artists: Int,
    val totalDurationMs: Long
)

fun MusicLibrary.stats(): LibraryStats {
    return LibraryStats(
        songs = songs.size,
        albums = albums.size,
        artists = artists.size,
        totalDurationMs = songs.sumOf { it.durationMs }
    )
}

data class MusicSearchResult(
    val songs: List<Song>,
    val albums: List<Album>,
    val artists: List<Artist>
)

fun MusicLibrary.search(query: String): MusicSearchResult {
    val cleanQuery = query.trim().lowercase()
    if (cleanQuery.isEmpty()) {
        return MusicSearchResult(emptyList(), emptyList(), emptyList())
    }

    val matchedSongs = songs.filter {
        it.title.lowercase().contains(cleanQuery) ||
                it.artist.lowercase().contains(cleanQuery) ||
                it.album?.lowercase()?.contains(cleanQuery) == true
    }

    val matchedAlbums = albums.filter {
        it.title.lowercase().contains(cleanQuery) ||
                it.artist.lowercase().contains(cleanQuery)
    }

    val matchedArtists = artists.filter {
        it.name.lowercase().contains(cleanQuery)
    }

    return MusicSearchResult(
        songs = matchedSongs,
        albums = matchedAlbums,
        artists = matchedArtists
    )
}
