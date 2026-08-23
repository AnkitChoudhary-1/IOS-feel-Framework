package dev.iosfeel.sonora.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Artist(
    val id: Long,
    val name: String,
    val albums: List<Album> = emptyList(),
    val songCount: Int = 0
) {
    val albumCount: Int
        get() = albums.size
}
