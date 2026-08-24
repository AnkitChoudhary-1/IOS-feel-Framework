package dev.iosfeel.sonora.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Playlist(
    val id: Long,
    val name: String,
    val songs: List<Song> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalDurationMs: Long
        get() = songs.sumOf { it.durationMs }

    val formattedDuration: String
        get() {
            val totalSeconds = totalDurationMs / 1000
            val minutes = totalSeconds / 60
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            return if (hours > 0) {
                "${hours} hr ${remainingMinutes} min"
            } else {
                "${minutes} min"
            }
        }
}
