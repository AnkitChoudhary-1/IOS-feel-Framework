package dev.iosfeel.sonora.core.repository

import dev.iosfeel.sonora.core.database.dao.PlaylistDao
import dev.iosfeel.sonora.core.database.entity.PlaylistEntity
import dev.iosfeel.sonora.core.database.entity.PlaylistSongCrossRef
import dev.iosfeel.sonora.core.model.MusicLibrary
import dev.iosfeel.sonora.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class PlaylistRepositoryTest {

    private lateinit var fakeDao: FakePlaylistDao
    private lateinit var fakeMusicRepo: FakeMusicLibraryRepo
    private lateinit var repository: DefaultPlaylistRepository

    private val song1 = Song(id = 1, title = "Song A", artist = "Artist A", durationMs = 120000)
    private val song2 = Song(id = 2, title = "Song B", artist = "Artist B", durationMs = 150000)
    private val song3 = Song(id = 3, title = "Song C", artist = "Artist C", durationMs = 180000)

    @Before
    fun setup() {
        fakeDao = FakePlaylistDao()
        fakeMusicRepo = FakeMusicLibraryRepo(MusicLibrary(songs = listOf(song1, song2, song3), albums = emptyList(), artists = emptyList()))
        repository = DefaultPlaylistRepository(fakeDao, fakeMusicRepo)
    }

    @Test
    fun `create and observe playlist`() = runBlocking {
        val playlistId = repository.create("Workout Hits")
        assertEquals(1L, playlistId)

        val playlists = repository.playlists.first()
        assertEquals(1, playlists.size)
        assertEquals("Workout Hits", playlists.first().name)
    }

    @Test
    fun `add songs and reorder playlist songs`() = runBlocking {
        val playlistId = repository.create("Chill Mix")
        repository.addSongs(playlistId, listOf(1L, 2L, 3L))

        val playlist = repository.observePlaylist(playlistId).first()
        assertNotNull(playlist)
        assertEquals(3, playlist!!.songs.size)
        assertEquals(listOf(1L, 2L, 3L), playlist.songs.map { it.id })

        // Reorder
        repository.reorderSongs(playlistId, listOf(3L, 1L, 2L))
        val reordered = repository.observePlaylist(playlistId).first()
        assertEquals(listOf(3L, 1L, 2L), reordered!!.songs.map { it.id })
    }

    @Test
    fun `remove song and delete playlist`() = runBlocking {
        val playlistId = repository.create("Roadtrip")
        repository.addSongs(playlistId, listOf(1L, 2L))

        repository.removeSong(playlistId, 1L)
        val playlistAfterRemove = repository.observePlaylist(playlistId).first()
        assertEquals(1, playlistAfterRemove!!.songs.size)
        assertEquals(2L, playlistAfterRemove.songs.first().id)

        repository.delete(playlistId)
        val playlists = repository.playlists.first()
        assertEquals(0, playlists.size)
    }

    private class FakePlaylistDao : PlaylistDao {
        private val playlists = MutableStateFlow<List<PlaylistEntity>>(emptyList())
        private val crossRefs = MutableStateFlow<List<PlaylistSongCrossRef>>(emptyList())
        private var nextId = 1L

        override fun getAllPlaylists(): Flow<List<PlaylistEntity>> = playlists

        override fun observePlaylist(id: Long): Flow<PlaylistEntity?> =
            playlists.map { list -> list.find { it.id == id } }

        override suspend fun getPlaylistById(id: Long): PlaylistEntity? =
            playlists.value.find { it.id == id }

        override suspend fun insertPlaylist(playlist: PlaylistEntity): Long {
            val id = nextId++
            val created = playlist.copy(id = id)
            playlists.value = playlists.value + created
            return id
        }

        override suspend fun updatePlaylist(playlist: PlaylistEntity) {
            playlists.value = playlists.value.map { if (it.id == playlist.id) playlist else it }
        }

        override suspend fun deletePlaylist(playlist: PlaylistEntity) {
            playlists.value = playlists.value.filter { it.id != playlist.id }
        }

        override suspend fun deletePlaylistById(playlistId: Long) {
            playlists.value = playlists.value.filter { it.id != playlistId }
        }

        override fun getSongIdsForPlaylist(playlistId: Long): Flow<List<Long>> =
            crossRefs.map { list ->
                list.filter { it.playlistId == playlistId }
                    .sortedBy { it.orderIndex }
                    .map { it.songId }
            }

        override suspend fun addSongToPlaylist(crossRef: PlaylistSongCrossRef) {
            crossRefs.value = crossRefs.value + crossRef
        }

        override suspend fun insertPlaylistSongs(crossRefs: List<PlaylistSongCrossRef>) {
            this.crossRefs.value = this.crossRefs.value + crossRefs
        }

        override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
            crossRefs.value = crossRefs.value.filter { !(it.playlistId == playlistId && it.songId == songId) }
        }

        override suspend fun clearPlaylistSongs(playlistId: Long) {
            crossRefs.value = crossRefs.value.filter { it.playlistId != playlistId }
        }
    }

    private class FakeMusicLibraryRepo(private val library: MusicLibrary) : MusicLibraryRepository {
        override suspend fun loadLibrary(): MusicLibrary = library
        override fun observeLibrary(): Flow<MusicLibrary> = flowOf(library)
    }
}
