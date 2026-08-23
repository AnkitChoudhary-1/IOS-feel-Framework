package dev.iosfeel.sonora.core.model

import android.net.Uri
import androidx.compose.runtime.Immutable

@Immutable
data class Song(
    val id: Long,
    val title: String,
    val artistId: Long? = null,
    val artist: String,
    val albumId: Long? = null,
    val album: String? = null,
    val durationMs: Long,
    val trackNumber: Int? = null,
    val year: Int? = null,
    val dateAddedSeconds: Long = 0,
    val contentUri: Uri? = null
) {
    val durationFormatted: String
        get() = durationMs.formatDuration()
}

fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
