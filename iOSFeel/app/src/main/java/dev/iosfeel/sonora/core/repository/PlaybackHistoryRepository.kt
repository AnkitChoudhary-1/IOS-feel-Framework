package dev.iosfeel.sonora.core.repository

import kotlinx.coroutines.flow.Flow

interface PlaybackHistoryRepository {
    fun observeRecentlyPlayedIds(limit: Int = 30): Flow<List<Long>>
    fun observeMostPlayedIds(limit: Int = 30): Flow<List<Long>>
    suspend fun recordPlayback(songId: Long)
    suspend fun incrementPlayCount(songId: Long)
}
