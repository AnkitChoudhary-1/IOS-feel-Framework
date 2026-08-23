package dev.iosfeel.sonora.core.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val artistId: Long = 0,
    val album: String,
    val albumId: Long = 0,
    val durationMs: Long,
    val trackNumber: Int = 0,
    val uri: Uri? = null,
    val artworkUri: Uri? = null,
    val year: Int = 0,
    val dateAdded: Long = 0,
    val sizeBytes: Long = 0,
    val isFavorite: Boolean = false
) {
    val durationFormatted: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
