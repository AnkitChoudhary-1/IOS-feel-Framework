package dev.iosfeel.sonora.core.repository

import dev.iosfeel.sonora.core.database.dao.FavoriteDao
import dev.iosfeel.sonora.core.database.entity.FavoriteEntity
import dev.iosfeel.sonora.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DefaultFavoritesRepository(
    private val favoriteDao: FavoriteDao
) : FavoritesRepository {

    override val favoriteSongIds: Flow<Set<Long>> =
        favoriteDao.getAllFavoriteIds().map { it.toSet() }

    override fun observeIsFavorite(songId: Long): Flow<Boolean> {
        return favoriteDao.isFavorite(songId)
    }

    override suspend fun setFavorite(songId: Long, favorite: Boolean) {
        if (favorite) {
            favoriteDao.addFavorite(FavoriteEntity(songId = songId, addedAt = System.currentTimeMillis()))
        } else {
            favoriteDao.removeFavorite(songId)
        }
    }

    override suspend fun toggleFavorite(songId: Long) {
        val isCurrentlyFav = favoriteDao.isFavorite(songId).firstOrNull() ?: false
        setFavorite(songId, !isCurrentlyFav)
    }

    override fun observeFavoriteSongs(musicRepository: MusicLibraryRepository): Flow<List<Song>> {
        return combine(favoriteDao.getAllFavoriteIds(), musicRepository.observeLibrary()) { favIds: List<Long>, library: dev.iosfeel.sonora.core.model.MusicLibrary ->
            val songMap = library.songs.associateBy { it.id }
            // Preserves favorite order (addedAt DESC) and gracefully drops deleted/stale songs
            favIds.mapNotNull { id -> songMap[id] }
        }
    }
}
