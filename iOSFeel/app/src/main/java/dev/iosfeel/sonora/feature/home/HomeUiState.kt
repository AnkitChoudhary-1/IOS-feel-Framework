package dev.iosfeel.sonora.feature.home

import androidx.compose.runtime.Immutable
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.LibraryStats
import dev.iosfeel.sonora.core.model.Song

@Immutable
data class HomeUiState(
    val loading: Boolean = true,
    val recentlyPlayed: List<Song> = emptyList(),
    val recentlyAdded: List<Album> = emptyList(),
    val mostPlayed: List<Song> = emptyList(),
    val recentArtists: List<Artist> = emptyList(),
    val libraryStats: LibraryStats? = null
)
