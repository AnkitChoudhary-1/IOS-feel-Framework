package dev.iosfeel.sonora.core.repository

import dev.iosfeel.sonora.core.model.Song
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    val favoriteSongIds: Flow<Set<Long>>
    fun observeIsFavorite(songId: Long): Flow<Boolean>
    suspend fun setFavorite(songId: Long, favorite: Boolean)
    suspend fun toggleFavorite(songId: Long)
    fun observeFavoriteSongs(musicRepository: MusicLibraryRepository): Flow<List<Song>>
}
