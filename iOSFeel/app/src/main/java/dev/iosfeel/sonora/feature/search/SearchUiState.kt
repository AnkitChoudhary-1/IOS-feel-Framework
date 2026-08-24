package dev.iosfeel.sonora.feature.search

import androidx.compose.runtime.Immutable
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.Playlist
import dev.iosfeel.sonora.core.model.Song

@Immutable
data class SearchUiState(
    val query: String = "",
    val searching: Boolean = false,
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val recentSearches: List<String> = emptyList()
) {
    val isQueryEmpty: Boolean
        get() = query.trim().isEmpty()

    val hasResults: Boolean
        get() = songs.isNotEmpty() || albums.isNotEmpty() || artists.isNotEmpty() || playlists.isNotEmpty()

    val isSearchingOrActive: Boolean
        get() = searching || query.isNotEmpty()
}
