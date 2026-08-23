package dev.iosfeel.sonora.feature.album

import androidx.compose.runtime.Immutable
import dev.iosfeel.sonora.core.model.Album

@Immutable
data class AlbumUiState(
    val loading: Boolean = false,
    val album: Album? = null,
    val isCurrentAlbum: Boolean = false
)
