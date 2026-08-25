package dev.iosfeel.sonora.feature.home

import androidx.compose.runtime.Immutable
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.LibraryStats
import dev.iosfeel.sonora.core.model.Playlist
import dev.iosfeel.sonora.core.model.Song

@Immutable
data class HomeUiState(
    val loading: Boolean = true,
    val recentlyPlayed: List<Song> = emptyList(),
    val recentlyAdded: List<Album> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val mostPlayed: List<Song> = emptyList(),
    val quickPicks: List<Song> = emptyList(),
    val featuredAlbums: List<Album> = emptyList(),
    val recentArtists: List<Artist> = emptyList(),
    val trendingOnline: List<Song> = emptyList(),
    val newReleasesOnline: List<Album> = emptyList(),
    val chartsOnline: List<Song> = emptyList(),
    val libraryStats: LibraryStats? = null
) {
    val isEmpty: Boolean
        get() = !loading && recentlyPlayed.isEmpty() && recentlyAdded.isEmpty() && quickPicks.isEmpty() && featuredAlbums.isEmpty() && favorites.isEmpty() && playlists.isEmpty() && trendingOnline.isEmpty()
}
