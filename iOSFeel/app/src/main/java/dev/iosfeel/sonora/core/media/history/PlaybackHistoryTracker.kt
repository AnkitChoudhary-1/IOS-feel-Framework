package dev.iosfeel.sonora.core.media.history

import dev.iosfeel.sonora.core.model.Song
import dev.iosfeel.sonora.core.repository.PlaybackHistoryRepository

class PlaybackHistoryTracker(
    private val historyRepository: PlaybackHistoryRepository
) {
    private var currentTrackingSongId: Long? = null
    private var hasIncrementedPlayCount: Boolean = false

    suspend fun onSongStarted(song: Song) {
        currentTrackingSongId = song.id
        hasIncrementedPlayCount = false
        historyRepository.recordPlayback(song.id)
    }

    suspend fun onPositionUpdated(songId: Long, positionMs: Long, durationMs: Long) {
        if (currentTrackingSongId != songId || hasIncrementedPlayCount) return

        val reachedThirtySeconds = positionMs >= 30_000L
        val reachedFiftyPercent = durationMs > 0L && positionMs >= (durationMs / 2L)

        if (reachedThirtySeconds || reachedFiftyPercent) {
            hasIncrementedPlayCount = true
            historyRepository.incrementPlayCount(songId)
        }
    }

    fun onSongCompletedOrChanged() {
        currentTrackingSongId = null
        hasIncrementedPlayCount = false
    }
}
