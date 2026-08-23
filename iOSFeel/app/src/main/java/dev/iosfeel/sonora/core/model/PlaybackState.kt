package dev.iosfeel.sonora.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val queue: List<Song> = emptyList(),
    val currentQueueIndex: Int = -1,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
    val playbackSpeed: Float = 1f,
    val error: PlaybackError? = null
) {
    val hasActiveMedia: Boolean
        get() = currentSong != null

    val progressFraction: Float
        get() {
            if (durationMs <= 0L) return 0f
            return (positionMs.toFloat() / durationMs).coerceIn(0f, 1f)
        }

    val progress: Float
        get() = progressFraction
}
