package dev.iosfeel.sonora.core.repository

import dev.iosfeel.sonora.core.database.dao.HistoryDao
import dev.iosfeel.sonora.core.database.entity.PlaybackHistoryEntity
import kotlinx.coroutines.flow.Flow

class DefaultPlaybackHistoryRepository(
    private val historyDao: HistoryDao
) : PlaybackHistoryRepository {

    override fun observeRecentlyPlayedIds(limit: Int): Flow<List<Long>> {
        return historyDao.observeRecentIds(limit)
    }

    override fun observeMostPlayedIds(limit: Int): Flow<List<Long>> {
        return historyDao.observeMostPlayedIds(limit)
    }

    override suspend fun recordPlayback(songId: Long) {
        val existing = historyDao.getHistoryForSong(songId)
        val playCount = existing?.playCount ?: 0
        historyDao.recordPlay(
            PlaybackHistoryEntity(
                songId = songId,
                playedAt = System.currentTimeMillis(),
                playCount = playCount
            )
        )
    }

    override suspend fun incrementPlayCount(songId: Long) {
        val existing = historyDao.getHistoryForSong(songId)
        val currentCount = existing?.playCount ?: 0
        historyDao.recordPlay(
            PlaybackHistoryEntity(
                songId = songId,
                playedAt = existing?.playedAt ?: System.currentTimeMillis(),
                playCount = currentCount + 1
            )
        )
    }
}
