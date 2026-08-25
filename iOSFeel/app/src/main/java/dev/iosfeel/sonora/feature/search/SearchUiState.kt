package dev.iosfeel.sonora.feature.search

import androidx.compose.runtime.Immutable
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.Playlist
import dev.iosfeel.sonora.core.model.Song

enum class SearchSourceScope {
    ALL,
    YOUTUBE_MUSIC,
    LOCAL
}

@Immutable
data class SearchUiState(
    val query: String = "",
    val searching: Boolean = false,
    val isOnlineLoading: Boolean = false,
    val searchScope: SearchSourceScope = SearchSourceScope.ALL,
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val onlineSongs: List<Song> = emptyList(),
    val onlineAlbums: List<Album> = emptyList(),
    val onlineArtists: List<Artist> = emptyList(),
    val recentSearches: List<String> = emptyList()
) {
    val isQueryEmpty: Boolean
        get() = query.trim().isEmpty()

    val hasResults: Boolean
        get() = songs.isNotEmpty() || albums.isNotEmpty() || artists.isNotEmpty() || playlists.isNotEmpty() || onlineSongs.isNotEmpty() || onlineAlbums.isNotEmpty()

    val isSearchingOrActive: Boolean
        get() = searching || isOnlineLoading || query.isNotEmpty()
}
