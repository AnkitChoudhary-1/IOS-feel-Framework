package dev.iosfeel.sonora.core.repository

import dev.iosfeel.sonora.core.database.dao.FavoriteDao
import dev.iosfeel.sonora.core.database.entity.FavoriteEntity
import dev.iosfeel.sonora.core.model.MusicLibrary
import dev.iosfeel.sonora.core.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavoritesRepositoryTest {

    private lateinit var fakeDao: FakeFavoriteDao
    private lateinit var fakeMusicRepo: FakeMusicLibraryRepo
    private lateinit var repository: DefaultFavoritesRepository

    private val song1 = Song(id = 101, title = "Song 1", artist = "Artist 1", durationMs = 180000)
    private val song2 = Song(id = 102, title = "Song 2", artist = "Artist 2", durationMs = 210000)

    @Before
    fun setup() {
        fakeDao = FakeFavoriteDao()
        fakeMusicRepo = FakeMusicLibraryRepo(MusicLibrary(songs = listOf(song1, song2), albums = emptyList(), artists = emptyList()))
        repository = DefaultFavoritesRepository(fakeDao)
    }

    @Test
    fun `toggleFavorite toggles favorite state`() = runBlocking {
        assertFalse(repository.observeIsFavorite(101).first())

        repository.toggleFavorite(101)
        assertTrue(repository.observeIsFavorite(101).first())

        repository.toggleFavorite(101)
        assertFalse(repository.observeIsFavorite(101).first())
    }

    @Test
    fun `observeFavoriteSongs resolves domain songs and filters out stale IDs`() = runBlocking {
        repository.setFavorite(101, true)
        repository.setFavorite(999, true) // Stale ID not in MediaStore

        val favSongs = repository.observeFavoriteSongs(fakeMusicRepo).first()
        assertEquals(1, favSongs.size)
        assertEquals(101L, favSongs.first().id)
    }

    private class FakeFavoriteDao : FavoriteDao {
        private val favorites = MutableStateFlow<List<FavoriteEntity>>(emptyList())

        override fun getAllFavoriteIds(): Flow<List<Long>> = favorites.map { list -> list.map { it.songId } }

        override fun isFavorite(songId: Long): Flow<Boolean> = favorites.map { list -> list.any { it.songId == songId } }

        override suspend fun addFavorite(favorite: FavoriteEntity) {
            favorites.value = favorites.value.filter { it.songId != favorite.songId } + favorite
        }

        override suspend fun removeFavorite(songId: Long) {
            favorites.value = favorites.value.filter { it.songId != songId }
        }
    }

    private class FakeMusicLibraryRepo(private val library: MusicLibrary) : MusicLibraryRepository {
        override suspend fun loadLibrary(): MusicLibrary = library
        override fun observeLibrary(): Flow<MusicLibrary> = flowOf(library)
    }
}
