package dev.iosfeel.sonora.core.repository

import dev.iosfeel.sonora.core.database.dao.PlaylistDao
import dev.iosfeel.sonora.core.database.entity.PlaylistEntity
import dev.iosfeel.sonora.core.database.entity.PlaylistSongCrossRef
import dev.iosfeel.sonora.core.model.Playlist
import dev.iosfeel.sonora.core.model.Song
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultPlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val musicRepository: MusicLibraryRepository
) : PlaylistRepository {

    override val playlists: Flow<List<Playlist>> =
        combine(
            playlistDao.getAllPlaylists(),
            musicRepository.observeLibrary()
        ) { entities, library: dev.iosfeel.sonora.core.model.MusicLibrary ->
            entities to library
        }.flatMapLatest { (entities, library) ->
            if (entities.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    entities.map { entity ->
                        playlistDao.getSongIdsForPlaylist(entity.id).map { songIds ->
                            val songMap = library.songs.associateBy { it.id }
                            val resolvedSongs = songIds.mapNotNull { songMap[it] }
                            Playlist(
                                id = entity.id,
                                name = entity.name,
                                songs = resolvedSongs,
                                createdAt = entity.createdAt,
                                updatedAt = entity.updatedAt
                            )
                        }
                    }
                ) { playlistArray ->
                    playlistArray.toList()
                }
            }
        }

    override fun observePlaylist(id: Long): Flow<Playlist?> {
        return combine(
            playlistDao.observePlaylist(id),
            playlistDao.getSongIdsForPlaylist(id),
            musicRepository.observeLibrary()
        ) { entity, songIds, library: dev.iosfeel.sonora.core.model.MusicLibrary ->
            if (entity == null) return@combine null
            val songMap = library.songs.associateBy { it.id }
            val resolvedSongs = songIds.mapNotNull { songMap[it] }
            Playlist(
                id = entity.id,
                name = entity.name,
                songs = resolvedSongs,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }

    override suspend fun create(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return -1L
        val now = System.currentTimeMillis()
        return playlistDao.insertPlaylist(
            PlaylistEntity(
                name = trimmed,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    override suspend fun rename(playlistId: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        val entity = playlistDao.getPlaylistById(playlistId) ?: return
        playlistDao.updatePlaylist(
            entity.copy(
                name = trimmed,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun delete(playlistId: Long) {
        playlistDao.clearPlaylistSongs(playlistId)
        playlistDao.deletePlaylistById(playlistId)
    }

    override suspend fun addSong(playlistId: Long, songId: Long) {
        val currentSongIds = playlistDao.getSongIdsForPlaylist(playlistId).firstOrNull() ?: emptyList()
        if (currentSongIds.contains(songId)) return
        val nextOrder = currentSongIds.size
        playlistDao.addSongToPlaylist(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                orderIndex = nextOrder,
                addedAt = System.currentTimeMillis()
            )
        )
        val entity = playlistDao.getPlaylistById(playlistId)
        if (entity != null) {
            playlistDao.updatePlaylist(entity.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    override suspend fun addSongs(playlistId: Long, songIds: List<Long>) {
        if (songIds.isEmpty()) return
        val currentSongIds = playlistDao.getSongIdsForPlaylist(playlistId).firstOrNull() ?: emptyList()
        val currentSet = currentSongIds.toSet()
        val newSongs = songIds.filter { !currentSet.contains(it) }
        if (newSongs.isEmpty()) return

        var nextOrder = currentSongIds.size
        val crossRefs = newSongs.map { songId ->
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                orderIndex = nextOrder++,
                addedAt = System.currentTimeMillis()
            )
        }
        playlistDao.insertPlaylistSongs(crossRefs)
        val entity = playlistDao.getPlaylistById(playlistId)
        if (entity != null) {
            playlistDao.updatePlaylist(entity.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    override suspend fun removeSong(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
        val remainingIds = playlistDao.getSongIdsForPlaylist(playlistId).firstOrNull() ?: emptyList()
        reorderSongs(playlistId, remainingIds)
    }

    override suspend fun reorderSongs(playlistId: Long, newSongIdsOrder: List<Long>) {
        playlistDao.clearPlaylistSongs(playlistId)
        val now = System.currentTimeMillis()
        val newRefs = newSongIdsOrder.mapIndexed { index, songId ->
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                orderIndex = index,
                addedAt = now
            )
        }
        playlistDao.insertPlaylistSongs(newRefs)
        val entity = playlistDao.getPlaylistById(playlistId)
        if (entity != null) {
            playlistDao.updatePlaylist(entity.copy(updatedAt = now))
        }
    }
}
