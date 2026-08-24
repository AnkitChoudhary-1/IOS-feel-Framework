package dev.iosfeel.sonora.core.repository

import dev.iosfeel.sonora.core.model.Playlist
import dev.iosfeel.sonora.core.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    val playlists: Flow<List<Playlist>>
    fun observePlaylist(id: Long): Flow<Playlist?>
    suspend fun create(name: String): Long
    suspend fun rename(playlistId: Long, name: String)
    suspend fun delete(playlistId: Long)
    suspend fun addSong(playlistId: Long, songId: Long)
    suspend fun addSongs(playlistId: Long, songIds: List<Long>)
    suspend fun removeSong(playlistId: Long, songId: Long)
    suspend fun reorderSongs(playlistId: Long, newSongIdsOrder: List<Long>)
}
