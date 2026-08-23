package dev.iosfeel.sonora.feature.library

import androidx.compose.runtime.Immutable
import dev.iosfeel.sonora.core.model.Album
import dev.iosfeel.sonora.core.model.Artist
import dev.iosfeel.sonora.core.model.LibrarySection
import dev.iosfeel.sonora.core.model.MusicLibrary
import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.core.model.SongSort
import dev.iosfeel.sonora.core.model.SortDirection

@Immutable
data class LibraryUiState(
    val permissionGranted: Boolean = false,
    val loading: Boolean = false,
    val library: MusicLibrary = MusicLibrary.Empty,
    val section: LibrarySection = LibrarySection.Songs,
    val songSort: SongSort = SongSort.Title,
    val sortDirection: SortDirection = SortDirection.Ascending,
    val selectedAlbum: Album? = null,
    val selectedArtist: Artist? = null,
    val error: LibraryError? = null
)

sealed interface LibraryError {
    data object PermissionDenied : LibraryError
    data object QueryFailed : LibraryError
    data class Unknown(val cause: Throwable) : LibraryError
}
