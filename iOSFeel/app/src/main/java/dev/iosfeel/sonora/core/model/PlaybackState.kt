package dev.iosfeel.sonora.core.model

enum class RepeatMode {
    Off,
    All,
    One
}

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val playbackSpeed: Float = 1.0f
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
