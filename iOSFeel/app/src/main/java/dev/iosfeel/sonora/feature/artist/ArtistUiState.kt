package dev.iosfeel.sonora.feature.artist

import androidx.compose.runtime.Immutable
import dev.iosfeel.sonora.core.model.Artist

@Immutable
data class ArtistUiState(
    val loading: Boolean = false,
    val artist: Artist? = null
)
